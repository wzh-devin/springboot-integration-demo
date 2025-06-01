package com.devin.minio.domain.entity;

import lombok.Data;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 2025/6/1 15:29.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
public class FileInfo {

    /**
     * 输入流.
     */
    private InputStream inputStream;

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
    private Integer expireTime = 300;
}
