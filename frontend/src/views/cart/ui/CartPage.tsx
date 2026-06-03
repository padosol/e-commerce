'use client'

import { useEffect, useState } from 'react'
import { fetchCart, type Cart } from '@/entities/cart'
import { type OrderLine } from '@/entities/order'
import { CheckoutButton } from '@/features/checkout'

export function CartPage() {
  const [cart, setCart] = useState<Cart | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchCart()
      .then(setCart)
      .catch(() => setError('장바구니를 불러오지 못했습니다.'))
  }, [])

  // NOTE: 데모 단순화 — 단가는 결제 시 product/cart에서 해소해야 한다(여기선 0 placeholder).
  const lines: OrderLine[] = (cart?.items ?? []).map((item) => ({
    productId: item.productId,
    quantity: item.quantity,
    unitPrice: 0,
  }))

  return (
    <main style={{ padding: 24 }}>
      <h1>장바구니</h1>
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
      <ul>
        {(cart?.items ?? []).map((item) => (
          <li key={item.productId}>
            {item.productId} × {item.quantity}
          </li>
        ))}
      </ul>
      <CheckoutButton lines={lines} />
    </main>
  )
}
