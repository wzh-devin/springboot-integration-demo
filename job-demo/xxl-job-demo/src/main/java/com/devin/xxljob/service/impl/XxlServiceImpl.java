package com.devin.xxljob.service.impl;

import com.devin.xxljob.domain.XxlJobInfo;
import com.devin.xxljob.mapper.XxlJobInfoDao;
import com.devin.xxljob.service.XxlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * 2025/5/16 18:49.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class XxlServiceImpl implements XxlService {

    private final XxlJobInfoDao xxlJobInfoDao;

    @Override
    public void add(final XxlJobInfo jobInfo) {
        jobInfo.setAddTime(new Date());
        jobInfo.setUpdateTime(new Date());
        jobInfo.setGlueUpdatetime(new Date());

        xxlJobInfoDao.save(jobInfo);
        if (jobInfo.getId() < 1) {
            log.error("add job error");
            throw new RuntimeException("add job error");
        }

        start(jobInfo);
    }

    private void start(final XxlJobInfo xxlJobInfo) {
        xxlJobInfo.setTriggerStatus(1);
        xxlJobInfo.setTriggerLastTime(0);
        xxlJobInfo.setTriggerNextTime(new Date().getTime());

        xxlJobInfo.setUpdateTime(new Date());
        xxlJobInfoDao.update(xxlJobInfo);
    }
}
