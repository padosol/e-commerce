// BFF(Next Route Handler)로 로그인 — refresh 는 httpOnly 쿠키로 저장되고 access 만 반환된다.
export async function login(email: string, password: string): Promise<string> {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  if (!res.ok) throw new Error('로그인에 실패했습니다')
  const data = (await res.json()) as { accessToken: string }
  return data.accessToken
}
