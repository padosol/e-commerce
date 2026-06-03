export interface OrderLine {
  productId: string
  quantity: number
  unitPrice: number
}

export interface ShippingAddress {
  zipcode: string
  line1: string
  line2?: string | null
}

export interface Order {
  id: string
  userId: string
  status: string
  totalAmount: number
  lines: OrderLine[]
}
