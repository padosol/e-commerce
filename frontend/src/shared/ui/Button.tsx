'use client'

import type { ButtonHTMLAttributes } from 'react'

export function Button(props: ButtonHTMLAttributes<HTMLButtonElement>) {
  const { children, ...rest } = props
  return (
    <button
      {...rest}
      style={{
        padding: '8px 16px',
        borderRadius: 6,
        border: '1px solid #ccc',
        cursor: 'pointer',
        ...rest.style,
      }}
    >
      {children}
    </button>
  )
}
