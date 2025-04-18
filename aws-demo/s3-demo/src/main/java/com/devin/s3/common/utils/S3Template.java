package com.devin.s3.common.utils;

import com.devin.s3.common.exception.VerificationException;
import com.devin.s3.domain.eneity.FileInfo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectOwnership;
import software.amazon.awssdk.services.s3.model.OwnershipControls;
import software.amazon.awssdk.services.s3.model.OwnershipControlsRule;
import software.amazon.awssdk.services.s3.model.PublicAccessBlockConfiguration;
import software.amazon.awssdk.services.s3.model.PutBucketAclRequest;
import software.amazon.awssdk.services.s3.model.PutBucketOwnershipControlsRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutPublicAccessBlockRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * 2025/4/17 20:20.
 *
 * <p>
 *     S3工具类模板<br>
 *     这里封装的是对S3客户端操作的模板工具<br>
 *     因为该类需要依赖于注入的：{@linkplain software.amazon.awssdk.services.s3.S3Client} 和 {@linkplain software.amazon.awssdk.services.s3.presigner.S3Presigner}<br>
 *     所以，如果你有需要，请阅读逻辑后自行定义工具类
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@Component
@RequiredArgsConstructor
public class S3Template {

    private static final String PUBLIC_READ_URL = "https://%s.s3.%s.amazonaws.com/%s";

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.region}")
    private String region;

    /**
     * 创建新的默认存储桶.
     * @param bucketName 存储桶名
     */
    public void createNewDefaultBucket(final String bucketName) {
        if (hasBucket(bucketName)) {
            throw new VerificationException("该存储桶已存在，请重新添加...");
        }

        // 创建存储桶的配置信息
        CreateBucketConfiguration createBucketConfiguration = CreateBucketConfiguration.builder()
                .locationConstraint(region)
                .build();

        // 添加存储桶
        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .createBucketConfiguration(createBucketConfiguration)
                .build();

        s3Client.createBucket(createBucketRequest);
    }

    /**
     * 开启存储桶的ACL权限.
     *
     * <p>
     *     如果想要
     *     <u>以永久访问地址作为URL访问</u>, 则存储桶的ACL权限必须开启<br/>
     *     并且前提需要依次开启
     *     <li>
     *         存储桶所有者规则 onOwnerShipRuler
     *     </li>
     *     <li>
     *         开启存储桶的访问控制列表 onPublicAccessBlock
     *     </li>
     * </p>
     * @param bucketName 存储桶名
     * @param acl 存储桶的ACL权限 {@linkplain software.amazon.awssdk.services.s3.model.BucketCannedACL}
     */
    public void createNewAclBucket(final String bucketName, final String acl) {
        // 创建默认存储桶
        createNewDefaultBucket(bucketName);

        try {
            // 设置存储桶所有者规则
            onOwnerShipRuler(bucketName);

            // 开启公共列表权限
            onPublicAccessBlock(bucketName);

            // 修改存储桶策略，支持ACL
            PutBucketAclRequest putBucketAclRequest = PutBucketAclRequest.builder()
                    .bucket(bucketName)
                    .acl(acl)
                    .build();

            s3Client.putBucketAcl(putBucketAclRequest);
        } catch (Exception e) {
            // 修改存储桶过程中出现异常，需要删除存储桶
            if (hasBucket(bucketName)) {
                deleteBucket(bucketName);
            }
        }

    }

    /**
     * 获取所有桶名的列表.
     * @return List
     */
    public List<String> getAllBucketList() {
        return s3Client.listBuckets().buckets().stream()
                .map(Bucket::name)
                .toList();
    }

    /**
     * 判断存储桶是否存在.
     * @param bucketName 存储桶名
     * @return Boolean
     */
    public Boolean hasBucket(final String bucketName) {
        return s3Client.listBuckets().buckets()
                .stream()
                .anyMatch(bucket -> bucket.name().equals(bucketName));
    }

    /**
     * 上传具权限的文件.
     * @param bucketName 存储桶名
     * @param fileInfo 文件信息
     * @return 上传后的文件URL地址
     */
    public String uploadAclFile(final String bucketName, final FileInfo fileInfo) {
        if (!hasBucket(bucketName)) {
            throw new VerificationException("存储桶不存在，请重新输入桶名...");
        }

        // 创建上传请求
        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(bucketName);

        // 判断请求的ACL权限
        if (Objects.nonNull(fileInfo.getAcl())) {
            builder.acl(fileInfo.getAcl());
        }

        PutObjectRequest putObjectRequest = builder
                .contentType(fileInfo.getContentType())
                .key(fileInfo.getKey())
                .build();

        try {
            // 上传文件
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileInfo.getFileBytes()));

            // 判断文件权限类型
            if (ObjectCannedACL.PUBLIC_READ.toString().equals(fileInfo.getAcl())) {
                // 返回公共地址
                return generatePublicReadUrl(bucketName, fileInfo.getKey());
            } else {
                // 返回预签名请求URL地址
                return generatePresignerUrl(bucketName, fileInfo);
            }
        } catch (Exception e) {
            // TODO 如果出现异常，将上传文件进行删除
        }

        return "";
    }

    /**
     * 上传默认权限文件.
     * @param bucketName 存储桶名
     * @param fileInfo 文件信息
     * @return 预签名请求URL
     */
    public String uploadDefaultFile(final String bucketName, final FileInfo fileInfo) {
        // 置空权限
        fileInfo.setAcl(null);

        // 上传文件
        return uploadAclFile(bucketName, fileInfo);
    }

    /**
     * 下载文件.
     * @param bucketName 存储桶名
     * @param key 文件名称
     * @return InputStream 输入流
     */
    public InputStream downloadFile(final String bucketName, final String key) {
        if (!hasBucket(bucketName)) {
            throw new VerificationException("存储桶不存在，请重新输入桶名...");
        }

        // 创建下载请求
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.getObject(getObjectRequest, ResponseTransformer.toInputStream());
    }

    /**
     * 上传公共的文件.
     * @param bucketName 存储桶名
     * @param fileInfo 文件信息
     * @return URL
     */
    public String uploadPublicFile(final String bucketName, final FileInfo fileInfo) {
        if (!hasBucket(bucketName)) {
            throw new VerificationException("存储桶不存在，请重新输入桶名...");
        }

        // 设置公共读权限
        fileInfo.setAcl(ObjectCannedACL.PUBLIC_READ.toString());

        return uploadAclFile(bucketName, fileInfo);
    }

    /**
     * 生成公共访问地址.
     * @param bucketName 存储桶名
     * @param key 文件名
     * @return URL
     */
    public String generatePublicReadUrl(final String bucketName, final String key) {
        if (!hasBucket(bucketName)) {
            throw new VerificationException("存储桶不存在，请重新确认桶名...");
        }
        return String.format(PUBLIC_READ_URL, bucketName, region, key);
    }

    /**
     * 生成预签名临时URL.
     * @param bucketName 存储桶名
     * @param fileInfo 文件信息
     * @return 临时访问的URL地址
     */
    public String generatePresignerUrl(final String bucketName, final FileInfo fileInfo) {
        if (!hasBucket(bucketName)) {
            throw new VerificationException("存储桶不存在，请重新输入桶名...");
        }
        // 创建请求对象
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileInfo.getKey())
                .build();

        // 创建预签名请求对象
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(fileInfo.getExpireTime())
                .getObjectRequest(getObjectRequest)
                .build();

        // 生成预签名临时URL
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * 删除存储桶.
     * @param bucketName 存储桶名
     */
    public void deleteBucket(final String bucketName) {
        if (!hasBucket(bucketName)) {
            return;
        }

        // 创建删除请求
        DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                .bucket(bucketName)
                .build();

        // 删除存储桶
        s3Client.deleteBucket(deleteBucketRequest);
    }

    /**
     * 删除文件.
     *
     * <p>
     *     TODO 后续需要通过MD5校验文件是否存在，进行删除前置判断
     * </p>
     * @param bucketName 存储桶名
     * @param key 文件名
     */
    public void deleteFile(final String bucketName, final String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    /**
     * 开启存储桶的访问控制列表.
     *
     * <p>
     *     默认公共权限是被关闭<br/>
     *     如果需要开启桶的公共访问权限，则需要执行这个方法
     * </p>
     * @param bucketName 桶名
     */
    private void onPublicAccessBlock(final String bucketName) {
        PublicAccessBlockConfiguration publicAccessBlockConfiguration = PublicAccessBlockConfiguration.builder()
                .blockPublicAcls(false)
                .ignorePublicAcls(false)
                .blockPublicPolicy(true)
                .restrictPublicBuckets(true)
                .build();

        PutPublicAccessBlockRequest putPublicAccessBlockRequest = PutPublicAccessBlockRequest.builder()
                .bucket(bucketName)
                .publicAccessBlockConfiguration(publicAccessBlockConfiguration)
                .build();

        s3Client.putPublicAccessBlock(putPublicAccessBlockRequest);
    }

    /**
     * 开启存储桶的所有者规则.
     *
     * <p>
     *     默认关闭，主要是用于对后续存储桶整个的ACL权限设置
     * </p>
     * @param bucketName 桶名
     */
    private void onOwnerShipRuler(final String bucketName) {
        OwnershipControlsRule rule = OwnershipControlsRule.builder()
                .objectOwnership(ObjectOwnership.BUCKET_OWNER_PREFERRED)
                .build();

        OwnershipControls ownershipControls = OwnershipControls.builder()
                .rules(rule)
                .build();

        PutBucketOwnershipControlsRequest putBucketOwnershipControlsRequest = PutBucketOwnershipControlsRequest.builder()
                .bucket(bucketName)
                .ownershipControls(ownershipControls)
                .build();

        s3Client.putBucketOwnershipControls(putBucketOwnershipControlsRequest);
    }
}
