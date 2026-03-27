import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    checkoutItems: []
  }),

  actions: {
    setCheckoutItems(items) {
      this.checkoutItems = items
    },
    clearCheckoutItems() {
      this.checkoutItems = []
    }
  }
})
