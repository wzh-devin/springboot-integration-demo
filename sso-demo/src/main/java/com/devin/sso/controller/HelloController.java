package com.devin.sso.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 2025/9/18 18:54.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/home")
public class HelloController {

    @GetMapping("")
    public String hello() {
        return "hello, world";
    }
}
