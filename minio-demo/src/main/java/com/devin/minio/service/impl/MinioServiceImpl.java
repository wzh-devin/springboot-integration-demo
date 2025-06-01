package com.devin.minio.service.impl;

import cn.hutool.core.io.FileUtil;
import com.devin.minio.common.utils.MinioTemplate;
import com.devin.minio.domain.entity.FileInfo;
import com.devin.minio.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

/**
 * 2025/6/1 15:22.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioTemplate minioTemplate;

    @Override
    public String upload(final MultipartFile multipartFile) throws Exception {
        String url;
        try (InputStream inputStream = multipartFile.getInputStream()) {
            String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());

            String key = minioTemplate.generateMd5(multipartFile.getBytes()) + "." + suffix;

            FileInfo fileInfo = new FileInfo();
            fileInfo.setInputStream(inputStream);
            fileInfo.setOriginalFileName(multipartFile.getOriginalFilename());
            fileInfo.setContentType("image/" + suffix);
            fileInfo.setKey(key);
            fileInfo.setSize(multipartFile.getSize());

            url = minioTemplate.uploadFile("testtest", fileInfo);
        } catch (IOException e) {
            throw new Exception(e);
        }
        return url;
    }

    @SneakyThrows
    @Override
    public void createBucket(final String bucketName) {
        minioTemplate.createCustomBucket(bucketName);
    }

    @Override
    public ResponseEntity<byte[]> download(final String key) throws Exception {
        byte[] bytes = minioTemplate.downloadAsBytes("testtest", key);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", key);

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}
