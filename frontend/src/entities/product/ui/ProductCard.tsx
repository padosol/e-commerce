import type { ReactNode } from 'react'
import type { Product } from '../model/types'

export function ProductCard({ product, action }: { product: Product; action?: ReactNode }) {
  return (
    <div style={{ border: '1px solid #eee', borderRadius: 8, padding: 16 }}>
      <h3 style={{ margin: '0 0 8px' }}>{product.name}</h3>
      <p style={{ color: '#666', margin: '0 0 8px' }}>{product.description}</p>
      <p style={{ fontWeight: 600 }}>
        {product.amount.toLocaleString()} {product.currency}
      </p>
      {action}
    </div>
  )
}
