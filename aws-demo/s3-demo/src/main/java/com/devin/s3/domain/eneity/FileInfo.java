package com.devin.s3.domain.eneity;

import lombok.Data;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import java.time.Duration;

/**
 * 2025/4/16 17:43.
 *
 * <p>
 *     文件信息
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
public class FileInfo {

    /**
     * 文件字节数组.
     */
    private byte[] fileBytes;

    /**
     * 文件原始名.
     */
    private String originalFileName;

    /**
     * 文件名前缀.
     */
    private String prefix;

    /**
     * 文件名后缀.
     */
    private String suffix;

    /**
     * 文件大小.
     */
    private long size;

    /**
     * 文件的md5信息.
     * TODO 后续用于判断文件是否重复上传
     */
    private String md5;

    /**
     * 文件的访问权限. {@link ObjectCannedACL}
     */
    private String acl;

    /**
     * 文件类型.
     */
    private String contentType;

    /**
     * 上传的文件名.
     */
    private String key;

    /**
     * 文件访问的过期时间.<br>
     * <u>默认时间5分钟</u>
     */
    private Duration expireTime = Duration.ofMinutes(5);
}
