import { NextResponse } from 'next/server';

export function GET() {
  return NextResponse.json(
    { status: 'UP', service: 'escala-frontend' },
    { headers: { 'Cache-Control': 'no-store' } }
  );
}
