import { NextResponse } from 'next/server';
import { resolveAvatarReadOwner } from '@/lib/avatar/principal';
import { readPrivateAvatarFile } from '@/lib/avatar/storage';

export const runtime = 'nodejs';

type RouteContext = {
  params: Promise<{
    fileName: string;
  }>;
};

export async function GET(request: Request, context: RouteContext) {
  const { fileName } = await context.params;
  const owner = await resolveAvatarReadOwner(request, fileName);
  if (owner instanceof NextResponse) return owner;
  const avatar = await readPrivateAvatarFile(owner, fileName).catch(() => null);

  if (!avatar) {
    return NextResponse.json({ message: 'Avatar not found' }, { status: 404, headers: { 'Cache-Control': 'no-store' } });
  }

  return new NextResponse(avatar.bytes, {
    status: 200,
    headers: {
      'Content-Type': avatar.contentType,
      'Cache-Control': 'private, no-store',
      'Content-Disposition': 'inline',
      'X-Content-Type-Options': 'nosniff',
    },
  });
}
