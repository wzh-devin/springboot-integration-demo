package com.devin.s3.domain.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 2025/4/16 18:56.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@Schema(name = "存储桶信息")
public class BucketInfoReq {
    /**
     * 桶名.
     */
    @Schema(description = "桶名")
    private String name;

    /**
     * 桶的访问权限.
     * @see software.amazon.awssdk.services.s3.model.BucketCannedACL
     */
    @Schema(description = "桶的访问权限")
    private String acl;
}
