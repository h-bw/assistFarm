/**
 * 购物车状态管理模块
 * 使用Pinia 管理购物车的选中产品和结算状态等
 */
import { defineStore } from 'pinia'

/**
 * 定义并导出购物车 Store
 */
export const useCartStore = defineStore('cart', {
    /**
     * 状态定义
     */
    state: () => ({
        checkoutItems: [] //初始化为空数组, 用于存放用户选中的待结算产品
    }),

    /**
     * 动作方法
     * @description 定义修改状态的操作方法
     */
    actions: {
        /**
         * 设置待结算产品
         * @param items - 要结算的商品数组
         * @description 更新 checkoutItems 状态, 用户在用户点击'去结算'按钮时调用
         */
        setCheckoutItems(items) {
            //直接替换原有的结算产品列表
            this.checkoutItems = items
        }
    }
})
