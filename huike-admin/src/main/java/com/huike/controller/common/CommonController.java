package com.huike.controller.common;

import com.huike.common.config.HuiKeConfig;
import com.huike.common.config.MinioConfig;
import com.huike.domain.common.AjaxResult;
import com.huike.utils.MinioUtils;
import com.huike.utils.StringUtils;
import com.huike.utils.file.FileUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.huike.controller.core.BaseController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 通用请求处理
 */
@Slf4j
@Api(tags ="通用服务")
@RestController
public class CommonController extends BaseController {


    /**
     * 通用下载请求
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    @ApiOperation("文件-下载")
    @GetMapping("/common/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request) {
        try {
            if (!FileUtils.checkAllowDownload(fileName)) {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = HuiKeConfig.getDownloadPath() + fileName;
            log.info("下载路径 {}", filePath);

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete) {
                FileUtils.deleteFile(filePath);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }


    @Autowired
    private MinioUtils minioUtils;
    @Autowired
    private MinioConfig minioConfig;

    /**
     * 文件上传
     */
    @ApiOperation("文件上传")
    @PostMapping("/common/upload")
    public AjaxResult upload(MultipartFile file) throws Exception {
        log.info("进行文件上传 , {}" , file.getOriginalFilename());
        String fileName = minioUtils.upload(file);

        AjaxResult ajaxResult = AjaxResult.success();

        String url = "http://" + minioConfig.getEndpoint() + ":" + minioConfig.getPort() + fileName;

        ajaxResult.put("url", url);
        ajaxResult.put("fileName", fileName);

        return ajaxResult;
    }



}
