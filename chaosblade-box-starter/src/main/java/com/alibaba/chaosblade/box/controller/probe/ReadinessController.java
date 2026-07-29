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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Box 就绪探测接口，供 agent-manage 在启动 Agent 前判断 Box 是否已完全就绪。
 *
 * <p>该接口无需鉴权（与 InnerController 同级，不带 @LoginUser、不带 @AgentEndPoint）。
 *
 * <p>就绪 = 「Spring 生命周期已就绪」且「DB 可用」。叠加 DB 检查用于覆盖“启动后 DB 挂了但进程还活着”的情况，
 * 避免 agent-manage 在 DB 不可用时放行启动 Agent，导致注册写库失败变僵尸。
 *
 * <ul>
 *   <li>就绪：HTTP 200，body "ready"
 *   <li>启动中/停机中：HTTP 503，body "starting"
 *   <li>生命周期已就绪但 DB 不可用：HTTP 503，body "db-unavailable"
 * </ul>
 *
 * <p>注意：本接口是普通 Spring MVC 端点，位于根路径 {@code /probe/ready}（不走 Agent 端点的
 * {@code /chaos} 前缀，也不受其影响），即 {@code http://{boxAddress}/probe/ready}。
 *
 * @author agent-manage readiness gate
 */
@RestController
public class ReadinessController {

  @Autowired private BoxReadinessListener boxReadinessListener;

  @Autowired private DbHealthChecker dbHealthChecker;

  @GetMapping("/probe/ready")
  public ResponseEntity<String> ready() {
    if (!boxReadinessListener.isReady()) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("starting");
    }
    if (!dbHealthChecker.isHealthy()) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("db-unavailable");
    }
    return ResponseEntity.ok("ready");
  }
}
