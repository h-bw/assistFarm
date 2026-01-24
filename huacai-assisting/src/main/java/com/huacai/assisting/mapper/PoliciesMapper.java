package com.huacai.assisting.mapper;

import java.util.List;
import com.huacai.assisting.domain.Policies;

/**
 * 助农政策Mapper接口
 * 
 * @author huacai
 * @date 2025-08-23
 */
public interface PoliciesMapper 
{
    /**
     * 查询助农政策
     * 
     * @param policiesId 助农政策主键
     * @return 助农政策
     */
    public Policies selectPoliciesByPoliciesId(String policiesId);

    /**
     * 查询助农政策列表
     * 
     * @param policies 助农政策
     * @return 助农政策集合
     */
    public List<Policies> selectPoliciesList(Policies policies);

    /**
     * 新增助农政策
     * 
     * @param policies 助农政策
     * @return 结果
     */
    public int insertPolicies(Policies policies);

    /**
     * 修改助农政策
     * 
     * @param policies 助农政策
     * @return 结果
     */
    public int updatePolicies(Policies policies);

    /**
     * 删除助农政策
     * 
     * @param policiesId 助农政策主键
     * @return 结果
     */
    public int deletePoliciesByPoliciesId(String policiesId);

    /**
     * 批量删除助农政策
     * 
     * @param policiesIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePoliciesByPoliciesIds(String[] policiesIds);
}
