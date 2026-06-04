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

package com.alibaba.chaosblade.box.service.command.agent;

import com.alibaba.chaosblade.box.common.commands.SpringBeanCommand;
import com.alibaba.chaosblade.box.common.common.domain.response.Response;
import com.alibaba.chaosblade.box.common.infrastructure.DomainFactory;
import com.alibaba.chaosblade.box.dao.model.DeviceDO;
import com.alibaba.chaosblade.box.dao.repository.DeviceRepository;
import com.alibaba.chaosblade.box.service.command.scope.PrivateScope;
import com.alibaba.chaosblade.box.service.infrastructure.UserApplicationRegister;
import com.alibaba.chaosblade.box.service.model.agent.ChaosAgentRegisterResultEntity;
import com.alibaba.chaosblade.box.service.model.agent.RegisteredCallbackRequest;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** @author haibin */
@Component
@Slf4j
public class RegisteredRequestCommand
    extends SpringBeanCommand<RegisteredCallbackRequest, Response<ChaosAgentRegisterResultEntity>> {

  private static Logger LOGGER = LoggerFactory.getLogger("agent-register");

  @Autowired private DomainFactory domainFactory;

  @Autowired private UserApplicationRegister userApplicationRegister;

  @Autowired private DeviceRepository deviceRepository;

  @Override
  public Response<ChaosAgentRegisterResultEntity> execute(
      RegisteredCallbackRequest registeredCallbackRequest) {
    log.info(
        "start handle client register request,{}" + JSON.toJSONString(registeredCallbackRequest));
    try {
      // === 多实例 Agent 支持：从 appGroup 解析 javaPid/javaPort ===
      resolveMultiInstanceMeta(registeredCallbackRequest);
      // === 多实例 Agent 支持结束 ===

      // 1. check parameter license
      if (Strings.isNullOrEmpty(registeredCallbackRequest.getAk())) {
        return Response.ofFailure(Response.Code.Parameter_Empty, "license Required");
      }
      // 2. get userId by license
      String userId =
          userApplicationRegister.getUserId(
              registeredCallbackRequest.getUserId(), registeredCallbackRequest.getAk());
      if (Strings.isNullOrEmpty(userId)) {
        return Response.ofFailure(Response.Code.Parameter_Empty, "uid illegal");
      }
      registeredCallbackRequest.setUserId(userId);

      // 3. check namespace
      if (StringUtils.isBlank(registeredCallbackRequest.getNamespace())
          || !userApplicationRegister.checkNamespace(
              userId, registeredCallbackRequest.getNamespace())) {
        return Response.ofFailure(Response.Code.Parameter_Empty, "namespace illegal");
      }

      // 4. register agent
      DeviceDO requestDeviceDO =
          domainFactory.getBean(PrivateScope.class).register(registeredCallbackRequest);
      Long appId =
          userApplicationRegister.registerApplicationByHost(
              requestDeviceDO,
              registeredCallbackRequest.getAppName(),
              registeredCallbackRequest.getAppGroup());
      if (appId == null) {
        return Response.ofFailure(Response.Code.SERVER_ERROR, "register application failed");
      }
      ChaosAgentRegisterResultEntity chaosAgentRegisterResultEntity =
          new ChaosAgentRegisterResultEntity();
      chaosAgentRegisterResultEntity.setConfigurationId(requestDeviceDO.getConfigurationId());
      chaosAgentRegisterResultEntity.setAk(registeredCallbackRequest.getAk());
      chaosAgentRegisterResultEntity.setSk(registeredCallbackRequest.getUserId());
      chaosAgentRegisterResultEntity.setUid(registeredCallbackRequest.getUserId());
      return Response.ofSuccess(chaosAgentRegisterResultEntity);
    } catch (Exception ex) {
      LOGGER.error("execute agent register request failed", ex);
      return Response.ofFailure(
          Response.Code.SERVER_ERROR, "execute agent register request failed:" + ex.getMessage());
    }
  }

  /**
   * 多实例 Agent 支持：从 appGroup 中解析 javaPid/javaPort/javaProcess 元数据。
   * <p>
   * agent-manage 启动 agent 时会将元数据编码到 appGroup 字段中：
   * 格式: "originalAppGroup#javaPid=12345#javaPort=18080#javaProcess=app.jar"
   * <p>
   * 触发条件：appGroup 包含 #javaPid= 或 #javaProcess= 之一
   * <p>
   * 本方法做四件事：
   * 1. 恢复原始 appGroup（去掉 # 后的编码部分）
   * 2. 将 javaPid 设置到 pid 字段（如果存在）
   * 3. 将 javaProcess 设置到 javaProcess 字段（如果存在，供 ext_info 存储）
   * 4. 改写 instanceId 和 deviceId 使多实例在 Box 中唯一
   *    格式: hostname-appInstance:javaPort
   */
  private void resolveMultiInstanceMeta(RegisteredCallbackRequest request) {
    String appGroup = request.getAppGroup();
    if (appGroup == null || !appGroup.contains("#")) {
      return; // 不含 # 分隔符，非多实例模式
    }
    // 至少需要包含 javaPid 或 javaProcess 之一才触发
    if (!appGroup.contains("#javaPid=") && !appGroup.contains("#javaProcess=")) {
      return;
    }

    int firstHash = appGroup.indexOf('#');
    String originalAppGroup = appGroup.substring(0, firstHash);
    String meta = appGroup.substring(firstHash + 1);

    String javaPid = null;
    String javaPort = null;
    String javaProcess = null;
    for (String kv : meta.split("#")) {
      String[] pair = kv.split("=", 2);
      if (pair.length == 2) {
        if ("javaPid".equals(pair[0])) javaPid = pair[1];
        if ("javaPort".equals(pair[0])) javaPort = pair[1];
        if ("javaProcess".equals(pair[0])) javaProcess = pair[1];
      }
    }

    // 恢复原始 appGroup
    request.setAppGroup(originalAppGroup);

    // 改写 instanceId 和 deviceId 使多实例唯一
    String appName = request.getAppName();
    String instanceId = request.getInstanceId();
    if (appName != null && !appName.isEmpty() && javaPort != null) {
      String newId = instanceId + "-" + appName + ":" + javaPort;
      request.setInstanceId(newId);
      request.setDeviceId(newId);
      log.info("[multi-instance] rewrite instanceId={}, appGroup={}", newId, originalAppGroup);
    }

    // 将 javaPid 设置到 pid 字段；如果没有 javaPid 但有 javaProcess，清除 agent 自身 PID
    if (javaPid != null && !javaPid.isEmpty() && !"0".equals(javaPid)) {
      request.setPid(javaPid);
    } else if (javaProcess != null && !javaProcess.isEmpty()) {
      // 只有 javaProcess 场景：清除 agent 自身上报的 pid，避免存入 ext_info
      request.setPid(null);
    }

    // 将 javaProcess 存入 request 的扩展属性（供 PrivateScope 写入 ext_info）
    if (javaProcess != null && !javaProcess.isEmpty()) {
      request.setJavaProcess(javaProcess);
    }
  }
}
