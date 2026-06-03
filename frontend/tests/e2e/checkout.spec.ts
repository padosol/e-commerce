import { test, expect } from '@playwright/test'

// 로그인 → 상품 → 장바구니 → 주문 E2E.
// 사전조건: 백엔드 스택 구동(docker compose + 서비스) + 프론트 dev 서버, 시드 상품/계정.
test('로그인 → 상품 탐색 → 장바구니 담기 → 주문', async ({ page }) => {
  await page.goto('/login')
  await page.getByPlaceholder('이메일').fill('user@example.com')
  await page.getByPlaceholder('비밀번호').fill('pw12345!')
  await page.getByRole('button', { name: '로그인' }).click()

  await expect(page).toHaveURL(/\/products/)

  await page.getByRole('button', { name: '장바구니 담기' }).first().click()

  await page.goto('/cart')
  await page.getByRole('button', { name: '주문하기' }).click()

  await expect(page.getByText(/주문 상태/)).toBeVisible()
})
