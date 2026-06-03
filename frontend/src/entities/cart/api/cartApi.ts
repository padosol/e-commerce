import { apiFetch } from '@/shared/api'
import type { Cart } from '../model/types'

export function fetchCart(): Promise<Cart> {
  return apiFetch<Cart>('/api/carts')
}

export function addCartItem(productId: string, quantity: number): Promise<Cart> {
  return apiFetch<Cart>('/api/carts/items', {
    method: 'POST',
    body: JSON.stringify({ productId, quantity }),
  })
}
