
# 系统 A 接口模拟方案（用于系统 B 开发阶段）

## 背景

在系统 B 的开发初期，系统 A 尚不能提供稳定、可用的接口服务进行联调。为保证开发进度，需设计一套模拟方案，仿真系统 A 的接口行为及 OAuth2 鉴权机制，以便系统 B 能独立完成开发与初步测试。

## 模拟方案目标

- 构建一个本地或开发环境可部署的 **Mock Server**；
- 模拟系统 A 的 OAuth2 客户端模式下的鉴权流程；
- 模拟系统 A 的资源接口（如 `/api/protected/data`）响应；
- 保持接口路径、参数、鉴权机制与实际一致，便于后续无缝切换；

## 技术选型

| 模块 | 技术推荐 | 说明 |
|------|----------|------|
| 模拟服务框架 | [WireMock](http://wiremock.org/) / MockServer / Spring Boot Mock Controller | 可快速模拟 REST 接口和 OAuth2 Token 发放 |
| 语言 | Java / Kotlin / Node.js | 视团队技术栈选择 |
| 接口定义 | Swagger/OpenAPI | 与系统 A 保持一致，便于维护 |

## 目录结构示例（Spring Boot + Mock Controller）

```
mock-system-a/
├── src/
│   ├── main/
│   │   ├── java/com/mock/a/
│   │   │   ├── controller/
│   │   │   │   ├── TokenController.java       # 模拟 OAuth2 Token 获取
│   │   │   │   └── ProtectedApiController.java# 模拟受保护接口
│   │   │   └── MockApplication.java
│   └── resources/
│       └── application.yml
├── pom.xml
```

## 关键实现

### 1. 模拟 Token 获取接口

#### 请求

```
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
client_id=b-system-client
client_secret=b-system-secret
```

#### 响应

```json
{
  "access_token": "mocked-access-token-123456",
  "token_type": "bearer",
  "expires_in": 3600,
  "scope": "read"
}
```

> 实现：检查 `client_id` 和 `client_secret` 是否为预设值，返回固定 Token。

### 2. 模拟受保护接口

#### 请求

```
GET /api/protected/data
Authorization: Bearer mocked-access-token-123456
```

#### 响应

```json
{
  "status": "success",
  "data": {
    "name": "Mocked Resource",
    "value": "This is protected data"
  }
}
```

> 实现：校验请求头中 `Authorization` 是否为合法模拟 Token。

### 3. 自定义配置

```yaml
# application.yml
mock:
  oauth:
    token:
      value: mocked-access-token-123456
      expires-in: 3600
    client-id: b-system-client
    client-secret: b-system-secret
```

## 启动与使用

1. 本地运行 `MockApplication.java`；
2. 系统 B 配置 token 请求地址为 `http://localhost:8080/oauth/token`；
3. 系统 B 资源访问地址为 `http://localhost:8080/api/protected/data`；
4. 可使用 Postman 或系统 B 模拟完整调用链；

## 好处

- 无需依赖系统 A，可独立开发系统 B；
- 后期切换真实接口只需替换 baseUrl；
- 可模拟错误响应、超时等异常场景，便于健壮性测试；

## 建议

- 保持与系统 A Swagger 文档中接口定义一致；
- 支持多种场景：成功 / 鉴权失败 / 权限不足 / 接口异常；
- 提供切换开关（如 profile）控制是否使用 mock；

## 示例仓库结构建议

可将此 mock 模块作为系统 B 的一个子模块，如：

```
b-system/
├── b-system-backend/
├── b-system-frontend/
└── b-system-mock-a/
```
