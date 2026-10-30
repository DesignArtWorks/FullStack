/** Envio do formulario de contato pelo navegador, sempre via BFF. */
export class ContactClientAdapter {
  private static readonly url = '/api/bff/contact';

  static async send(body: Record<string, unknown>): Promise<void> {
    const response = await fetch(this.url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });

    if (!response.ok) throw new Error('Falha ao enviar mensagem');
  }
}
