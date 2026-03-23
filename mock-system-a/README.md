# 系统A接口模拟服务

本项目实现了一个模拟系统A的OAuth2授权服务和受保护API接口的Mock Server，用于系统B开发阶段的测试。

## 功能特性

- 模拟OAuth2客户端模式授权
- 模拟系统A的受保护接口
- 接口路径、参数、鉴权机制与实际系统保持一致

## 快速开始

### 环境要求

- JDK 11+
- Maven 3.6+

### 构建与运行

```bash
# 构建项目
mvn clean package

# 运行服务
java -jar target/mock-system-a-0.0.1-SNAPSHOT.jar
```

服务默认运行在 `http://localhost:8080`

## 接口说明

### 1. 获取Token接口

```
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials&client_id=b-system-client&client_secret=b-system-secret
```

成功响应示例：

```json
{
  "access_token": "mocked-access-token-123456",
  "token_type": "bearer",
  "expires_in": 3600,
  "scope": "read"
}
```

### 2. 受保护资源接口

```
GET /api/protected/data
Authorization: Bearer mocked-access-token-123456
```

成功响应示例：

```json
{
  "status": "success",
  "data": {
    "name": "Mocked Resource",
    "value": "This is protected data"
  }
}
```

## 配置说明

通过 `application.yml` 可以自定义以下配置：

```yaml
mock:
  oauth:
    token:
      value: mocked-access-token-123456  # 模拟的access_token值
      expires-in: 3600                   # token有效期（秒）
    client-id: b-system-client           # 预设的客户端ID
    client-secret: b-system-secret       # 预设的客户端密钥
```

## 系统B集成说明

系统B需要将OAuth2客户端配置指向此模拟服务的地址，例如：

```
oauth2.token-url=http://localhost:8080/oauth/token
oauth2.client-id=b-system-client
oauth2.client-secret=b-system-secret
api.base-url=http://localhost:8080
``` 