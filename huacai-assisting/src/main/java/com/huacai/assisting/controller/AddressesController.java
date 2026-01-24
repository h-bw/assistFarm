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
import com.huacai.assisting.domain.Addresses;
import com.huacai.assisting.service.IAddressesService;
import com.huacai.common.utils.poi.ExcelUtil;
import com.huacai.common.core.page.TableDataInfo;

/**
 * 收货地址Controller
 *
 * @author huacai
 * @date 2025-08-19
 */
@RestController
@RequestMapping("/assisting/addresses")
public class AddressesController extends BaseController
{
    @Autowired
    private IAddressesService addressesService;

    /**
     * 查询收货地址列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Addresses addresses)
    {
        startPage();
        List<Addresses> list = addressesService.selectAddressesList(addresses);
        return getDataTable(list);
    }

    /**
     * 导出收货地址列表
     */
    @Log(title = "收货地址", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Addresses addresses)
    {
        List<Addresses> list = addressesService.selectAddressesList(addresses);
        ExcelUtil<Addresses> util = new ExcelUtil<Addresses>(Addresses.class);
        util.exportExcel(response, list, "收货地址数据");
    }

    /**
     * 下载模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<Addresses> util = new ExcelUtil<Addresses>(Addresses.class);
        util.importTemplateExcel(response, "收货地址数据");
    }

    /**
     * 导入数据
     */
    @Log(title = "收货地址", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception
    {
        ExcelUtil<Addresses> util = new ExcelUtil<Addresses>(Addresses.class);
        InputStream inputStream = file.getInputStream();
        List<Addresses> list = util.importExcel(inputStream );
        inputStream.close();
        int count = addressesService.batchInsertAddresses(list);
        return AjaxResult.success("导入成功" + count + "条信息！");
    }

    /**
     * 获取收货地址详细信息
     */
    @GetMapping(value = "/{addressesId}")
    public AjaxResult getInfo(@PathVariable("addressesId") String addressesId)
    {
        return success(addressesService.selectAddressesByAddressesId(addressesId));
    }

    /**
     * 新增收货地址
     */
    @Log(title = "收货地址", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Addresses addresses)
    {
        return toAjax(addressesService.insertAddresses(addresses));
    }

    /**
     * 修改收货地址
     */
    @Log(title = "收货地址", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Addresses addresses)
    {
        return toAjax(addressesService.updateAddresses(addresses));
    }

    /**
     * 删除收货地址
     */
    @Log(title = "收货地址", businessType = BusinessType.DELETE)
	@DeleteMapping("/{addressesIds}")
    public AjaxResult remove(@PathVariable String[] addressesIds)
    {
        return toAjax(addressesService.deleteAddressesByAddressesIds(addressesIds));
    }
}
