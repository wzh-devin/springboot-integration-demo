package com.devin.minio.controller;

import com.devin.common.utils.ApiResult;
import com.devin.minio.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
@Slf4j
@RestController
@Tag(name = "Minio存储相关接口")
@RequestMapping("/minio")
@RequiredArgsConstructor
public class MinioController {

    private final MinioService minioService;

    /**
     * 文件上传.
     * @param multipartFile 文件
     * @return URL
     */
    @PutMapping("/upload")
    @Operation(summary = "上传文件")
    @Parameter(name = "multipartFile", description = "上传文件信息", required = true)
    public ApiResult<String> upload(final MultipartFile multipartFile) throws Exception {
        return ApiResult.success(minioService.upload(multipartFile));
    }

    /**
     * 文件下载.
     *
     * @param key 文件名称
     * @return ResponseEntity响应信息
     */
    @GetMapping("/download")
    @Operation(summary = "文件下载")
    @Parameter(name = "key", description = "文件名", required = true)
    public ResponseEntity<byte[]> download(final String key) throws Exception {
        return minioService.download(key);
    }

    /**
     * 创建存储桶.
     *
     * @param bucketName 存储桶名
     * @return Void
     */
    @PutMapping("/createBucket")
    @Operation(summary = "创建存储桶")
    @Parameter(name = "bucketName", description = "存储桶名", required = true)
    public ApiResult<Void> createBucket(final String bucketName) {
        minioService.createBucket(bucketName);
        return ApiResult.success();
    }
}
