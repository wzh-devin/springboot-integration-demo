package com.devin.s3.service;

import com.devin.s3.domain.vo.req.BucketInfoReq;
import org.springframework.web.multipart.MultipartFile;

/**
 * 2025/4/16 15:59.
 *
 * <p>
 *     S3服务类
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
public interface S3Service {

    /**
     * 创建默认Bucket.
     * @param bucketName Bucket名
     */
    void createNewDefaultBucket(String bucketName);

    /**
     * 创建指定访问权限的Bucket.
     * @param bucketInfoReq Bucket信息 {@linkplain BucketInfoReq}
     */
    void createNewAclBucket(BucketInfoReq bucketInfoReq);

    /**
     * 删除Bucket.
     * @param bucketName 存储桶名
     */
    void deleteBucket(String bucketName);

    /**
     * 获取文件的临时访问地址.
     * @param bucketName 存储桶名
     * @param fileName 文件名
     * @return URL
     */
    String getFileTempUrl(String bucketName, String fileName);

    /**
     * 上传公共文件.
     * @param bucketName 存储桶名
     * @param file 文件
     * @return URL
     */
    String uploadPublicFile(String bucketName, MultipartFile file);

    /**
     * 删除文件.
     * @param bucketName 存储桶名
     * @param fileName 文件名
     */
    void deleteFile(String bucketName, String fileName);
}
