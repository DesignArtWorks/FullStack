import { PayrollItem } from '@/core/domain/models/payroll.model';

/** Relatorio de folha consumido pelo navegador, sempre via BFF. */
export class PayrollReportClientAdapter {
  private static readonly baseUrl = '/api/bff/reports/payroll';

  /** Devolve null quando o BFF responde com erro, para a tela manter o estado anterior. */
  static async fetchReport(month: string): Promise<PayrollItem[] | null> {
    const response = await fetch(`${this.baseUrl}?month=${month}`);
    if (!response.ok) return null;
    return (await response.json()) as PayrollItem[];
  }

  static exportUrl(month: string): string {
    return `${this.baseUrl}/export?month=${month}`;
  }
}
