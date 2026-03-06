package com.huacai.assisting.mapper;

import java.util.List;
import java.util.Map;
import com.huacai.assisting.domain.Orders;
import com.huacai.assisting.domain.OrdersProducts;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
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

    /**
     * 获取用户-商品交互数据（用于相似度计算）
     * @param status 订单状态（如“已完成”）
     * @return 包含 user_id, products_id, interaction 的 Map 列表
     */
    List<Map<String, Object>> selectUserProductInteractions(@Param("status") String status);

    /**
     * 获取用户最近购买的商品ID（用于推荐）
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 商品ID列表
     */
    List<String> selectRecentProductIdsByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 获取热门商品ID（按总销量排序）
     * @param limit 限制数量
     * @return 商品ID列表
     */
    List<String> selectHotProductIds(@Param("limit") int limit);

}
