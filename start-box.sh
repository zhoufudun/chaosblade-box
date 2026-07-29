#!/bin/bash
#
# ChaosBlade Box 启动脚本
# 用法: ./start-box.sh {test|prod} {start|stop|restart|status}
#

APP_NAME="chaosblade-box"
JAR_FILE="chaosblade-box-1.1.0.jar"
PID_FILE="/data0/app/${APP_NAME}/${APP_NAME}.pid"
LOG_FILE="/data0/logs/app/${APP_NAME}/${APP_NAME}.log"

# JVM 参数
JAVA_OPTS="-Xms512m -Xmx1g -Duser.timezone=Asia/Shanghai"

# ========== 环境配置 ==========
load_env() {
    case "$1" in
        local)
            DB_URL="jdbc:mysql://127.0.0.1:3306/chaosblade?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
            DB_USER="root"
            DB_PASS="root123456"
            ;;
        test)
            DB_URL="jdbc:mysql://10.68.37.172:3306/chaosblade?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
            DB_USER="app"
            DB_PASS="App@0000"
            ;;
        prod)
            DB_URL="jdbc:mysql://xxx:3306/chaosblade?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
            DB_USER="xxx"
            DB_PASS="xxx"
            ;;
        *)
            echo "未知环境: $1"
            echo "用法: $0 {test|prod} {start|stop|restart|status}"
            exit 1
            ;;
    esac
    echo "当前环境: $1"
}

get_pid() {
    if [ -f "$PID_FILE" ]; then
        local pid=$(cat "$PID_FILE")
        if kill -0 "$pid" 2>/dev/null; then
            echo "$pid"
            return 0
        fi
        rm -f "$PID_FILE"
    fi
    return 1
}

start() {
    local pid
    if pid=$(get_pid); then
        echo "${APP_NAME} 已在运行 (PID: $pid)"
        return 1
    fi

    if [ ! -f "$JAR_FILE" ]; then
        echo "JAR 文件不存在: $JAR_FILE"
        return 1
    fi

    echo "启动 ${APP_NAME}..."
    nohup java $JAVA_OPTS -jar "$JAR_FILE" \
        --spring.datasource.url="$DB_URL" \
        --spring.datasource.username="$DB_USER" \
        --spring.datasource.password="$DB_PASS" \
        > "$LOG_FILE" 2>&1 &

    echo $! > "$PID_FILE"
    sleep 2

    if pid=$(get_pid); then
        local ip=$(hostname -I 2>/dev/null | awk '{print $1}' || hostname -i 2>/dev/null)
        echo "${APP_NAME} 启动成功 (PID: $pid)"
        echo "访问地址: http://${ip:-localhost}:7001"
        echo "日志文件: $LOG_FILE"
    else
        echo "${APP_NAME} 启动失败，查看日志: $LOG_FILE"
        return 1
    fi
}

stop() {
    local pid
    if ! pid=$(get_pid); then
        echo "${APP_NAME} 未在运行"
        return 0
    fi

    echo "停止 ${APP_NAME} (PID: $pid)..."
    kill "$pid"

    for i in $(seq 1 10); do
        if ! kill -0 "$pid" 2>/dev/null; then
            rm -f "$PID_FILE"
            echo "${APP_NAME} 已停止"
            return 0
        fi
        sleep 1
    done

    echo "强制停止..."
    kill -9 "$pid" 2>/dev/null
    rm -f "$PID_FILE"
    echo "${APP_NAME} 已强制停止"
}

status() {
    local pid
    if pid=$(get_pid); then
        echo "${APP_NAME} 运行中 (PID: $pid)"
    else
        echo "${APP_NAME} 未运行"
    fi
}

# ========== 参数校验 ==========
if [ $# -lt 2 ]; then
    echo "用法: $0 {test|prod} {start|stop|restart|status}"
    exit 1
fi

ENV=$1
ACTION=$2

case "$ACTION" in
    start)   load_env "$ENV"; start ;;
    stop)    stop ;;
    restart) load_env "$ENV"; stop; sleep 2; start ;;
    status)  status ;;
    *)
        echo "用法: $0 {test|prod} {start|stop|restart|status}"
        exit 1
        ;;
esac