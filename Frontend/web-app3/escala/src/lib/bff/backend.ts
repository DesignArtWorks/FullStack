import { getToken } from 'next-auth/jwt';
import { NextResponse } from 'next/server';
import { ENV } from '@/constants/env';
import { getOptionalServerAccessToken } from '@/lib/auth/server-auth';
import { rejectCrossSiteBffRequest } from '@/lib/bff/request-origin';

type BackendRequestOptions = {
  method?: string;
  body?: unknown;
  authenticated?: boolean;
  searchParams?: URLSearchParams;
  request?: Request;
  extraHeaders?: Record<string, string>;
};

type PublicApiError = {
  code: string;
  message: string;
  status: number;
  errorId: string;
  correlationId: string;
};

const CORRELATION_HEADER = 'X-Correlation-ID';
const SAFE_CORRELATION_ID = /^[A-Za-z0-9][A-Za-z0-9._:-]{0,127}$/;

function correlationIdFor(request?: Request) {
  const supplied = request?.headers.get(CORRELATION_HEADER);
  return supplied && SAFE_CORRELATION_ID.test(supplied) ? supplied : crypto.randomUUID();
}

function errorResponse(status: number, code: string, message: string, correlationId: string) {
  const error: PublicApiError = {
    code,
    message,
    status,
    errorId: `ERR-${crypto.randomUUID()}`,
    correlationId,
  };
  return NextResponse.json(error, { status, headers: responseHeaders(correlationId) });
}

function responseHeaders(correlationId: string) {
  return {
    'Cache-Control': 'no-store',
    'X-Content-Type-Options': 'nosniff',
    'Referrer-Policy': 'strict-origin-when-cross-origin',
    [CORRELATION_HEADER]: correlationId,
  };
}

export async function proxyBackend(path: string, options: BackendRequestOptions = {}) {
  const correlationId = correlationIdFor(options.request);
  if (options.request) {
    const originError = rejectCrossSiteBffRequest(options.request);
    if (originError) return originError;
  }

  const url = new URL(path, ENV.API_BASE_URL);
  if (options.searchParams) {
    options.searchParams.forEach((value, key) => url.searchParams.set(key, value));
  }

  const headers: HeadersInit = {
    Accept: 'application/json',
    [CORRELATION_HEADER]: correlationId,
  };

  const isFormData = options.body instanceof FormData;

  if (options.body !== undefined && !isFormData) {
    headers['Content-Type'] = 'application/json';
  }

  if (options.request) {
    const userAgent = options.request.headers.get('user-agent');
    const forwardedProto = options.request.headers.get('x-forwarded-proto');
    const forwardedHost = options.request.headers.get('x-forwarded-host') || options.request.headers.get('host');

    if (userAgent) {
      headers['User-Agent'] = userAgent;
    }
    if (forwardedProto) {
      headers['X-Forwarded-Proto'] = forwardedProto;
    }
    if (forwardedHost) {
      headers['X-Forwarded-Host'] = forwardedHost;
    }
  }

  if (options.extraHeaders) {
    Object.assign(headers, options.extraHeaders);
  }
  // Callers may add conditional headers, but correlation is owned by this boundary.
  headers[CORRELATION_HEADER] = correlationId;

  if (options.authenticated !== false) {
    let accessToken: string | undefined;
    // 1. Tenta extrair o token do cookie (NextAuth) se houver requisição
    if (options.request) {
      const jwtToken = await getToken({
        req: options.request as any,
        secret: process.env.NEXTAUTH_SECRET ?? process.env.AUTH_SECRET,
      });
      const sessionAccessToken =
        typeof jwtToken?.accessToken === 'string' ? jwtToken.accessToken : undefined;
      accessToken = sessionAccessToken || accessToken;
    }

    // 2. Fallback: tenta obter da sessão (necessário para Server Components e chamadas server-side)
    if (!accessToken) {
      accessToken = (await getOptionalServerAccessToken()) ?? undefined;
    }

    if (!accessToken) {
      return errorResponse(401, 'AUTHENTICATION_REQUIRED', 'Autenticacao necessaria.', correlationId);
    }
    headers.Authorization = `Bearer ${accessToken}`;
  }

  let body: BodyInit | undefined;
  if (options.body instanceof FormData) {
    body = options.body;
  } else if (options.body !== undefined) {
    body = JSON.stringify(options.body);
  }

  let response: Response;
  try {
    response = await fetch(url, {
      method: options.method ?? 'GET',
      headers,
      body,
      cache: 'no-store',
    });
  } catch (error) {
    console.warn(JSON.stringify({
      level: 'warn', service: 'escala-bff', event: 'backend_unavailable', correlationId,
      method: options.method ?? 'GET', route: path, errorType: error instanceof Error ? error.name : 'UnknownError',
    }));
    return errorResponse(503, 'BACKEND_UNAVAILABLE', 'Servico temporariamente indisponivel.', correlationId);
  }

  const contentType = response.headers.get('content-type') ?? '';
  if (response.status === 204) {
    return new NextResponse(null, {
      status: 204,
      headers: responseHeaders(response.headers.get(CORRELATION_HEADER) ?? correlationId),
    });
  }

  const responseCorrelationId = response.headers.get(CORRELATION_HEADER) ?? correlationId;
  const data = contentType.includes('application/json') ? await response.json() : await response.text();

  if (!response.ok) {
    const backendError = typeof data === 'object' && data !== null ? data as Partial<PublicApiError> : {};
    const defaults: Record<number, [string, string]> = {
      400: ['BAD_REQUEST', 'Requisicao invalida.'], 401: ['AUTHENTICATION_REQUIRED', 'Autenticacao necessaria.'],
      403: ['ACCESS_DENIED', 'Acesso negado.'], 404: ['RESOURCE_NOT_FOUND', 'Recurso nao encontrado.'],
      409: ['CONFLICT', 'A operacao conflita com o estado atual.'], 429: ['RATE_LIMITED', 'Muitas requisicoes. Tente novamente em instantes.'],
    };
    const [defaultCode, defaultMessage] = defaults[response.status] ?? ['UPSTREAM_ERROR', 'Nao foi possivel concluir a operacao.'];
    return NextResponse.json({
      code: backendError.code ?? defaultCode,
      message: backendError.message ?? defaultMessage,
      status: response.status,
      errorId: backendError.errorId ?? `ERR-${crypto.randomUUID()}`,
      correlationId: responseCorrelationId,
    } satisfies PublicApiError, { status: response.status, headers: responseHeaders(responseCorrelationId) });
  }

  return NextResponse.json(data, {
    status: response.status,
    headers: responseHeaders(responseCorrelationId),
  });
}

export async function readJson(request: Request) {
  const text = await request.text();
  return text ? JSON.parse(text) : undefined;
}
