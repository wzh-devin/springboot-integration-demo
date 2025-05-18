package com.devin.xxljob.common.handler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 2025/5/16 14:53.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class JobHandler {

    /**
     * 测试任务.
     */
    @XxlJob(value = "skillHandler")
    public void skillHandler() {
        String skillId = XxlJobHelper.getJobParam();
        log.info("技能id==> {} <==执行", skillId);
    }
}
