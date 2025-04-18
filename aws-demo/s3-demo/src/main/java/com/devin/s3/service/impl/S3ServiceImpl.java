package com.devin.s3.service.impl;

import com.devin.s3.common.exception.VerificationException;
import com.devin.s3.common.utils.S3Template;
import com.devin.s3.domain.eneity.FileInfo;
import com.devin.s3.domain.vo.req.BucketInfoReq;
import com.devin.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * 2025/4/16 15:59.
 *
 * <p>
 *     S3 服务实现类
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Template s3Template;

    @Override
    public void createNewDefaultBucket(final String bucketName) {
        s3Template.createNewDefaultBucket(bucketName);
    }

    @Override
    public void createNewAclBucket(final BucketInfoReq bucketInfoReq) {
        s3Template.createNewAclBucket(bucketInfoReq.getName(), bucketInfoReq.getAcl());
    }

    @Override
    public void deleteBucket(final String bucketName) {
        s3Template.deleteBucket(bucketName);
    }

    @Override
    public String getFileTempUrl(final String bucketName, final String fileName) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setKey(fileName);
        fileInfo.setExpireTime(Duration.ofMinutes(5));
        return s3Template.generatePresignerUrl(bucketName, fileInfo);
    }

    @Override
    public String uploadPublicFile(final String bucketName, final MultipartFile file) {
        // 获取文件信息
        try (InputStream is = file.getInputStream()) {
            // 获取文件对象
            FileInfo fileInfo = generateFileInfo(file);
            return s3Template.uploadPublicFile(bucketName, fileInfo);
        } catch (IOException e) {
            log.error("上传文件失败: {}", e.getMessage());
        }
        return "";
    }

    @Override
    public void deleteFile(final String bucketName, final String fileName) {
        s3Template.deleteFile(bucketName, fileName);
    }

    /**
     * 生成文件信息.
     * @param file 文件
     * @return FileInfo
     */
    private FileInfo generateFileInfo(final MultipartFile file) throws IOException {
        // 获取文件信息
        String originalFilename = file.getOriginalFilename();
        if (Objects.isNull(originalFilename)) {
            throw new VerificationException("文件名不能为空...");
        }
        String[] fileArr = originalFilename.split("\\.");
        String prefix = fileArr[0];
        String suffix = fileArr[fileArr.length - 1];

        // UUID生成文件名
        String uuid = UUID.randomUUID().toString();
        String key = prefix + "_" + uuid + "." + suffix;

        // 生成文件信息
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOriginalFileName(originalFilename);
        fileInfo.setFileBytes(file.getBytes());
        fileInfo.setPrefix(prefix);
        fileInfo.setSuffix(suffix);
        fileInfo.setKey(key);
        fileInfo.setContentType(file.getContentType());
        fileInfo.setSize(file.getSize());

        return fileInfo;
    }
}
