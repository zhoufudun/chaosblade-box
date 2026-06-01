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

package com.alibaba.chaosblade.box.dao.infrastructure.app.function.sync;

import com.alibaba.chaosblade.box.dao.infrastructure.event.BaseChaosEvent;
import com.alibaba.chaosblade.box.dao.infrastructure.event.ChaosEventListener;
import com.alibaba.chaosblade.box.dao.mapper.SceneFunctionMapper;
import com.alibaba.chaosblade.box.dao.model.SceneFunctionDO;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 仅主机场景过滤器 — 以 ChaosEventListener 方式实现，零侵入原有同步逻辑。
 *
 * <p>功能说明：
 * 监听 {@link ChaosBladeSyncFinishedOnStartedUpEvent} 事件（ChaosBlade 场景同步完成后触发）。
 * 当配置 {@code chaos.scene.host-only=true} 时，自动将数据库中所有非主机场景
 * （support_scope_types 不等于 '[0]'）标记为 is_delete=1，使其对前端不可见。
 *
 * <p>设计原理：
 * - Box 启动时 ChaosBladeSynchronizer 会从 YAML 同步所有场景（包括 K8s/容器/Pod/Node）到数据库
 * - 同步完成后通过 ChaosEventDispatcher 发出 ChaosBladeSyncFinishedOnStartedUpEvent 事件
 * - 本监听器收到事件后，根据配置决定是否清理非主机场景
 * - 前端所有查询都带 is_delete=false 条件，被标记的场景自动不可见
 *
 * <p>配置项（application.yml）：
 * <pre>
 * chaos:
 *   scene:
 *     host-only: true   # true=只保留主机场景，false=保留所有（默认，兼容官方）
 * </pre>
 *
 * <p>恢复 K8s 场景：
 * 1. 将配置改为 chaos.scene.host-only=false
 * 2. 执行 SQL: UPDATE t_chaos_scene_function SET is_delete=0 WHERE support_scope_types != '[0]';
 * 3. 重启 Box
 *
 * @author zhoufudun
 */
@Slf4j
@Component
public class HostOnlySceneFilter implements ChaosEventListener {

    /**
     * 仅主机模式开关。
     * true  = 同步完成后自动隐藏 K8s/容器/Pod/Node 场景，只保留主机场景
     * false = 不做任何处理，保留所有场景（默认行为，兼容官方版本）
     */
    @Value("${chaos.scene.host-only:false}")
    private boolean hostOnly;

    @Resource
    private SceneFunctionMapper sceneFunctionMapper;

    /**
     * 只处理 ChaosBladeSyncFinishedOnStartedUpEvent 事件
     */
    @Override
    public boolean support(BaseChaosEvent event) {
        return event instanceof ChaosBladeSyncFinishedOnStartedUpEvent;
    }

    /**
     * 场景同步完成后的后置处理。
     *
     * <p>当 host-only=true 时，隐藏所有 K8s/容器专属场景。
     * 判断逻辑：满足以下任一条件的场景会被隐藏：
     * 1. support_scope_types = '[2]' → 明确标记为 K8s 专属
     * 2. code 以 'chaos.container-' 开头 → 容器内场景
     * 3. code 以 'chaos.cri-' 开头 → CRI 容器运行时场景
     * 4. code 以 'chaos.node-' 开头 → K8s Node 场景
     * 5. code 以 'chaos.pod-' 开头 → K8s Pod 场景
     * 6. code 以 'litmuschaos.' 开头 → LitmusChaos（K8s 专属混沌引擎）
     *
     * <p>不会被误删的场景：
     * - support_scope_types = '[0]' 的主机场景（如 chaos.cpu.fullload）
     * - support_scope_types = '[]' 且 code 不匹配上述前缀的通用场景
     *   （如 chaos.jvm.install、chaos.jvm.uninstall、chaos.process.stop.stop）
     */
    @Override
    public void onChaosEvent(BaseChaosEvent event) {
        if (!hostOnly) {
            log.debug("[HostOnlySceneFilter] host-only mode disabled, skip filtering.");
            return;
        }

        log.info("[HostOnlySceneFilter] host-only mode enabled, hiding K8s/Container scenes...");

        // 隐藏所有明确为 K8s 专属的场景，以及 code 前缀匹配 K8s/容器的场景
        int hiddenCount = sceneFunctionMapper.update(
            null,
            new UpdateWrapper<SceneFunctionDO>()
                .set("is_delete", true)
                .eq("is_delete", false)
                .and(w -> w
                    .eq("support_scope_types", "[2]")
                    .or()
                    .likeRight("code", "chaos.container-")
                    .or()
                    .likeRight("code", "chaos.cri-")
                    .or()
                    .likeRight("code", "chaos.node-")
                    .or()
                    .likeRight("code", "chaos.pod-")
                    .or()
                    .likeRight("code", "litmuschaos.")
                )
        );

        log.info("[HostOnlySceneFilter] Done. Hidden {} K8s/Container scenes. "
            + "Host scenes (support_scope_types='[0]') and common scenes "
            + "(like chaos.jvm.install) remain visible.",
            hiddenCount);
    }

    /**
     * 过滤失败不中断启动流程，只记录日志
     */
    @Override
    public boolean interruptIfError() {
        return false;
    }
}
