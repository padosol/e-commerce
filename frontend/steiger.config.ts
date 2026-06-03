import { defineConfig } from 'steiger'
import fsd from '@feature-sliced/steiger-plugin'

// FSD 레이어/경계 린트: app → pages → widgets → features → entities → shared (하향 의존만 허용)
export default defineConfig([
  ...fsd.configs.recommended,
])
