import { apiFetch } from '@/shared/api'
import type { Order, OrderLine, ShippingAddress } from '../model/types'

export function placeOrder(lines: OrderLine[], shippingAddress: ShippingAddress): Promise<Order> {
  return apiFetch<Order>('/api/orders', {
    method: 'POST',
    body: JSON.stringify({ lines, shippingAddress }),
  })
}

export function fetchOrder(id: string): Promise<Order> {
  return apiFetch<Order>(`/api/orders/${id}`)
}
