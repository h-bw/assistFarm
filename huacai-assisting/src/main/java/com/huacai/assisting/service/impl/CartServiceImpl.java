package com.huacai.assisting.service.impl;

import java.util.List;

import com.huacai.common.utils.DateUtils;
import com.huacai.common.utils.uuid.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.huacai.assisting.mapper.CartMapper;
import com.huacai.assisting.domain.Cart;
import com.huacai.assisting.service.ICartService;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import static com.huacai.common.utils.SecurityUtils.getUserId;

/**
 * 购物车Service业务层处理
 *
 * @author huacai
 * @date 2025-08-15
 */
@Service
public class CartServiceImpl implements ICartService {
    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 查询购物车
     *
     * @param cartId 购物车主键
     * @return 购物车
     */
    @Override
    public Cart selectCartByCartId(String cartId) {
        return cartMapper.selectCartByCartId(cartId);
    }

    /**
     * 查询购物车列表
     *
     * @param cart 购物车
     * @return 购物车
     */
    @Override
    public List<Cart> selectCartList(Cart cart) {
        return cartMapper.selectCartList(cart);
    }

    /**
     * 新增购物车
     *
     * @param cart 购物车
     * @return 结果
     */
    @Override
    @Transactional
    public int insertCart(Cart cart) {
        cart.setCreateTime(DateUtils.getNowDate());
        //生成一个新的UUID
        String uuid = IdUtils.fastSimpleUUID();
        //插入主键ID
        cart.setCartId(uuid);
        //获取当前登录用户的ID
        Long loginUserId = getUserId();
        //插入用户ID
        cart.setUserId(loginUserId);

        //根据产品ID和用户ID查询购物车项ID
        String cartId = cartMapper.selectCartIdByProductsAndUserId(cart.getProductsId(), loginUserId);

        //如果cartId不存在, 说明当前用户的购物车中没有这个产品, 需要新增一条购物车项
        //如果cartId存在, 说明当前用户的购物车中有这个产品, 只需要在当前项的数量 +1
        if (cartId == null) {
            return cartMapper.insertCart(cart);
        } else {
            //根据cartId查询购物车项信息
            Cart oldCart = cartMapper.selectCartByCartId(cartId);
            oldCart.setCartId(cartId);
            oldCart.setQuantity(oldCart.getQuantity() + cart.getQuantity());
            return cartMapper.updateCart(oldCart);
        }
    }

    /**
     * 批量新增购物车
     *
     * @param carts 购物车List
     * @return 结果
     */
    @Override
    public int batchInsertCart(List<Cart> carts) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        int count = 0;
        if (!CollectionUtils.isEmpty(carts)) {
            try {
                for (int i = 0; i < carts.size(); i++) {
                    int row = cartMapper.insertCart(carts.get(i));
                    // 防止内存溢出，每100次提交一次,并清除缓存
                    boolean bool = (i > 0 && i % 100 == 0) || i == carts.size() - 1;
                    if (bool) {
                        sqlSession.commit();
                        sqlSession.clearCache();
                    }
                    count = i + 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 没有提交的数据可以回滚
                sqlSession.rollback();
            } finally {
                sqlSession.close();
                return count;
            }
        }
        return count;
    }

    /**
     * 修改购物车
     *
     * @param cart 购物车
     * @return 结果
     */
    @Override
    public int updateCart(Cart cart) {
        return cartMapper.updateCart(cart);
    }

    /**
     * 批量删除购物车
     *
     * @param cartIds 需要删除的购物车主键
     * @return 结果
     */
    @Override
    public int deleteCartByCartIds(String[] cartIds) {
        return cartMapper.deleteCartByCartIds(cartIds);
    }

    /**
     * 删除购物车信息
     *
     * @param cartId 购物车主键
     * @return 结果
     */
    @Override
    public int deleteCartByCartId(String cartId) {
        return cartMapper.deleteCartByCartId(cartId);
    }
}
