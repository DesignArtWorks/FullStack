/**
 * Regras de exibicao por papel. Servem apenas para decidir o que a interface mostra;
 * a autorizacao real e sempre aplicada pelo backend.
 */
type Roles = readonly string[] | null | undefined;

const hasRole = (roles: Roles, role: string): boolean => roles?.includes(role) ?? false;

/** ADMIN ou OWNER. */
export function isAdminOrOwner(roles: Roles): boolean {
  return hasRole(roles, 'ADMIN') || hasRole(roles, 'OWNER');
}

/** ADMIN, OWNER ou qualquer papel MANAGER*: quem pode gerir escalas. */
export function canManageSchedules(roles: Roles): boolean {
  return isAdminOrOwner(roles) || (roles?.some((role) => role.startsWith('MANAGER')) ?? false);
}
