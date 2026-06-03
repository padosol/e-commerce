import { defineConfig } from 'steiger'
import fsd from '@feature-sliced/steiger-plugin'

// FSD 레이어/경계 린트: app → views(=pages) → widgets → features → entities → shared (하향 의존만)
// NOTE: Next.js App Router 충돌로 FSD 'pages' 레이어를 'views'로 명명했다. steiger가 'views'를
//       통한 교차참조를 추적하지 못해 insignificant-slice 오탐이 발생하므로 해당 규칙만 비활성화한다.
//       (forbidden-imports/public-api 등 핵심 경계 규칙은 유지)
export default defineConfig([
  ...fsd.configs.recommended,
  {
    rules: {
      'fsd/insignificant-slice': 'off',
    },
  },
])
