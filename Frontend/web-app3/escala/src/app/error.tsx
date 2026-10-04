'use client';

import { useEffect } from 'react';

export default function GlobalError({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  useEffect(() => {
    console.error('Erro de interface', { digest: error.digest });
  }, [error]);

  return (
    <main className="flex min-h-screen items-center justify-center p-6">
      <section className="max-w-md text-center" role="alert">
        <h1 className="text-2xl font-semibold">Nao foi possivel carregar esta tela</h1>
        <p className="mt-3 text-muted-foreground">Tente novamente. Se o problema continuar, informe o codigo {error.digest ?? 'indisponivel'} ao suporte.</p>
        <button className="mt-6 rounded-md bg-primary px-4 py-2 text-primary-foreground" onClick={reset}>Tentar novamente</button>
      </section>
    </main>
  );
}
