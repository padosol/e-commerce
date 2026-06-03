import { type NextRequest, NextResponse } from 'next/server'

// BFF: 게이트웨이의 auth-service로 로그인 프록시. refresh 는 httpOnly 쿠키로 저장, access 만 클라이언트에 반환.
const AUTH_BASE_URL =
  process.env.AUTH_API_BASE_URL ?? process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080'

export async function POST(request: NextRequest) {
  const body = await request.json()
  const upstream = await fetch(`${AUTH_BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })

  if (!upstream.ok) {
    return NextResponse.json({ error: 'login failed' }, { status: upstream.status })
  }

  const tokens = (await upstream.json()) as {
    accessToken: string
    refreshToken: string
    expiresInSeconds: number
  }

  const response = NextResponse.json({ accessToken: tokens.accessToken })
  response.cookies.set('refresh_token', tokens.refreshToken, {
    httpOnly: true,
    secure: true,
    sameSite: 'lax',
    path: '/',
  })
  return response
}
