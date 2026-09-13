export type AvatarOwner = { userId: string; companyId: string };

const UUID = '[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}';
const uuidPattern = new RegExp(`^${UUID}$`);
const avatarPattern = new RegExp(`^(${UUID})-(${UUID})-(${UUID})\\.(jpg|png|webp)$`);

export function isAvatarOwner(value: AvatarOwner | null | undefined): value is AvatarOwner {
  return !!value && uuidPattern.test(value.userId) && uuidPattern.test(value.companyId);
}

export function avatarOwnerFromFileName(fileName: string): AvatarOwner | null {
  const match = avatarPattern.exec(fileName);
  return match ? { companyId: match[1], userId: match[2] } : null;
}

/** Compare both immutable identifiers; profile.avatarUrl is never an authority. */
export function ownsAvatar(owner: AvatarOwner, fileName: string): boolean {
  if (!isAvatarOwner(owner)) return false;
  const match = avatarPattern.exec(fileName);
  return !!match && match[1] === owner.companyId && match[2] === owner.userId;
}
