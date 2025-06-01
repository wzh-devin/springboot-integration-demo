package com.devin.minio.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * 2025/6/1 15:22.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
public interface MinioService {

    /**
     * 文件上传.
     * @param multipartFile 文件
     * @return URL
     */
    String upload(MultipartFile multipartFile) throws Exception;

    /**
     * 创建存储桶.
     * @param bucketName 存储桶名
     */
    void createBucket(String bucketName);

    /**
     * 文件下载.
     * @param key 文件名
     * @return  ResponseEntity
     */
    ResponseEntity<byte[]> download(String key) throws Exception;
}
