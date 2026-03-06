package com.huacai.assisting.mapper;

import com.huacai.assisting.domain.ItemSimilarity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 商品相似度Mapper接口
 */
@Mapper
public interface ItemSimilarityMapper {

    /**
     * 清空相似度表（或删除全部数据）
     */
    void truncateTable();

    /**
     * 批量插入相似度数据
     */
    void batchInsert(List<ItemSimilarity> list);

    /**
     * 查询与指定商品最相似的N个商品ID
     */
    List<String> selectTopSimilar(@Param("itemId") String itemId, @Param("limit") int limit);
}