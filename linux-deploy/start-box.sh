#!/bin/bash
#
# 启动 ChaosBlade Box（CMS GC + OOM dump + GC 日志轮转）
#
# 用法:
#   bash start-box.sh [env]
#     env: local | test | prod （默认 local）
#
# 不同环境堆大小:
#   local: -Xms256m  -Xmx512m
#   test:  -Xms512m  -Xmx1g
#   prod:  -Xms2g    -Xmx4g
#
# CMS GC 参数适用于 JDK 8/11（JDK 14+ 已移除 CMS）
#

DEPLOY_DIR=$(cd "$(dirname "$0")" && pwd)
LOG_DIR="$DEPLOY_DIR/logs"
DUMP_DIR="$DEPLOY_DIR/dump"
GC_LOG_DIR="$DEPLOY_DIR/gc-logs"

# JAR 路径：优先用 deploy 目录下的，其次用 chaosblade-box 项目 target 下的最新编译产物
JAR="$DEPLOY_DIR/chaosblade-box-1.1.0.jar"
if [ ! -f "$JAR" ]; then
    BOX_TARGET_JAR="/Users/zhoufudun/IdeaProjects/chaosblade-box/chaosblade-box-starter/target/chaosblade-box-1.1.0.jar"
    if [ -f "$BOX_TARGET_JAR" ]; then
        JAR="$BOX_TARGET_JAR"
    fi
fi

LOG="$LOG_DIR/box.log"
PID_FILE="$DEPLOY_DIR/box.pid"

ENV="${1:-local}"

# 校验 env
case "$ENV" in
    local|test|prod) ;;
    -h|--help|help)
        cat <<EOF
用法: bash start-box.sh [local|test|prod]

环境           profile yml             堆大小
-----------------------------------------------
local (默认)   application-local.yml   256m / 512m
test           application-test.yml    512m / 1g
prod           application-prod.yml    2g / 4g

GC:        CMS（适用于 JDK 8/11）
OOM dump:  $DEPLOY_DIR/dump/
GC 日志:   $DEPLOY_DIR/gc-logs/（最多保留 10 个，每个 100M）
应用日志:  $DEPLOY_DIR/logs/box.log
EOF
        exit 0
        ;;
    *)
        echo "[ERROR] 不支持的环境: $ENV，仅支持 local | test | prod"
        exit 1
        ;;
esac

# 根据 env 配堆大小
case "$ENV" in
    local) XMS="256m"; XMX="512m" ;;
    test)  XMS="512m"; XMX="1g"   ;;
    prod)  XMS="2g";   XMX="4g"   ;;
esac

if [ ! -f "$JAR" ]; then
    echo "[ERROR] $JAR not found"
    exit 1
fi

# 创建日志/dump 目录
mkdir -p "$LOG_DIR" "$DUMP_DIR" "$GC_LOG_DIR"

# 停掉旧进程
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if kill -0 "$OLD_PID" 2>/dev/null; then
        echo "[INFO] 停掉旧 Box (PID=$OLD_PID)"
        kill "$OLD_PID" 2>/dev/null
        sleep 3
    fi
    rm -f "$PID_FILE"
fi

# 检测 JDK 版本，决定 GC 日志参数格式
JAVA_VERSION=$(java -version 2>&1 | awk -F'"' '/version/ {print $2}' | cut -d'.' -f1)
[ "$JAVA_VERSION" = "1" ] && JAVA_VERSION=8  # 1.8.x

# 构造 GC 日志参数
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
if [ "$JAVA_VERSION" -ge 9 ]; then
    # JDK 9+ 统一日志：-Xlog:gc*:file=...:filecount=10:filesize=100M
    GC_LOG_OPTS="-Xlog:gc*,safepoint:file=$GC_LOG_DIR/gc-${ENV}-${TIMESTAMP}.log:tags,uptime,time,level:filecount=10,filesize=100M"
else
    # JDK 8 风格
    GC_LOG_OPTS="-Xloggc:$GC_LOG_DIR/gc-${ENV}-${TIMESTAMP}.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"
fi

# CMS GC + 相关优化参数
GC_OPTS="-XX:+UseConcMarkSweepGC \
    -XX:+UseParNewGC \
    -XX:+CMSParallelRemarkEnabled \
    -XX:+UseCMSCompactAtFullCollection \
    -XX:CMSFullGCsBeforeCompaction=0 \
    -XX:+CMSClassUnloadingEnabled \
    -XX:CMSInitiatingOccupancyFraction=70 \
    -XX:+UseCMSInitiatingOccupancyOnly"

# OOM dump 参数
OOM_OPTS="-XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=$DUMP_DIR \
    -XX:+ExitOnOutOfMemoryError"

echo "=========================================="
echo " 启动 ChaosBlade Box (env=$ENV)"
echo "=========================================="
echo "[INFO] jar:      $JAR"
echo "[INFO] env:      $ENV (application-${ENV}.yml)"
echo "[INFO] heap:     -Xms$XMS -Xmx$XMX"
echo "[INFO] gc:       CMS"
echo "[INFO] jdk:      $JAVA_VERSION"
echo "[INFO] dump:     $DUMP_DIR"
echo "[INFO] gc log:   $GC_LOG_DIR/gc-${ENV}-${TIMESTAMP}.log（最多 10 个 × 100M）"
echo "[INFO] app log:  $LOG"
echo ""

nohup java \
    -Xms"$XMS" -Xmx"$XMX" \
    -Duser.timezone=Asia/Shanghai \
    $GC_OPTS \
    $OOM_OPTS \
    $GC_LOG_OPTS \
    -jar "$JAR" \
    --spring.profiles.active="$ENV" \
    > "$LOG" 2>&1 &

BOX_PID=$!
echo "$BOX_PID" > "$PID_FILE"

echo "[INFO] Box 启动中 PID=$BOX_PID，等待就绪..."

for i in $(seq 1 120); do
    sleep 1
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:7001/ 2>/dev/null | grep -q "200"; then
        echo ""
        echo "[OK] Box 启动成功 (${i}s, env=$ENV, heap=$XMS/$XMX)"
        echo "     访问: http://$(hostname -I 2>/dev/null | awk '{print $1}' || echo localhost):7001"
        echo "     应用日志: tail -f $LOG"
        echo "     GC 日志:  tail -f $GC_LOG_DIR/gc-${ENV}-${TIMESTAMP}.log"
        echo "     停止:     bash $DEPLOY_DIR/stop-box.sh"
        exit 0
    fi
    if ! kill -0 "$BOX_PID" 2>/dev/null; then
        echo ""
        echo "[FAIL] Box 进程退出，查看日志:"
        tail -20 "$LOG"
        rm -f "$PID_FILE"
        exit 1
    fi
    if [ $((i % 15)) -eq 0 ]; then
        echo "[INFO] 等待中... (${i}s)"
    fi
done

echo ""
echo "[WARN] Box 启动超时（120s），可能还在初始化，查看日志:"
echo "  tail -f $LOG"
