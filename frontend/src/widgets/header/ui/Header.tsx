import Link from 'next/link'

export function Header() {
  return (
    <header style={{ borderBottom: '1px solid #eee', padding: '12px 16px' }}>
      <nav style={{ display: 'flex', gap: 16 }}>
        <Link href="/products">상품</Link>
        <Link href="/cart">장바구니</Link>
        <Link href="/login">로그인</Link>
      </nav>
    </header>
  )
}
