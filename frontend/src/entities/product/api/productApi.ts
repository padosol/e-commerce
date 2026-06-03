import { apiFetch } from '@/shared/api'
import type { Product } from '../model/types'

export function fetchProducts(): Promise<Product[]> {
  return apiFetch<Product[]>('/api/products')
}

export function fetchProduct(id: string): Promise<Product> {
  return apiFetch<Product>(`/api/products/${id}`)
}
