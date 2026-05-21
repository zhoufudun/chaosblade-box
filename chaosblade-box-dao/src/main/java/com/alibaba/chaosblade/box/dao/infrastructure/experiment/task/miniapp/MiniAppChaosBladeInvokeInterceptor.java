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

package com.alibaba.chaosblade.box.dao.infrastructure.experiment.task.miniapp;

import com.alibaba.chaosblade.box.common.app.sdk.ChaosAppRequest;
import com.alibaba.chaosblade.box.common.app.sdk.ChaosAppResponse;
import com.alibaba.chaosblade.box.common.app.sdk.constants.PhaseType;
import com.alibaba.chaosblade.box.common.app.sdk.scope.Host;
import com.alibaba.chaosblade.box.common.common.domain.chaosblade.ChaosBladeMetaData;
import com.alibaba.chaosblade.box.common.common.domain.response.Response;
import com.alibaba.chaosblade.box.common.common.util.MiniAppUtils;
import com.alibaba.chaosblade.box.common.experiment.task.flow.ChaosBladeAppResponse;
import com.alibaba.chaosblade.box.common.infrastructure.constant.CommonConstant;
import com.alibaba.chaosblade.box.dao.infrastructure.app.ChaosAppResponseReference;
import com.alibaba.chaosblade.box.dao.infrastructure.app.chaosblade.ChaosBladeInvoker;
import com.alibaba.chaosblade.box.dao.infrastructure.app.chaosblade.ChaosBladeMiniAppInterceptor;
import com.alibaba.chaosblade.box.dao.infrastructure.experiment.task.flow.invoker.ChaosBladeAppInvoker;
import com.alibaba.chaosblade.box.dao.infrastructure.experiment.task.flow.step.MiniAppInvokeContext;
import com.alibaba.chaosblade.box.dao.infrastructure.experiment.task.interceptor.BaseMiniAppInvokeInterceptor;
import com.alibaba.chaosblade.box.dao.infrastructure.monitor.log.RecordsRepo;
import com.alibaba.chaosblade.box.dao.model.ActivityTaskDO;
import com.alibaba.chaosblade.box.dao.model.ChaosBladeExpUidDO;
import com.alibaba.chaosblade.box.dao.model.ExperimentTaskDO;
import com.alibaba.chaosblade.box.dao.repository.ActivityTaskRepository;
import com.alibaba.chaosblade.box.dao.repository.ChaosBladeExpUidRepository;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 如果是chaosblade的话需要校验一些逻辑
 *
 * @author haibin
 */
@Component
@Slf4j
public class MiniAppChaosBladeInvokeInterceptor extends BaseMiniAppInvokeInterceptor {

  @Autowired private ChaosBladeInvoker chaosBladeInvoker;

  @Autowired private ChaosBladeExpUidRepository chaosBladeExpUidRepository;

  @Autowired private List<ChaosBladeMiniAppInterceptor> chaosBladeMiniAppInterceptorList;

  @Autowired private ActivityTaskRepository activityTaskRepository;

  @Override
  public boolean preHandle(
      MiniAppInvokeContext miniAppInvokeContext,
      ChaosAppResponseReference chaosAppResponseReference) {
    chaosAppResponseReference.setChaosAppResponse(new ChaosBladeAppResponse());
    return preHandle(miniAppInvokeContext, chaosAppResponseReference.getChaosAppResponse());
  }

  @Override
  public boolean preHandle(
      MiniAppInvokeContext miniAppInvokeContext, ChaosAppResponse chaosAppResponse) {
    if (!isChaosBladeScene(miniAppInvokeContext)) {
      return true;
    }
    ExperimentTaskDO experimentTaskDO =
        miniAppInvokeContext.getActivityInvokeContext().getContextData().getExperimentTaskDO();
    String experimentTaskId = experimentTaskDO.getTaskId();
    ActivityTaskDO activityTaskDO =
        miniAppInvokeContext.getActivityInvokeContext().getContextData().getActivityTaskDO();
    return setExpUidWhenRecoveryPhase(
        miniAppInvokeContext, experimentTaskId, activityTaskDO, chaosAppResponse);
  }

  @Override
  public void afterHandle(
      MiniAppInvokeContext miniAppInvokeContext,
      ChaosAppResponse chaosAppResponse,
      Throwable throwable) {
    if (!isChaosBladeScene(miniAppInvokeContext)) {
      return;
    }
    if (chaosAppResponse instanceof ChaosBladeAppResponse) {
      ChaosBladeAppResponse chaosBladeAppResponse = (ChaosBladeAppResponse) chaosAppResponse;
      if (chaosBladeAppResponse.getChaosBladeResponse() == null) {
        return;
      }
      PhaseType phaseType = miniAppInvokeContext.getStepExecuteContext().getPhase();
      createRecordWhenNonRecoveryPhase(miniAppInvokeContext, chaosBladeAppResponse, phaseType);
      updateRecordWhenRecoveryPhase(miniAppInvokeContext, chaosBladeAppResponse, phaseType);
    }
  }

  private boolean isChaosBladeScene(MiniAppInvokeContext miniAppInvokeContext) {
    String appCode = miniAppInvokeContext.getActivityInvokeContext().getExecutableAppCode();
    return MiniAppUtils.isFromChaosBlade(appCode) && miniAppInvokeContext.getHost() != null;
  }

  /**
   * 当是恢复阶段时候,需要查找一下expId
   *
   * @param miniAppInvokeContext
   * @param experimentTaskId
   * @param activityTaskDO
   * @param chaosAppResponse
   * @return
   */
  private boolean setExpUidWhenRecoveryPhase(
      MiniAppInvokeContext miniAppInvokeContext,
      String experimentTaskId,
      ActivityTaskDO activityTaskDO,
      ChaosAppResponse chaosAppResponse) {
    String appCode = miniAppInvokeContext.getActivityInvokeContext().getExecutableAppCode();
    if (miniAppInvokeContext.getHost() == null) {
      return true;
    }
    if (PhaseType.RECOVER.equals(activityTaskDO.getPhase())) {
      String chaosBladeExpId =
          getChaosBladeExpId(experimentTaskId, miniAppInvokeContext.getHost(), activityTaskDO);
      if (chaosBladeExpId != null) {
        miniAppInvokeContext
            .getTempData()
            .add(ChaosBladeAppInvoker.CHAOS_BLADE_EXP_ID, chaosBladeExpId);
      } else {
        if (destroyWithoutExpIdUnderNotAgentScene(
            miniAppInvokeContext, chaosAppResponse, appCode)) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean destroyWithoutExpIdUnderNotAgentScene(
      MiniAppInvokeContext miniAppInvokeContext,
      ChaosAppResponse chaosAppResponse,
      String appCode) {
    if (!MiniAppUtils.isChaosUninstall(appCode)) {
      String expId = destroyExpWithoutUid(miniAppInvokeContext);
      if (expId != null) {
        chaosAppResponse.setSuccess(true);
        chaosAppResponse.addResponseData("response", expId);
        return true;
      }
    }
    return false;
  }

  private String getChaosBladeExpId(
      String experimentTaskId, Host host, ActivityTaskDO activityTaskDO) {
    ChaosBladeExpUidDO chaosBladeExpUidDO = null;
    if (MiniAppUtils.isJvmAgentInstall(activityTaskDO.getAppCode())) {
      return queryJavaAgentExpUidDO(host, experimentTaskId);
    }
    log.info("[getChaosBladeExpId] host={}, configId={}, attackActivityTaskId={}",
        host.getIp(), host.getDeviceConfigurationId(), activityTaskDO.getAttackActivityTaskId());
    if (activityTaskDO.getAttackActivityTaskId() != null) {
      chaosBladeExpUidDO = getRecordIfFoundAttackActivityTaskId(host, activityTaskDO);
      log.info("[getChaosBladeExpId] found by attackActivityTaskId: expUid={}",
          chaosBladeExpUidDO != null ? chaosBladeExpUidDO.getExpUid() : "NULL");
    } else {
      chaosBladeExpUidDO =
          getRecordIfNotfoundAttackActivityTaskId(experimentTaskId, host, activityTaskDO);
      log.info("[getChaosBladeExpId] found by experimentTaskId+host: expUid={}",
          chaosBladeExpUidDO != null ? chaosBladeExpUidDO.getExpUid() : "NULL");
    }
    return Optional.ofNullable(chaosBladeExpUidDO).map(ChaosBladeExpUidDO::getExpUid).orElse(null);
  }

  /**
   * 根据攻击阶段的 activityTaskId 查找对应的 UID 记录。
   *
   * 解决同一台 VM 上多 Agent 实例（agent-manage 场景）恢复时 UID 冲突问题：
   * 原逻辑用 activityTaskId + host IP 查询，同 IP 多设备会查到同一条记录。
   * 改为查出所有记录后用 configurationId 精确匹配当前设备。
   *
   * @param host 当前恢复目标设备
   * @param activityTaskDO 恢复阶段的 activityTask（含 attackActivityTaskId）
   * @return 当前设备对应的 UID 记录，用于构造 destroy 命令
   */
  private ChaosBladeExpUidDO getRecordIfFoundAttackActivityTaskId(
      Host host, ActivityTaskDO activityTaskDO) {
    // 查出攻击阶段该 activityTask 下所有 UID 记录（每个设备一条）
    List<ChaosBladeExpUidDO> records = chaosBladeExpUidRepository.findByActivityTaskId(
        activityTaskDO.getAttackActivityTaskId());
    if (records == null || records.isEmpty()) {
      return null;
    }
    // 单条记录：直接返回（单设备场景，最常见，无歧义）
    if (records.size() == 1) {
      return records.get(0);
    }
    // 多条记录（同 IP 多 Agent 实例）：用 configurationId 精确匹配当前设备
    String configId = host.getDeviceConfigurationId();
    if (configId != null) {
      for (ChaosBladeExpUidDO record : records) {
        if (configId.equals(record.getConfigurationId())) {
          return record;
        }
      }
    }
    // 回退：用 host IP 匹配（兼容旧数据，configurationId 为空的场景）
    for (ChaosBladeExpUidDO record : records) {
      if (host.getIp().equals(record.getHost())) {
        return record;
      }
    }
    return records.get(0);
  }

  /**
   * 老的演练场景是没有attack activity taskId的,所以需要根据code来查询
   *
   * @param experimentTaskId
   * @param host
   * @param activityTaskDO
   * @return
   */
  private ChaosBladeExpUidDO getRecordIfNotfoundAttackActivityTaskId(
      String experimentTaskId, Host host, ActivityTaskDO activityTaskDO) {
    String executableAppCode = activityTaskDO.getExecutableAppCode();
    String realAppCode = null;
    if (MiniAppUtils.isChaosUninstall(executableAppCode)) {
      realAppCode = MiniAppUtils.getInstallCode(activityTaskDO.getExecutableAppCode());
    } else {
      realAppCode = MiniAppUtils.getAttackCodeByRecoveryCode(executableAppCode);
    }
    return chaosBladeExpUidRepository.findLastByExperimentTaskIdAndHostAndAppCodeAndNotExpired(
        experimentTaskId, host.getIp(), realAppCode);
  }

  /**
   * 如果故障注入成功了,就需要插入ChaosBlade ExpId
   *
   * @param miniAppInvokeContext
   * @param chaosBladeAppResponse
   * @param phaseType
   */
  private void createRecordWhenNonRecoveryPhase(
      MiniAppInvokeContext miniAppInvokeContext,
      ChaosBladeAppResponse chaosBladeAppResponse,
      PhaseType phaseType) {
    if (PhaseType.PREPARE.equals(phaseType) || PhaseType.ATTACK.equals(phaseType)) {
      String expId = chaosBladeAppResponse.getChaosBladeExpId();
      createChaosBladeExpRecord(miniAppInvokeContext, expId, chaosBladeAppResponse);
    }
  }

  /**
   * 如果恢复成功了,就需要更新ChaosBlade ExpId记录
   *
   * @param miniAppInvokeContext
   * @param chaosAppResponse
   * @param phaseType
   */
  private void updateRecordWhenRecoveryPhase(
      MiniAppInvokeContext miniAppInvokeContext,
      ChaosBladeAppResponse chaosAppResponse,
      PhaseType phaseType) {
    if (PhaseType.RECOVER.equals(phaseType)) {
      String attackExpId = miniAppInvokeContext.getChaosBladeExpId();
      if ((Strings.isNullOrEmpty(attackExpId))) {
        return;
      }
      ChaosBladeExpUidDO chaosBladeExpUidDO =
          findChaosBladeExpRecord(miniAppInvokeContext, attackExpId);
      chaosBladeMiniAppInterceptorList.forEach(
          chaosBladeMiniAppInterceptor ->
              chaosBladeMiniAppInterceptor.preUpdate(
                  chaosBladeExpUidDO, miniAppInvokeContext, chaosAppResponse));
      if (chaosAppResponse.isSuccess()) {
        saveRecord(chaosBladeExpUidDO);
      }
    }
  }

  private ChaosBladeExpUidDO findChaosBladeExpRecord(
      MiniAppInvokeContext miniAppInvokeContext, String attackExpId) {
    ChaosBladeExpUidDO chaosBladeExpUidDO;
    ActivityTaskDO activityTaskDO =
        miniAppInvokeContext.getActivityInvokeContext().getActivityTask();
    if (CommonConstant.APP_CODE_JAVA_AGENT_UNINSTALL.equals(
        activityTaskDO.getExecutableAppCode())) {
      chaosBladeExpUidDO =
          chaosBladeExpUidRepository.findByExpUidAndExperimentTaskId(
              attackExpId, activityTaskDO.getExperimentTaskId());
    } else {
      chaosBladeExpUidDO = chaosBladeExpUidRepository.findByExpUid(attackExpId);
    }
    return chaosBladeExpUidDO;
  }

  /**
   * 创建或查找 ChaosBlade 实验 UID 记录。
   *
   * 修复同 IP 多 Agent 场景：原逻辑用 activityTaskId + host IP 查询，
   * 同 IP 多设备会查到同一条记录导致后续设备跳过创建。
   * 改为用 configurationId 精确匹配当前设备的记录。
   */
  private void createChaosBladeExpRecord(
      MiniAppInvokeContext miniAppInvokeContext,
      String expId,
      ChaosBladeAppResponse chaosAppResponse) {
    Host host = miniAppInvokeContext.getHost();
    String configId = host.getDeviceConfigurationId();
    ChaosBladeExpUidDO chaosBladeExpUidDO = null;

    if (configId != null) {
      // 多 Agent 场景：用 configurationId 精确匹配当前设备的记录
      List<ChaosBladeExpUidDO> records = chaosBladeExpUidRepository.findByActivityTaskId(
          miniAppInvokeContext.getActivityTaskId());
      log.info("[createChaosBladeExpRecord] configId={}, activityTaskId={}, records.size={}",
          configId, miniAppInvokeContext.getActivityTaskId(), records.size());
      for (ChaosBladeExpUidDO r : records) {
        if (configId.equals(r.getConfigurationId())) {
          chaosBladeExpUidDO = r;
          break;
        }
      }
    } else {
      // 回退：官方 Agent 单设备场景，用原有逻辑
      log.info("[createChaosBladeExpRecord] configId is null, using fallback by IP");
      chaosBladeExpUidDO = chaosBladeExpUidRepository.findLastByActivityTaskIdAndHost(
          miniAppInvokeContext.getActivityTaskId(), host.getIp());
    }

    log.info("[createChaosBladeExpRecord] existing record found={}, expId to save={}",
        chaosBladeExpUidDO != null, expId);
    if (chaosBladeExpUidDO == null) {
      chaosBladeExpUidDO =
          chaosBladeExpUidRepository.createChaosBladeExpRecord(
              expId, miniAppInvokeContext.getMiniAppTaskDO(), host.getTargetIp(), false);
    }
    for (ChaosBladeMiniAppInterceptor chaosBladeMiniAppInterceptor :
        chaosBladeMiniAppInterceptorList) {
      chaosBladeMiniAppInterceptor.preCreate(
          chaosBladeExpUidDO, miniAppInvokeContext, chaosAppResponse);
    }
    saveRecord(expId, chaosBladeExpUidDO);
    for (ChaosBladeMiniAppInterceptor chaosBladeMiniAppInterceptor :
        chaosBladeMiniAppInterceptorList) {
      chaosBladeMiniAppInterceptor.afterCreate(
          chaosBladeExpUidDO, miniAppInvokeContext, chaosAppResponse);
    }
  }

  private void saveRecord(String expId, ChaosBladeExpUidDO chaosBladeExpUidDO) {
    if (chaosBladeExpUidDO.getId() != null) {
      chaosBladeExpUidRepository.update(chaosBladeExpUidDO);
    } else {
      try {
        chaosBladeExpUidRepository.add(chaosBladeExpUidDO);
      } catch (Exception exception) {
        RecordsRepo.getMiniAppInvocationRecorder()
            .log("save chaosblade result failed,expId:" + expId, false, exception);
      }
    }
  }

  private void saveRecord(ChaosBladeExpUidDO chaosBladeExpUidDO) {
    chaosBladeExpUidDO.setExpiredTime(new Date());
    chaosBladeExpUidDO.setExpired(true);
    chaosBladeExpUidDO.setGmtModified(new Date());
    chaosBladeExpUidRepository.update(chaosBladeExpUidDO);
  }

  private String queryJavaAgentExpUidDO(Host host, String experimentTaskId) {
    // 如果是jvm场景，查询出prepare阶段expId，在恢复阶段如果恢复失败，验证下java agent是否已经卸载
    ChaosBladeExpUidDO chaosBladeExpUidDO =
        chaosBladeExpUidRepository.findLastByExperimentTaskIdAndHostAndAppCodeAndNotExpired(
            experimentTaskId, host.getIp(), MiniAppUtils.AGENT_INSTALL);
    if (null != chaosBladeExpUidDO) {
      return chaosBladeExpUidDO.getExpUid();
    }
    return null;
  }

  private String destroyExpWithoutUid(MiniAppInvokeContext appInvokeContext) {
    try {
      ActivityTaskDO activityTaskDO =
          appInvokeContext.getActivityInvokeContext().getContextData().getActivityTaskDO();
      String attackActivityTaskId = activityTaskDO.getAttackActivityTaskId();
      if (attackActivityTaskId == null) {
        return null;
      }
      ActivityTaskDO attackActivityTask =
          activityTaskRepository.findById(attackActivityTaskId).orElse(null);
      if (attackActivityTask == null) {
        return null;
      }
      ChaosAppRequest chaosAppRequest = new ChaosAppRequest();
      chaosAppRequest.setMatcher(attackActivityTask.getRunParam().getArguments().getAllArguments());
      chaosAppRequest.setScope(appInvokeContext.getHost());
      if (appInvokeContext.getActivityInvokeContext().getContextData().isMockMode()) {
        return null;
      }
      Response<String> response =
          chaosBladeInvoker.destroyExpWithoutUid(
              ChaosBladeMetaData.getInstance()
                  .getChaosBladeAction(attackActivityTask.getExecutableAppCode()),
              chaosAppRequest,
              appInvokeContext.getActivityInvokeContext().getUserAk(),
              appInvokeContext.getActivityInvokeContext().getUserSk());
      if (response.isSuccess()) {
        return response.getResult();
      }
    } catch (Exception ex) {
      log.warn("warning,try recovery without uid error,ignore this");
    }
    return null;
  }

  @Override
  public Integer getOrder() {
    return Integer.MIN_VALUE + 1;
  }
}
