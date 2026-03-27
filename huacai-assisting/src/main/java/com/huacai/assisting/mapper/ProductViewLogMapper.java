package com.huacai.assisting.mapper;

import com.huacai.assisting.domain.ProductViewLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductViewLogMapper {

    int upsertProductViewLog(ProductViewLog productViewLog);

    List<String> selectRecentProductIdsByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
}
