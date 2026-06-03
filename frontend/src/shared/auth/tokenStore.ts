// access 토큰은 메모리에만 보관(XSS 노출 최소화). refresh 는 httpOnly 쿠키(BFF).
let accessToken: string | null = null

export const tokenStore = {
  get(): string | null {
    return accessToken
  },
  set(token: string | null): void {
    accessToken = token
  },
}
