#!/bin/bash
#
# 停止 ChaosBlade Box
#

DEPLOY_DIR=$(cd "$(dirname "$0")" && pwd)
PID_FILE="$DEPLOY_DIR/box.pid"

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
        echo "停止 Box (PID=$PID) ..."
        kill "$PID"
        sleep 3
        if kill -0 "$PID" 2>/dev/null; then
            echo "强制停止..."
            kill -9 "$PID" 2>/dev/null
        fi
        echo "已停止"
    else
        echo "Box 未运行 (PID=$PID 不存在)"
    fi
    rm -f "$PID_FILE"
else
    # 通过进程名查找
    PID=$(pgrep -f "chaosblade-box-1.1.0.jar" 2>/dev/null)
    if [ -n "$PID" ]; then
        echo "停止 Box (PID=$PID) ..."
        kill "$PID"
        sleep 3
        echo "已停止"
    else
        echo "Box 未运行"
    fi
fi
