package com.devin.sso.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IdTokenClaims {

    /**
     * 用户标识.
     */
    private String subject;

    /**
     * 用户名称.
     */
    private String name;

    /**
     * 用户邮箱.
     */
    private String email;

}