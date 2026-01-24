package com.huacai.assisting.mapper;

import java.util.List;
import com.huacai.assisting.domain.Orders;
import com.huacai.assisting.domain.OrdersProducts;

/**
 * 订单Mapper接口
 * 
 * @author huacai
 * @date 2025-08-19
 */
public interface OrdersMapper 
{
    /**
     * 查询订单
     * 
     * @param ordersId 订单主键
     * @return 订单
     */
    public Orders selectOrdersByOrdersId(String ordersId);

    /**
     * 查询订单列表
     * 
     * @param orders 订单
     * @return 订单集合
     */
    public List<Orders> selectOrdersList(Orders orders);

    /**
     * 新增订单
     * 
     * @param orders 订单
     * @return 结果
     */
    public int insertOrders(Orders orders);

    /**
     * 修改订单
     * 
     * @param orders 订单
     * @return 结果
     */
    public int updateOrders(Orders orders);

    /**
     * 删除订单
     * 
     * @param ordersId 订单主键
     * @return 结果
     */
    public int deleteOrdersByOrdersId(String ordersId);

    /**
     * 批量删除订单
     * 
     * @param ordersIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOrdersByOrdersIds(String[] ordersIds);

    /**
     * 批量删除订单产品
     * 
     * @param ordersIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOrdersProductsByOrdersIds(String[] ordersIds);
    
    /**
     * 批量新增订单产品
     * 
     * @param ordersProductsList 订单产品列表
     * @return 结果
     */
    public int batchOrdersProducts(List<OrdersProducts> ordersProductsList);
    

    /**
     * 通过订单主键删除订单产品信息
     * 
     * @param ordersId 订单ID
     * @return 结果
     */
    public int deleteOrdersProductsByOrdersId(String ordersId);
}
