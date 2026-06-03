'use client'

import { useState } from 'react'
import { addCartItem } from '@/entities/cart'
import { Button } from '@/shared/ui'

export function AddToCartButton({ productId }: { productId: string }) {
  const [done, setDone] = useState(false)
  const [pending, setPending] = useState(false)

  async function onClick() {
    setPending(true)
    try {
      await addCartItem(productId, 1)
      setDone(true)
    } finally {
      setPending(false)
    }
  }

  return (
    <Button onClick={onClick} disabled={pending}>
      {done ? '담김 ✓' : '장바구니 담기'}
    </Button>
  )
}
