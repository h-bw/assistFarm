package com.huacai.assisting.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.huacai.common.annotation.Log;
import com.huacai.common.core.controller.BaseController;
import com.huacai.common.core.domain.AjaxResult;
import com.huacai.common.enums.BusinessType;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;
import com.huacai.assisting.domain.Policies;
import com.huacai.assisting.service.IPoliciesService;
import com.huacai.common.utils.poi.ExcelUtil;
import com.huacai.common.core.page.TableDataInfo;

/**
 * 助农政策Controller
 *
 * @author huacai
 * @date 2025-08-23
 */
@RestController
@RequestMapping("/assisting/policies")
public class PoliciesController extends BaseController
{
    @Autowired
    private IPoliciesService policiesService;

    /**
     * 查询助农政策列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Policies policies)
    {
        startPage();
        List<Policies> list = policiesService.selectPoliciesList(policies);
        return getDataTable(list);
    }

    /**
     * 导出助农政策列表
     */
    @Log(title = "助农政策", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Policies policies)
    {
        List<Policies> list = policiesService.selectPoliciesList(policies);
        ExcelUtil<Policies> util = new ExcelUtil<Policies>(Policies.class);
        util.exportExcel(response, list, "助农政策数据");
    }

    /**
     * 下载模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<Policies> util = new ExcelUtil<Policies>(Policies.class);
        util.importTemplateExcel(response, "助农政策数据");
    }

    /**
     * 导入数据
     */
    @Log(title = "助农政策", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception
    {
        ExcelUtil<Policies> util = new ExcelUtil<Policies>(Policies.class);
        InputStream inputStream = file.getInputStream();
        List<Policies> list = util.importExcel(inputStream );
        inputStream.close();
        int count = policiesService.batchInsertPolicies(list);
        return AjaxResult.success("导入成功" + count + "条信息！");
    }

    /**
     * 获取助农政策详细信息
     */
    @GetMapping(value = "/{policiesId}")
    public AjaxResult getInfo(@PathVariable("policiesId") String policiesId)
    {
        return success(policiesService.selectPoliciesByPoliciesId(policiesId));
    }

    /**
     * 新增助农政策
     */
    @Log(title = "助农政策", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Policies policies)
    {
        return toAjax(policiesService.insertPolicies(policies));
    }

    /**
     * 修改助农政策
     */
    @Log(title = "助农政策", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Policies policies)
    {
        return toAjax(policiesService.updatePolicies(policies));
    }

    /**
     * 删除助农政策
     */
    @Log(title = "助农政策", businessType = BusinessType.DELETE)
	@DeleteMapping("/{policiesIds}")
    public AjaxResult remove(@PathVariable String[] policiesIds)
    {
        return toAjax(policiesService.deletePoliciesByPoliciesIds(policiesIds));
    }
}
