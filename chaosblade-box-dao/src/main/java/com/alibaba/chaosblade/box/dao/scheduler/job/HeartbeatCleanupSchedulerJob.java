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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HeartbeatCleanupSchedulerJob {

  private static final long HEARTBEAT_TIMEOUT_MS = 2 * 60 * 1000;

  @Autowired private ApplicationDeviceRepository applicationDeviceRepository;

  @Autowired private DeviceRepository deviceRepository;

  @Scheduled(fixedRate = 120000)
  public void cleanup() {
    long threshold = System.currentTimeMillis() - HEARTBEAT_TIMEOUT_MS;

    try {
      int appDeviceDeleted = applicationDeviceRepository.deleteByLastHealthPingTimeLt(threshold);
      log.info("heartbeat cleanup: deleted {} records from t_chaos_application_device", appDeviceDeleted);
    } catch (Exception e) {
      log.error("heartbeat cleanup: failed to delete from t_chaos_application_device", e);
    }

    try {
      int deviceDeleted = deviceRepository.deleteByLastHealthPingTimeLt(threshold);
      log.info("heartbeat cleanup: deleted {} records from t_chaos_device", deviceDeleted);
    } catch (Exception e) {
      log.error("heartbeat cleanup: failed to delete from t_chaos_device", e);
    }
  }
}
