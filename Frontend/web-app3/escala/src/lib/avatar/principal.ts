import 'server-only';
import { NextResponse } from 'next/server';
import { proxyBackend } from '@/lib/bff/backend';
import { avatarOwnerFromFileName, isAvatarOwner, ownsAvatar, type AvatarOwner } from './owner';

/** Revalidate the bearer with Spring, including its current company and status. */
export async function resolveAvatarPrincipal(request: Request): Promise<AvatarOwner | NextResponse> {
  const response = await proxyBackend('/api/v1/users/me', { request });
  if (!response.ok) return response;
  const profile = await response.json();
  const owner = { userId: profile?.id, companyId: profile?.companyId };
  if (profile?.active !== true || !isAvatarOwner(owner)) {
    return NextResponse.json({ message: 'Forbidden' }, { status: 403, headers: { 'Cache-Control': 'no-store' } });
  }
  return owner;
}

/** Spring authorizes active colleagues using the current membership of both users. */
export async function resolveAvatarReadOwner(request: Request, fileName: string): Promise<AvatarOwner | NextResponse> {
  const requested = avatarOwnerFromFileName(fileName);
  const notFound = () => NextResponse.json({ message: 'Avatar not found' }, { status: 404, headers: { 'Cache-Control': 'no-store' } });
  if (!requested) return notFound();
  const response = await proxyBackend(`/api/v1/users/${requested.userId}/avatar-access`, { request });
  if (!response.ok) return response;
  const authorized = await response.json();
  if (!ownsAvatar(authorized, fileName)) return notFound();
  return authorized;
}
