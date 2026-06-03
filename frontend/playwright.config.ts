import { defineConfig } from '@playwright/test'

// E2E는 백엔드(게이트웨이) 구동 + 브라우저 필요. 실행: npm i -D @playwright/test && npx playwright install && npx playwright test
export default defineConfig({
  testDir: './tests/e2e',
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:3000',
  },
})
