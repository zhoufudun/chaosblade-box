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

import com.alibaba.chaosblade.box.dao.repository.ApplicationDeviceRepository;
import com.alibaba.chaosblade.box.dao.repository.UserRepository;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供 agent-manage 的僵尸检测（Tier2）查询“某些 agent 在 Box 侧是否 ONLINE”。
 *
 * <p>判据基于 application_device（心跳驱动在线），而非 device 表（ping 驱动在线）：Box 会 ping 活着的僵尸
 * agent 使 device 表保持在线，但机器数（应用管理）看的是 application_device，因此这里用后者才能真实反映
 * “注册成功且在心跳”的状态。
 *
 * <p>无需鉴权（与 InnerController 同级），按 license 解析 userId 做租户隔离，只返回属于该用户的在线记录。
 * 路径在根路径 {@code /probe/devices}（不带 /chaos 前缀）。
 *
 * <p>请求：{@code {"license":"...","deviceIds":["host-order-service:18180", ...]}}
 * <br>其中 deviceId 即 {@code {hostname}-{appName}:{javaPort}}（= application_device.device_name）。
 *
 * <p>响应：{@code {"licenseValid":true,"online":["host-order-service:18180"]}}
 * <br>online 为入参中当前 ONLINE 的子集；licenseValid=false 表示 license 解析不到用户，agent-manage 应跳过判定。
 */
@Slf4j
@RestController
public class DeviceProbeController {

  @Autowired private UserRepository userRepository;

  @Autowired private ApplicationDeviceRepository applicationDeviceRepository;

  @PostMapping("/probe/devices")
  public DeviceOnlineResponse devices(@RequestBody DeviceOnlineRequest request) {
    DeviceOnlineResponse response = new DeviceOnlineResponse();
    if (request == null
        || Strings.isNullOrEmpty(request.getLicense())
        || request.getDeviceIds() == null
        || request.getDeviceIds().isEmpty()) {
      response.setLicenseValid(false);
      response.setOnline(Collections.emptyList());
      return response;
    }
    String userId = userRepository.getUserIdByLicense(request.getLicense());
    if (Strings.isNullOrEmpty(userId)) {
      // license 解析不到用户：可能配置错误或 Box 侧异常。返回 licenseValid=false，让 agent-manage 跳过判定，
      // 避免因“查不到在线”把 agent 误判为僵尸而反复重启。
      response.setLicenseValid(false);
      response.setOnline(Collections.emptyList());
      return response;
    }
    List<String> online =
        applicationDeviceRepository.findOnlineDeviceNames(userId, request.getDeviceIds());
    response.setLicenseValid(true);
    response.setOnline(online);
    return response;
  }

  /** 请求体。 */
  public static class DeviceOnlineRequest {
    private String license;
    private List<String> deviceIds;

    public String getLicense() {
      return license;
    }

    public void setLicense(String license) {
      this.license = license;
    }

    public List<String> getDeviceIds() {
      return deviceIds;
    }

    public void setDeviceIds(List<String> deviceIds) {
      this.deviceIds = deviceIds;
    }
  }

  /** 响应体。 */
  public static class DeviceOnlineResponse {
    private boolean licenseValid;
    private List<String> online;

    public boolean isLicenseValid() {
      return licenseValid;
    }

    public void setLicenseValid(boolean licenseValid) {
      this.licenseValid = licenseValid;
    }

    public List<String> getOnline() {
      return online;
    }

    public void setOnline(List<String> online) {
      this.online = online;
    }
  }
}
