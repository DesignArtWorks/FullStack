import { API_ROUTES } from '@/constants';
import type { LeadCaptureResponse } from '@/types/campaign';

/** Captura publica de lead pelo navegador, sempre via BFF. */
export class LeadCaptureAdapter {
  static async capture(body: Record<string, unknown>): Promise<LeadCaptureResponse> {
    const response = await fetch(API_ROUTES.LEADS, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    const data = (await response.json()) as Partial<LeadCaptureResponse> & { message?: string };

    if (!response.ok) {
      throw new Error(data.message || 'Nao foi possivel registrar o lead');
    }

    return data as LeadCaptureResponse;
  }
}
