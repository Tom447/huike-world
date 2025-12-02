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
import com.huike.domain.system.SysOperLog;
import com.huike.service.ISysOperLogService;
import com.huike.common.annotation.Log;
import com.huike.controller.core.BaseController;
import com.huike.domain.common.AjaxResult;
import com.huike.page.TableDataInfo;
import com.huike.common.enums.BusinessType;

/**
 * 操作日志记录
 */
@Api(tags = "系统管理")
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperlogController extends BaseController {
    @Autowired
    private ISysOperLogService operLogService;


    @ApiOperation("操作日志-列表查询")
    @PreAuthorize("monitor:operlog:list")
    @GetMapping("/list")
    public TableDataInfo list(SysOperLog operLog) {
        startPage();
        List<SysOperLog> list = operLogService.selectOperLogList(operLog);
        return getDataTable(list);
    }

    @ApiOperation("操作日志-导出")
    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("monitor:operlog:export")
    @GetMapping("/export")
    public AjaxResult export(SysOperLog operLog) {
        List<SysOperLog> list = operLogService.selectOperLogList(operLog);
        ExcelUtil<SysOperLog> util = new ExcelUtil<SysOperLog>(SysOperLog.class);
        return util.exportExcel(list, "操作日志");
    }

    @ApiOperation("操作日志-查看")
    @PreAuthorize("monitor:operlog:remove")
    @DeleteMapping("/{operIds}")
    public AjaxResult remove(@PathVariable Long[] operIds) {
        return toAjax(operLogService.deleteOperLogByIds(operIds));
    }

    @ApiOperation("操作日志-清空")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("monitor:operlog:remove")
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        operLogService.cleanOperLog();
        return AjaxResult.success();
    }
}
