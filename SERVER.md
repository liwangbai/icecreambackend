# 服务器部署信息

## 基本信息

| 项目 | 值 |
|------|-----|
| 服务器 | 阿里云轻量应用服务器 |
| IP | 112.74.44.125 |
| 域名 | wpyai.cn |
| 系统 | CentOS + 宝塔面板 |
| 项目路径 | /root/opt/icecreambackend |
| Git 仓库 | https://github.com/liwangbai/icecreambackend.git |

## Docker 容器

| 容器名 | 端口 | 用途 |
|--------|------|------|
| `icecream-backend` | 8080 | Spring Boot 后端服务 |
| `icecream-mysql` | 3306 | MySQL 8.1.0 数据库 |
| `icecream-redis` | 6379 | Redis 7 缓存 |

所有端口仅绑定 `127.0.0.1`，外部通过 Nginx 反向代理访问。

```bash
# 查看容器状态
docker ps | grep icecream
```

## 环境变量

环境变量通过 `/root/opt/icecreambackend/.env` 文件配置，docker-compose 自动加载：

```bash
# .env 文件内容示例
DB_PASSWORD=你的数据库密码
JWT_SECRET=你的JWT密钥(至少256位)
CORS_ALLOWED_ORIGINS=*
```

## Nginx 配置

- 主站点配置: `/www/server/panel/vhost/nginx/wpyai.cn.conf`（包含 /api/v1 和 /ws 反代规则）
- 独立配置: `/www/server/panel/vhost/nginx/icecream.conf`
- SSL 证书: 由宝塔面板管理自动续签

### 路由规则 (wpyai.cn)

```
/ws/*      → 8080 (WebSocket)
/api/v1/*  → 8080 (App 接口)
```

```bash
# 修改 Nginx 配置后重载
nginx -t && systemctl reload nginx
```

## 部署流程

### 部署脚本

脚本位于 `/root/opt/icecreambackend/deploy.sh`：

```bash
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
```

赋予执行权限：
```bash
chmod +x /root/opt/icecreambackend/deploy.sh
```

### 更新流程

1. Mac 上修改代码，commit 并 push:
   ```bash
   cd /Users/wpy/work/workspace/IdeaProjects/icecreambackend
   git add .
   git commit -m "描述改动"
   git push origin dev
   ```

2. SSH 到服务器，执行部署:
   ```bash
   ssh root@112.74.44.125
   /root/opt/icecreambackend/deploy.sh
   ```

### 仅重启（不重新构建）

```bash
cd /root/opt/icecreambackend
docker-compose restart backend
```

### 首次部署

```bash
cd /root/opt/icecreambackend
# 创建 .env 文件
cp .env.example .env
# 编辑环境变量
vim .env
# 启动所有服务
docker-compose up -d
```

## 数据库管理

### 执行迁移脚本

```bash
# 进入 MySQL 容器执行迁移
docker exec -i icecream-mysql mysql -uroot -p${DB_PASSWORD} icecream_prod < docker/migration_xxx.sql
```

### 数据库备份

```bash
# 备份
docker exec icecream-mysql mysqldump -uroot -p${DB_PASSWORD} icecream_prod > backup_$(date +%Y%m%d).sql

# 恢复
docker exec -i icecream-mysql mysql -uroot -p${DB_PASSWORD} icecream_prod < backup_20240101.sql
```

## 故障排查

```bash
# 容器是否运行
docker ps | grep icecream

# 后端日志
docker-compose logs -f --tail=100 backend

# MySQL 日志
docker-compose logs -f --tail=100 mysql

# Redis 日志
docker-compose logs -f --tail=100 redis

# 端口是否在监听
ss -tlnp | grep 8080

# 本地测试接口
curl http://localhost:8080/manage/health

# Nginx 日志
tail -f /www/wwwlogs/wpyai.cn.log
tail -f /www/wwwlogs/wpyai.cn.error.log

# 查看容器资源占用
docker stats icecream-backend icecream-mysql icecream-redis
```

## 注意事项

- 宝塔面板管理 SSL 证书自动续签，不要手动改证书文件
- 修改 Nginx 配置时不要动 `/ws` 和 `/api/v1` 的 `location` 块，否则 App 接口会断
- 所有容器端口仅绑定 `127.0.0.1`，必须通过 Nginx 访问，不要改为 `0.0.0.0`
- MySQL 和 Redis 数据持久化在 Docker volumes 中，`docker-compose down` 不会丢失数据，但 `docker-compose down -v` 会清除数据
- 后端容器内存限制为 640M，如果 OOM 需要调整 docker-compose.yml 中的 `memory` 限制
