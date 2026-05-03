package com.huacai.assisting.mapper;

import com.huacai.assisting.domain.ItemSimilarity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 商品相似度 Mapper 接口
 */
@Mapper
public interface ItemSimilarityMapper {

    /**
     * 清空相似度表
     */
    void truncateTable();

    /**
     * 批量插入相似度数据
     */
    void batchInsert(List<ItemSimilarity> list);

    /**
     * 查询与指定商品最相似的 N 个商品 ID
     */
    List<String> selectTopSimilar(@Param("itemId") String itemId, @Param("limit") int limit);

    /**
     * 查询与指定商品最相似的商品及相似度分数
     */
    List<Map<String, Object>> selectTopSimilarWithScores(@Param("itemId") String itemId, @Param("limit") int limit);
}
