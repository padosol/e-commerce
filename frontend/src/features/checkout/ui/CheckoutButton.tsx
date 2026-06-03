'use client'

import { useState } from 'react'
import { placeOrder, type OrderLine } from '@/entities/order'
import { Button } from '@/shared/ui'

export function CheckoutButton({ lines }: { lines: OrderLine[] }) {
  const [status, setStatus] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  async function onClick() {
    setPending(true)
    try {
      const order = await placeOrder(lines, { zipcode: '12345', line1: '서울특별시 1로', line2: '201호' })
      setStatus(order.status)
    } finally {
      setPending(false)
    }
  }

  return (
    <div>
      <Button onClick={onClick} disabled={pending || lines.length === 0}>
        주문하기
      </Button>
      {status && <p>주문 상태: {status}</p>}
    </div>
  )
}
