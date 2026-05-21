#!/bin/bash
set -e

cd /root/opt/icecreambackend

echo ">>> 拉取最新代码..."
git pull origin dev

echo ">>> 重新构建并启动服务..."
docker-compose up -d --build backend

echo ">>> 等待服务启动..."
sleep 10

# 健康检查
if curl -sf http://localhost:8080/manage/health > /dev/null; then
    echo ">>> 部署成功! 服务已就绪"
else
    echo ">>> 警告: 健康检查未通过，请检查日志"
    docker-compose logs --tail=50 backend
    exit 1
fi
