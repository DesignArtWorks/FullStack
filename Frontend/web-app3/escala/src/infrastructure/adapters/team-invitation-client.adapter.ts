import { TeamInvitation } from '@/core/domain/models/invitation.model';

export interface InvitePayload {
  email: string;
  roleName: string;
}

export type InviteResult =
  | { ok: true; invitation: TeamInvitation }
  | { ok: false; message?: string };

/** Convites de equipe consumidos pelo navegador, sempre via BFF. Preserva a mensagem de erro do servidor. */
export class TeamInvitationClientAdapter {
  private static readonly baseUrl = '/api/bff/team/invitations';

  /** Devolve null quando o BFF responde com erro. */
  static async list(): Promise<TeamInvitation[] | null> {
    const response = await fetch(this.baseUrl);
    if (!response.ok) return null;
    return (await response.json()) as TeamInvitation[];
  }

  static async create(payload: InvitePayload): Promise<InviteResult> {
    const response = await fetch(this.baseUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (response.ok) {
      return { ok: true, invitation: (await response.json()) as TeamInvitation };
    }
    const error = await response.json();
    return { ok: false, message: error.message };
  }

  static async cancel(id: string): Promise<boolean> {
    const response = await fetch(`${this.baseUrl}/${id}`, { method: 'DELETE' });
    return response.ok;
  }
}
