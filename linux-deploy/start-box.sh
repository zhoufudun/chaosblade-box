#!/bin/bash
#
# 启动 ChaosBlade Box
# 用法:
#   bash start-box.sh [env]
#     env: local | test | prod （默认 local）
#
# 例:
#   bash start-box.sh local   # 用 application-local.yml
#   bash start-box.sh prod    # 用 application-prod.yml
#   bash start-box.sh test    # 用 application-test.yml
#

DEPLOY_DIR=$(cd "$(dirname "$0")" && pwd)

# JAR 路径：优先用 deploy 目录下的，其次用 chaosblade-box 项目 target 下的最新编译产物
JAR="$DEPLOY_DIR/chaosblade-box-1.1.0.jar"
if [ ! -f "$JAR" ]; then
    BOX_TARGET_JAR="/Users/zhoufudun/IdeaProjects/chaosblade-box/chaosblade-box-starter/target/chaosblade-box-1.1.0.jar"
    if [ -f "$BOX_TARGET_JAR" ]; then
        JAR="$BOX_TARGET_JAR"
    fi
fi

LOG="$DEPLOY_DIR/box.log"
PID_FILE="$DEPLOY_DIR/box.pid"

ENV="${1:-local}"

# 校验 env
case "$ENV" in
    local|test|prod) ;;
    -h|--help|help)
        echo "用法: bash start-box.sh [local|test|prod]"
        echo "  local (默认): application-local.yml"
        echo "  test:         application-test.yml"
        echo "  prod:         application-prod.yml"
        exit 0
        ;;
    *)
        echo "[ERROR] 不支持的环境: $ENV，仅支持 local|test|prod"
        exit 1
        ;;
esac

if [ ! -f "$JAR" ]; then
    echo "[ERROR] $JAR not found"
    exit 1
fi

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

echo "=========================================="
echo " 启动 ChaosBlade Box (env=$ENV)"
echo "=========================================="
echo "[INFO] jar:   $JAR"
echo "[INFO] env:   $ENV (application-${ENV}.yml)"
echo "[INFO] log:   $LOG"
echo ""

nohup java \
    -Xms512m -Xmx1g \
    -Duser.timezone=Asia/Shanghai \
    -jar "$JAR" \
    --spring.profiles.active="$ENV" \
    --logging.level.root=info \
    > "$LOG" 2>&1 &

BOX_PID=$!
echo "$BOX_PID" > "$PID_FILE"

echo "[INFO] Box 启动中 PID=$BOX_PID，等待就绪..."

for i in $(seq 1 120); do
    sleep 1
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:7001/ 2>/dev/null | grep -q "200"; then
        echo ""
        echo "[OK] Box 启动成功 (${i}s, env=$ENV)"
        echo "     访问: http://$(hostname -I 2>/dev/null | awk '{print $1}' || echo localhost):7001"
        echo "     日志: tail -f $LOG"
        echo "     停止: $DEPLOY_DIR/stop-box.sh"
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
