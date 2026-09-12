'use client';

export default function PrivateAreaError({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <section className="mx-auto mt-16 max-w-lg rounded-lg border p-6 text-center" role="alert">
      <h2 className="text-xl font-semibold">Esta area nao pode ser carregada</h2>
      <p className="mt-2 text-muted-foreground">As demais areas continuam disponiveis. Tente novamente ou informe o codigo {error.digest ?? 'indisponivel'} ao suporte.</p>
      <button className="mt-5 rounded-md bg-primary px-4 py-2 text-primary-foreground" onClick={reset}>Tentar novamente</button>
    </section>
  );
}
