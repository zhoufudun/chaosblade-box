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

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 维护 Box 的“就绪”状态，供 /probe/ready 探测使用。
 *
 * <p>就绪判定基于 Spring 的生命周期事件：
 *
 * <ul>
 *   <li>{@link ApplicationReadyEvent}：容器 refresh 完成且所有 ApplicationRunner/CommandLineRunner
 *       执行完毕后触发，代表应用已可对外提供服务（包括处理 Agent 注册），此时置为就绪。
 *   <li>{@link ContextClosedEvent}：应用开始优雅停机时触发，立即置为未就绪，使探测在停机期间返回失败，
 *       让 agent-manage 提前感知、不要在此刻启动/注册 Agent。
 * </ul>
 *
 * @author agent-manage readiness gate
 */
@Slf4j
@Component
public class BoxReadinessListener {

    private final AtomicBoolean ready = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        ready.set(true);
        log.info("[readiness] box is READY, /probe/ready will return 200");
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed(ContextClosedEvent event) {
        ready.set(false);
        log.info("[readiness] box is shutting down, /probe/ready will return 503");
    }

    public boolean isReady() {
        return ready.get();
    }
}
