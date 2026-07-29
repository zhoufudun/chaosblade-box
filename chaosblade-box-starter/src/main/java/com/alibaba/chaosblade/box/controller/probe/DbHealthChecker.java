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

package com.alibaba.chaosblade.box.controller.probe;

import java.sql.Connection;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 运行期 DB 健康检查，供 /probe/ready 使用。
 *
 * <p>{@code ApplicationReadyEvent} 只在启动成功时触发一次、之后 ready 恒为 true，无法反映“启动后 DB 挂了”的情况。
 * 若此时 /probe/ready 仍返回 200，agent-manage 会放行启动 agent，而 agent 的注册需要写库，注册会失败并变僵尸。
 * 因此在就绪判定里叠加一次轻量 DB 连通性检查（{@link Connection#isValid(int)}）。
 *
 * <p>为避免探测频繁（多台 agent-manage 各自每几秒探一次）打到 DB，用一个短 TTL 缓存结果。
 */
@Slf4j
@Component
public class DbHealthChecker {

  /** 缓存有效期：TTL 内复用上次检查结果，避免每次探测都连库。 */
  private static final long CACHE_TTL_MS = 2000L;

  /** isValid 校验超时（秒）。 */
  private static final int VALIDATE_TIMEOUT_SEC = 1;

  private final DataSource dataSource;

  private volatile long lastCheckMs = 0L;
  private volatile boolean lastHealthy = false;

  @Autowired
  public DbHealthChecker(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /** 返回 DB 是否可用（带短 TTL 缓存）。任何异常都视为不可用。 */
  public boolean isHealthy() {
    long now = System.currentTimeMillis();
    if (now - lastCheckMs < CACHE_TTL_MS) {
      return lastHealthy;
    }
    boolean healthy;
    try (Connection connection = dataSource.getConnection()) {
      healthy = connection.isValid(VALIDATE_TIMEOUT_SEC);
    } catch (Exception e) {
      healthy = false;
      log.warn("[readiness] db health check failed: {}", e.getMessage());
    }
    lastHealthy = healthy;
    lastCheckMs = now;
    return healthy;
  }
}
