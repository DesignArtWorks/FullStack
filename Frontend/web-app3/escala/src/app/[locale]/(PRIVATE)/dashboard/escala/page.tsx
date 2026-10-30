import { endOfMonth, format, startOfMonth } from 'date-fns';
import { getEscalas, getMinhasEscalas, getUsuariosEscala } from '@/core/adapters/escala.service';
import { EscalaPage as EscalaFeaturePage } from '@/features/escala/pages/EscalaPage';
import { sanitizeClientUser } from '@/lib/auth/client-user';
import { getRequiredServerAuth } from '@/lib/auth/server-auth';
import { canManageSchedules } from '@/lib/permissions';

export default async function EscalaPage() {
  const { session, accessToken } = await getRequiredServerAuth();

  const hoje = new Date();
  const periodo = {
    inicio: format(startOfMonth(hoje), 'yyyy-MM-dd'),
    fim: format(endOfMonth(hoje), 'yyyy-MM-dd'),
  };
  const roles = session.user.roles ?? [];
  const canViewAllEscalas = canManageSchedules(roles);
  const escalas = canViewAllEscalas
    ? await getEscalas(periodo, { authToken: accessToken })
    : await getMinhasEscalas(periodo, { authToken: accessToken });
  const usuarios = canViewAllEscalas ? await getUsuariosEscala(undefined, { authToken: accessToken }) : [];

  return <EscalaFeaturePage user={sanitizeClientUser(session.user)} escalas={escalas} usuarios={usuarios} />;
}
