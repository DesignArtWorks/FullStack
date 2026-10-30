import { SettingsForm } from '@/components/dashboard/SettingsForm';
import { sanitizeClientUser } from '@/lib/auth/client-user';
import { getRequiredServerAuth } from '@/lib/auth/server-auth';
import { isAdminOrOwner } from '@/lib/permissions';

export const metadata = {
  title: 'Configurações | Plataforma Escala',
};

export default async function SettingsPage() {
  const { session } = await getRequiredServerAuth();

  // Permissão de administrador ou owner
  const isAdmin = isAdminOrOwner(session.user.roles);

  return <SettingsForm user={sanitizeClientUser(session.user)} isAdmin={isAdmin} />;
}
