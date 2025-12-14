# 途点儿啥项目公网部署检查清单

**项目名称**: 途点儿啥 - 协作式旅行规划系统  
**部署日期**: 2025年12月13日  
**部署工程师**: AI Assistant

---

## 📋 信息收集清单

### 1. 服务器信息

```yaml
服务器配置:
  公网IP: ________________
  云服务商: [ ] 阿里云 [ ] 腾讯云 [ ] 华为云 [ ] AWS [ ] 其他: ________
  操作系统: [ ] Ubuntu 22.04 [ ] CentOS 7 [ ] 其他: ________
  CPU: ___核
  内存: ___GB
  磁盘: ___GB
  
SSH连接信息:
  端口: _______ (默认22)
  用户名: _________________
  登录方式: [ ] 密码 [ ] 密钥
  密码/密钥路径: _________________
```

**最低配置要求**：
- CPU: 2核
- 内存: 4GB
- 磁盘: 40GB
- 带宽: 1Mbps

---

### 2. 域名信息

```yaml
域名配置:
  是否有域名: [ ] 是 [ ] 否
  域名: _________________
  DNS解析状态: [ ] 已解析 [ ] 未解析
  备案状态: [ ] 已备案 [ ] 未备案 [ ] 无需备案（海外服务器）
  
SSL证书:
  是否启用HTTPS: [ ] 是 [ ] 否
  证书类型: [ ] Let's Encrypt免费证书 [ ] 付费证书 [ ] 暂不使用
```

**域名访问示例**：
- 有域名: `http://tudianersha.com` 或 `https://tudianersha.com`
- 无域名: `http://123.456.789.012:8010`

---

### 3. 数据库配置

```yaml
数据库方案: [ ] 云数据库RDS [ ] 服务器自建MySQL [ ] 现有数据库

云数据库RDS配置 (如适用):
  数据库地址: _________________
  端口: _______ (默认3306)
  数据库名: tudianersha
  用户名: _________________
  密码: _________________
  
服务器自建MySQL配置 (如适用):
  MySQL版本: [ ] 8.0 [ ] 5.7
  root密码: _________________
  应用数据库名: tudianersha
  应用用户名: _________________
  应用用户密码: _________________
```

**数据迁移**：
- [ ] 需要从本地数据库迁移数据
- [ ] 全新部署，无需迁移

---

### 4. 第三方服务密钥

```yaml
Kimi AI:
  当前密钥: sk-bCclui8VTON2LeiUgmNSUwPiC6FxmyIwpkXvidii4m4NQoTI
  生产环境: [ ] 继续使用当前密钥 [ ] 更换新密钥: _________________
  
高德地图:
  当前密钥: 4290f3a6e308a95a70bc29f5577a6a21
  生产环境: [ ] 继续使用当前密钥 [ ] 更换新密钥: _________________
  
QQ邮箱SMTP:
  SMTP服务器: smtp.qq.com
  邮箱地址: _________________
  SMTP授权码: _________________
  (注意: 不是QQ密码，需在QQ邮箱设置中生成)
```

**密钥安全提示**：
- ✅ 生产环境建议使用独立密钥
- ✅ 定期更换密钥
- ❌ 不要在公开代码中暴露密钥

---

### 5. 部署方式选择

```yaml
部署方案: 
  [ ] 方案1: 简单部署 (JAR包直接运行)
      - 访问方式: http://IP:8010
      - 适用场景: 快速测试
      
  [ ] 方案2: 标准部署 (Nginx反向代理 + JAR)
      - 访问方式: http://域名 或 https://域名
      - 适用场景: 正式上线
      
  [ ] 方案3: 完整方案 (Nginx + Docker + MySQL)
      - 访问方式: https://域名
      - 适用场景: 生产环境
```

**推荐**：方案2（标准部署）

---

### 6. 端口与安全配置

```yaml
防火墙/安全组规则:
  需要开放的端口:
    - 80   (HTTP)   [ ] 已开放 [ ] 待开放
    - 443  (HTTPS)  [ ] 已开放 [ ] 待开放
    - 22   (SSH)    [ ] 已开放 [ ] 待开放
    
  禁止公网访问的端口:
    - 3306 (MySQL)  [ ] 已限制 [ ] 待限制
    - 8010 (Spring Boot) [ ] 已限制 [ ] 待限制

SSH安全加固:
  [ ] 禁用root密码登录
  [ ] 仅允许密钥登录
  [ ] 限制SSH访问IP白名单
```

---

## 🚀 自动化部署流程

### 阶段1: 环境准备 (自动化执行)

```bash
# 1.1 连接服务器
ssh -p <端口> <用户名>@<服务器IP>

# 1.2 更新系统
sudo apt update && sudo apt upgrade -y  # Ubuntu
# 或
sudo yum update -y  # CentOS

# 1.3 安装Java 17
sudo apt install openjdk-17-jdk -y  # Ubuntu
# 或
sudo yum install java-17-openjdk -y  # CentOS

# 验证安装
java -version

# 1.4 安装MySQL (如果选择服务器自建)
sudo apt install mysql-server -y  # Ubuntu
# 或
sudo yum install mysql-server -y  # CentOS

# 启动MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# 1.5 安装Nginx (如果选择标准部署)
sudo apt install nginx -y  # Ubuntu
# 或
sudo yum install nginx -y  # CentOS

# 启动Nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

**执行状态**：
- [ ] Java 17 安装完成
- [ ] MySQL 安装完成
- [ ] Nginx 安装完成

---

### 阶段2: 数据库初始化 (自动化执行)

```bash
# 2.1 登录MySQL
sudo mysql -u root -p

# 2.2 创建数据库和用户
CREATE DATABASE tudianersha DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'tudianersha_user'@'localhost' IDENTIFIED BY '<设置密码>';
GRANT ALL PRIVILEGES ON tudianersha.* TO 'tudianersha_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# 2.3 导入数据库结构
mysql -u tudianersha_user -p tudianersha < /path/to/schema.sql
```

**执行状态**：
- [ ] 数据库创建完成
- [ ] 用户权限配置完成
- [ ] 表结构导入完成

---

### 阶段3: 应用部署 (自动化执行)

```bash
# 3.1 创建应用目录
sudo mkdir -p /opt/tudianersha
sudo chown -R <用户名>:<用户组> /opt/tudianersha

# 3.2 上传JAR包
# 本地执行打包
cd e:/tudianershatest/tudianersha
mvn clean package -DskipTests

# 上传到服务器
scp -P <端口> target/tudianersha-1.0.0.jar <用户名>@<服务器IP>:/opt/tudianersha/

# 3.3 上传配置文件
scp -P <端口> application-prod.yml <用户名>@<服务器IP>:/opt/tudianersha/

# 3.4 创建启动脚本
cat > /opt/tudianersha/start.sh << 'EOF'
#!/bin/bash
nohup java -jar \
  -Dspring.profiles.active=prod \
  -Xms512m -Xmx1024m \
  /opt/tudianersha/tudianersha-1.0.0.jar \
  > /opt/tudianersha/app.log 2>&1 &
echo $! > /opt/tudianersha/app.pid
EOF

chmod +x /opt/tudianersha/start.sh

# 3.5 创建停止脚本
cat > /opt/tudianersha/stop.sh << 'EOF'
#!/bin/bash
if [ -f /opt/tudianersha/app.pid ]; then
  kill $(cat /opt/tudianersha/app.pid)
  rm /opt/tudianersha/app.pid
fi
EOF

chmod +x /opt/tudianersha/stop.sh
```

**执行状态**：
- [ ] JAR包上传完成
- [ ] 配置文件配置完成
- [ ] 启动脚本创建完成

---

### 阶段4: Nginx配置 (如果选择标准部署)

```nginx
# 4.1 创建Nginx配置文件
sudo nano /etc/nginx/sites-available/tudianersha

# 配置内容
server {
    listen 80;
    server_name <域名或IP>;

    # 访问日志
    access_log /var/log/nginx/tudianersha_access.log;
    error_log /var/log/nginx/tudianersha_error.log;

    # 静态资源
    location /css/ {
        proxy_pass http://localhost:8010/css/;
    }
    
    location /js/ {
        proxy_pass http://localhost:8010/js/;
    }

    # API接口
    location /api/ {
        proxy_pass http://localhost:8010/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 其他请求
    location / {
        proxy_pass http://localhost:8010/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# 4.2 启用配置
sudo ln -s /etc/nginx/sites-available/tudianersha /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

**HTTPS配置 (可选)**：
```bash
# 安装Certbot
sudo apt install certbot python3-certbot-nginx -y

# 自动申请并配置SSL证书
sudo certbot --nginx -d <域名>

# 自动续期
sudo certbot renew --dry-run
```

**执行状态**：
- [ ] Nginx配置完成
- [ ] SSL证书配置完成

---

### 阶段5: 系统服务配置 (推荐)

```bash
# 5.1 创建systemd服务文件
sudo nano /etc/systemd/system/tudianersha.service

# 服务配置
[Unit]
Description=Tudianersha Travel Planning System
After=network.target mysql.service

[Service]
Type=forking
User=<用户名>
WorkingDirectory=/opt/tudianersha
ExecStart=/opt/tudianersha/start.sh
ExecStop=/opt/tudianersha/stop.sh
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target

# 5.2 启用服务
sudo systemctl daemon-reload
sudo systemctl enable tudianersha
sudo systemctl start tudianersha

# 5.3 查看状态
sudo systemctl status tudianersha
```

**执行状态**：
- [ ] 系统服务创建完成
- [ ] 应用启动成功

---

## ✅ 部署验证清单

### 1. 应用访问验证

```bash
# 检查Java进程
ps aux | grep tudianersha

# 检查端口监听
sudo netstat -tlnp | grep 8010

# 检查应用日志
tail -f /opt/tudianersha/app.log
```

**浏览器访问测试**：
- [ ] HTTP访问: http://<域名或IP>
- [ ] HTTPS访问: https://<域名> (如果配置)
- [ ] 登录页面: http://<域名或IP>/login.html
- [ ] API接口: http://<域名或IP>/api/health

---

### 2. 功能测试

```
用户注册: [ ] 通过 [ ] 失败
用户登录: [ ] 通过 [ ] 失败
创建项目: [ ] 通过 [ ] 失败
AI路线生成: [ ] 通过 [ ] 失败
实时聊天: [ ] 通过 [ ] 失败
PDF导出: [ ] 通过 [ ] 失败
```

---

### 3. 性能测试

```bash
# 内存使用
free -h

# 磁盘使用
df -h

# CPU使用
top

# 应用内存占用
ps aux | grep tudianersha
```

**性能指标**：
- 应用内存占用: _______ MB
- CPU使用率: _______ %
- 响应时间: _______ ms

---

## 🔧 故障排查

### 常见问题

#### 1. 应用无法启动
```bash
# 检查Java版本
java -version

# 检查端口占用
sudo lsof -i :8010

# 查看详细错误日志
cat /opt/tudianersha/app.log
```

#### 2. 数据库连接失败
```bash
# 测试数据库连接
mysql -h <数据库地址> -u <用户名> -p

# 检查防火墙
sudo ufw status
```

#### 3. Nginx 502错误
```bash
# 检查Spring Boot是否运行
ps aux | grep tudianersha

# 检查Nginx配置
sudo nginx -t

# 查看Nginx错误日志
sudo tail -f /var/log/nginx/error.log
```

---

## 📊 监控与维护

### 日志管理

```bash
# 应用日志
tail -f /opt/tudianersha/app.log

# Nginx访问日志
tail -f /var/log/nginx/tudianersha_access.log

# Nginx错误日志
tail -f /var/log/nginx/tudianersha_error.log

# 系统日志
sudo journalctl -u tudianersha -f
```

### 备份策略

```bash
# 数据库备份
mysqldump -u tudianersha_user -p tudianersha > backup_$(date +%Y%m%d).sql

# 应用备份
tar -czf tudianersha_backup_$(date +%Y%m%d).tar.gz /opt/tudianersha
```

**建议**：
- [ ] 每天自动备份数据库
- [ ] 每周备份应用文件
- [ ] 备份文件保留30天

---

## 📞 联系信息

**部署支持**: AI Assistant  
**紧急联系**: 提供项目相关信息后获取支持

---

## 📝 部署记录

| 日期 | 操作 | 执行人 | 备注 |
|------|------|--------|------|
| 2025-12-13 | 创建部署清单 | AI Assistant | 初始版本 |
|  |  |  |  |
|  |  |  |  |

---

**状态**: [ ] 待部署 [ ] 部署中 [ ] 已完成 [ ] 已上线

**最后更新**: 2025年12月13日
