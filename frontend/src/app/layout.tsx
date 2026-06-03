import type { ReactNode } from 'react'
import { Header } from '@/widgets/header'

export const metadata = {
  title: 'ecommerce',
  description: 'ecommerce MSA demo (Next.js FSD)',
}

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="ko">
      <body style={{ fontFamily: 'system-ui, sans-serif', margin: 0 }}>
        <Header />
        {children}
      </body>
    </html>
  )
}
