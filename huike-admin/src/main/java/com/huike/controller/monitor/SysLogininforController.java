package com.huike.controller.monitor;

import java.util.List;

import com.huike.utils.poi.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import com.huike.common.annotation.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huike.domain.system.SysLogininfor;
import com.huike.service.ISysLogininforService;
import com.huike.common.annotation.Log;
import com.huike.controller.core.BaseController;
import com.huike.domain.common.AjaxResult;
import com.huike.page.TableDataInfo;
import com.huike.common.enums.BusinessType;

/**
 * 系统访问记录
 */
@Api(tags = "系统管理")
@RestController
@RequestMapping("/monitor/logininfor")
public class SysLogininforController extends BaseController {
    @Autowired
    private ISysLogininforService logininforService;

    @ApiOperation("登录日志-列表")
    @PreAuthorize("monitor:logininfor:list")
    @GetMapping("/list")
    public TableDataInfo list(SysLogininfor logininfor) {
        startPage();
        List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
        return getDataTable(list);
    }

    @ApiOperation("登录日志-导出")
    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("monitor:logininfor:export")
    @GetMapping("/export")
    public AjaxResult export(SysLogininfor logininfor) {
        List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
        ExcelUtil<SysLogininfor> util = new ExcelUtil<SysLogininfor>(SysLogininfor.class);
        return util.exportExcel(list, "登录日志");
    }

    @ApiOperation("登录日志-详情")
    @PreAuthorize("monitor:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public AjaxResult remove(@PathVariable Long[] infoIds) {
        return toAjax(logininforService.deleteLogininforByIds(infoIds));
    }

    @ApiOperation("登录日志-清空")
    @PreAuthorize("monitor:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        logininforService.cleanLogininfor();
        return AjaxResult.success();
    }
}
