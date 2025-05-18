package com.devin.xxljob.controller;

import com.devin.common.utils.ApiResult;
import com.devin.xxljob.domain.XxlJobInfo;
import com.devin.xxljob.service.XxlService;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 2025/5/16 17:17.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobController {

    private final XxlService xxlService;

    /**
     * 添加任务.
     * @param jobInfo 任务信息
     * @return ApiResult
     */
    @PostMapping("/add")
    @ResponseBody
    public ApiResult<Void> add(@RequestBody final XxlJobInfo jobInfo) {
        xxlService.add(jobInfo);
        return ApiResult.success();
    }
}
