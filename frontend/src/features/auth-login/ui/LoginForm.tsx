'use client'

import { type FormEvent, useState } from 'react'
import { useRouter } from 'next/navigation'
import { tokenStore } from '@/shared/auth'
import { Button } from '@/shared/ui'
import { login } from '../api/loginApi'

export function LoginForm() {
  const router = useRouter()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    try {
      const accessToken = await login(email, password)
      tokenStore.set(accessToken)
      router.push('/products')
    } catch {
      setError('이메일 또는 비밀번호를 확인해 주세요.')
    }
  }

  return (
    <form onSubmit={onSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 8, maxWidth: 320 }}>
      <input
        type="email"
        placeholder="이메일"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        required
      />
      <input
        type="password"
        placeholder="비밀번호"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        required
      />
      <Button type="submit">로그인</Button>
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
    </form>
  )
}
