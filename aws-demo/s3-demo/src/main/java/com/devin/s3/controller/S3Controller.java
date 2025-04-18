package com.devin.s3.controller;

import com.devin.common.utils.ApiResult;
import com.devin.s3.domain.vo.req.BucketInfoReq;
import com.devin.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 2025/4/16 15:57.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@Tag(name = "亚马逊S3服务相关接口")
@RequiredArgsConstructor
@RequestMapping("/aws/s3")
public class S3Controller {
    private final S3Service s3Service;

    /**
     * 创建默认的Bucket.
     * @param bucketInfoReq 存储桶信息 {@linkplain BucketInfoReq}
     * @return Void
     */
    @PutMapping("/createNewBucket")
    @Operation(summary = "创建默认的Bucket")
    @Parameter(name = "bucketInfo", description = "存储桶信息", required = true)
    public ApiResult<Void> createNewBucket(@RequestBody final BucketInfoReq bucketInfoReq) {
        s3Service.createNewDefaultBucket(bucketInfoReq.getName());
        return ApiResult.success();
    }

    /**
     * 创建访问权限ACL的Bucket.
     * @param bucketInfoReq 存储桶信息 {@linkplain BucketInfoReq}
     * @return Void
     */
    @PutMapping("/createNewAclBucket")
    @Operation(summary = "创建ACL访问权限的Bucket")
    @Parameter(name = "bucketInfo", description = "存储桶信息", required = true)
    public ApiResult<Void> createNewAclBucket(@RequestBody final BucketInfoReq bucketInfoReq) {
        s3Service.createNewAclBucket(bucketInfoReq);
        return ApiResult.success();
    }

    /**
     * 删除存储桶.
     * @param bucketName 存储桶名
     * @return Void
     */
    @DeleteMapping("/deleteBucket")
    @Operation(summary = "删除存储桶")
    @Parameter(name = "bucketName", description = "存储桶名", required = true, in = ParameterIn.QUERY)
    public ApiResult<Void> deleteBucket(@RequestParam("bucketName") final String bucketName) {
        s3Service.deleteBucket(bucketName);
        return ApiResult.success();
    }

    /**
     * 获取文件的临时访问地址.
     * @param bucketName 存储桶名
     * @param fileName 文件名
     * @return 文件的临时URL
     */
    @GetMapping("/getFileTempUrl")
    @Operation(summary = "获取文件的临时访问地址")
    @Parameters({
            @Parameter(name = "fileName", description = "文件名", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "bucketName", description = "存储桶名", required = true, in = ParameterIn.QUERY)
    })
    public ApiResult<String> getFileTempUrl(@RequestParam("bucketName") final String bucketName, @RequestParam("fileName") final String fileName) {
        return ApiResult.success(s3Service.getFileTempUrl(bucketName, fileName));
    }

    /**
     * 上传文件.
     * @param bucketName 存储桶名
     * @param file 文件
     * @return 文件的公共访问地址
     */
    @PutMapping(value = "/uploadPublicFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传公共文件，并返回文件的访问地址")
    @Parameters({
            @Parameter(name = "bucketName", description = "存储桶名", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "file", description = "文件", required = true)
    })
    public ApiResult<String> uploadPublicFile(@RequestParam("bucketName") final String bucketName, @RequestParam("file") final MultipartFile file) {
        return ApiResult.success(s3Service.uploadPublicFile(bucketName, file));
    }

    /**
     * 删除文件.
     * @param bucketName 存储桶名
     * @param fileName 文件名
     * @return Void
     */
    @DeleteMapping("/deleteFile")
    @Operation(summary = "删除文件")
    @Parameters({
            @Parameter(name = "fileName", description = "文件名", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "bucketName", description = "存储桶名", required = true, in = ParameterIn.QUERY)
    })
    public ApiResult<Void> deleteFile(@RequestParam("bucketName") final String bucketName, @RequestParam("fileName") final String fileName) {
        s3Service.deleteFile(bucketName, fileName);
        return ApiResult.success();
    }
}
