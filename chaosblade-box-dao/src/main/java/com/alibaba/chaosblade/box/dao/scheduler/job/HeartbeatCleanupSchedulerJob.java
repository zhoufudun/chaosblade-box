/*
 * Copyright 2025 The ChaosBlade Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.chaosblade.box.dao.scheduler.job;

import com.alibaba.chaosblade.box.dao.repository.ApplicationDeviceRepository;
import com.alibaba.chaosblade.box.dao.repository.DeviceRepository;
import com.alibaba.chaosblade.box.dao.scheduler.SchedulerConstant;
import com.alibaba.chaosblade.box.dao.scheduler.SchedulerJobService;
import com.alibaba.chaosblade.box.dao.scheduler.domain.SchedulerJobCreateRequest;
import com.alibaba.chaosblade.box.dao.scheduler.quartz.BaseJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** @author heartbeat-cleanup */
@Component
@Slf4j
@DisallowConcurrentExecution
public class HeartbeatCleanupSchedulerJob extends BaseJob implements Job, InitializingBean {

  @Autowired private ApplicationDeviceRepository applicationDeviceRepository;

  @Autowired private DeviceRepository deviceRepository;

  @Autowired private SchedulerJobService schedulerJobService;

  private static final long HEARTBEAT_TIMEOUT_MS = 2 * 60 * 1000;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    long threshold = System.currentTimeMillis() - HEARTBEAT_TIMEOUT_MS;

    // 清理 t_chaos_application_device
    try {
      int appDeviceDeleted = applicationDeviceRepository.deleteByLastHealthPingTimeLt(threshold);
      log.info(
          "heartbeat cleanup: deleted {} records from t_chaos_application_device",
          appDeviceDeleted);
    } catch (Exception e) {
      log.error("heartbeat cleanup: failed to delete from t_chaos_application_device", e);
    }

    // 清理 t_chaos_device
    try {
      int deviceDeleted = deviceRepository.deleteByLastHealthPingTimeLt(threshold);
      log.info("heartbeat cleanup: deleted {} records from t_chaos_device", deviceDeleted);
    } catch (Exception e) {
      log.error("heartbeat cleanup: failed to delete from t_chaos_device", e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    String cronExpression = "0 0/2 * * * ?";
    SchedulerJobCreateRequest schedulerJobCreateRequest =
        new SchedulerJobCreateRequest(
            cronExpression,
            0,
            HeartbeatCleanupSchedulerJob.class.getName(),
            SchedulerConstant.BUSINESS_TYPE_HEARTBEAT_CLEANUP,
            "-1",
            HeartbeatCleanupSchedulerJob.class.getName());
    schedulerJobService.addSchedulerJob(schedulerJobCreateRequest);
  }
}
