'use client'

import { useEffect, useState } from 'react'
import { fetchProducts, ProductCard, type Product } from '@/entities/product'
import { AddToCartButton } from '@/features/add-to-cart'

export function ProductListPage() {
  const [products, setProducts] = useState<Product[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchProducts()
      .then(setProducts)
      .catch(() => setError('상품을 불러오지 못했습니다.'))
  }, [])

  return (
    <main style={{ padding: 24 }}>
      <h1>상품</h1>
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))',
          gap: 16,
        }}
      >
        {products.map((product) => (
          <ProductCard
            key={product.id}
            product={product}
            action={<AddToCartButton productId={product.id} />}
          />
        ))}
      </div>
    </main>
  )
}
