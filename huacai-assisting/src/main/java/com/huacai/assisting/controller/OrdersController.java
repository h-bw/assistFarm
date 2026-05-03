package com.huacai.assisting.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import com.huacai.assisting.domain.OrdersProducts;
import com.huacai.assisting.domain.Products;
import com.huacai.assisting.service.IProductsService;
import com.huacai.assisting.service.PayService;
import com.huacai.common.core.domain.entity.SysUser;
import com.huacai.common.utils.SecurityUtils;
import com.huacai.system.service.ISysUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
import com.huacai.assisting.domain.Orders;
import com.huacai.assisting.service.IOrdersService;
import com.huacai.common.utils.poi.ExcelUtil;
import com.huacai.common.core.page.TableDataInfo;

/**
 * 订单Controller
 *
 * @author huacai
 * @date 2025-08-19
 */
@RestController
@RequestMapping("/assisting/orders")
public class OrdersController extends BaseController {
    @Autowired
    private IOrdersService ordersService;

    @Autowired
    private PayService payService;

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private IProductsService productsService;

    /**
     * 查询订单列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Orders orders) {
        startPage();
        List<Orders> list = ordersService.selectOrdersList(orders);
        return getDataTable(list);
    }

    /**
     * 导出订单列表
     */
    @Log(title = "订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Orders orders) {
        List<Orders> list = ordersService.selectOrdersList(orders);
        ExcelUtil<Orders> util = new ExcelUtil<Orders>(Orders.class);
        util.exportExcel(response, list, "订单数据");
    }

    /**
     * 下载模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil<Orders> util = new ExcelUtil<Orders>(Orders.class);
        util.importTemplateExcel(response, "订单数据");
    }

    /**
     * 导入数据
     */
    @Log(title = "订单", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception {
        ExcelUtil<Orders> util = new ExcelUtil<Orders>(Orders.class);
        InputStream inputStream = file.getInputStream();
        List<Orders> list = util.importExcel(inputStream);
        inputStream.close();
        int count = ordersService.batchInsertOrders(list);
        return AjaxResult.success("导入成功" + count + "条信息！");
    }

    /**
     * 获取订单详细信息
     */
    @GetMapping(value = "/{ordersId}")
    public AjaxResult getInfo(@PathVariable("ordersId") String ordersId) {
        return success(ordersService.selectOrdersByOrdersId(ordersId));
    }

    /**
     * 新增订单
     */
    @Log(title = "订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Orders orders) {
        String ordersId = ordersService.insertOrders(orders);
        return AjaxResult.success((Object) ordersId);
    }

    /**
     * 修改订单
     */
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Orders orders) {
        return toAjax(ordersService.updateOrders(orders));
    }

    /**
     * 删除订单
     */
    @Log(title = "订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ordersIds}")
    public AjaxResult remove(@PathVariable String[] ordersIds) {
        return toAjax(ordersService.deleteOrdersByOrdersIds(ordersIds));
    }

    /**
     * 支付订单
     */
    @Log(title = "支付订单", businessType = BusinessType.UPDATE)
    @PutMapping("/payment/{ordersIds}")
    @Transactional
    public AjaxResult payment(@PathVariable String[] ordersIds) {
        if (!SecurityUtils.isAdmin(getUserId())) {
            return AjaxResult.error("仅管理员可使用手动支付接口");
        }
        for (String ordersId : ordersIds) {
            payService.markOrderPaid(ordersId);
        }
        return AjaxResult.success("手动支付补单处理成功");
    }

    /**
     * 发货
     */
    @Log(title = "发货", businessType = BusinessType.UPDATE)
    @PutMapping("/sendOutGoods")
    public AjaxResult sendOutGoods(@RequestBody Orders orders) {
        return toAjax(ordersService.sendOutGoods(orders));
    }

}
