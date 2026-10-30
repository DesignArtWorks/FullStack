export interface CheckInPayload {
  latitude: number;
  longitude: number;
  deviceFingerprint: string;
}

export type CheckInResult = { ok: true } | { ok: false; message?: string };

/** Registro de ponto pelo navegador, sempre via BFF. */
export class CheckInAdapter {
  private static readonly url = '/api/bff/check-in';

  static async register(payload: CheckInPayload): Promise<CheckInResult> {
    const response = await fetch(this.url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (response.ok) return { ok: true };
    const error = await response.json();
    return { ok: false, message: error.message };
  }
}
