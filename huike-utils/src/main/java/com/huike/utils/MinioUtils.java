package com.huike.utils;

import com.huike.common.config.MinioConfig;
import com.huike.utils.uuid.UUID;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class MinioUtils {

    private MinioConfig minioConfig;

    public MinioUtils(MinioConfig minioConfig) {
        this.minioConfig = minioConfig;
    }

    /**
     * 文件上传的方法 - 将文件传到minio中
     *
     * @param file
     */
    public String upload(MultipartFile file) throws Exception {
        // 创建minioClient客户端对象
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint("http://" + minioConfig.getEndpoint() + ":" + minioConfig.getPort())
                        .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                        .build();

        // 准备Bucket存储空间
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConfig.getBucketName()).build());
        }

        //构建对象的名字, 避免对象名重复
        String originalFilename = file.getOriginalFilename();
        String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = UUID.randomUUID().toString() + extName;

        String folderName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/"));
        objectName = folderName + objectName;

        //上传文件到指定bucket - 已知大小的流
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());

        return "/" +minioConfig.getBucketName() + "/" + objectName;
    }

}
