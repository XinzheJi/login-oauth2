# 系统 B API 接口文档

本文档详细描述了系统 B 的所有 REST API 接口，包括请求方法、URL、参数说明、权限要求和返回数据。

## 目录
- [1. 认证接口](#1-认证接口)
- [2. 租户管理接口](#2-租户管理接口)
- [3. 用户管理接口](#3-用户管理接口)
- [4. 角色管理接口](#4-角色管理接口)
- [5. 权限管理接口](#5-权限管理接口)
- [6. 系统 A 集成接口](#6-系统-a-集成接口)
- [7. 健康检查接口](#7-健康检查接口)
- [8. 电源台账管理接口](#8-电源台账管理接口)
- [9. 电源告警管理接口](#9-电源告警管理接口)

---

## 1. 认证接口

### 1.1 用户登录

- **URL**: `/api/auth/login`
- **Method**: `POST`
- **权限要求**: 无需权限
- **请求参数**:

```json
{
  "username": "用户名",
  "password": "密码",
  "tenantCode": "租户编码"
}
```

- **返回示例**:

```json
{
  "token": "JWT令牌",
  "userId": 1,
  "username": "admin",
  "tenantId": 1,
  "tenantCode": "default"
}
```

### 1.2 获取当前用户信息

- **URL**: `/api/auth/user/info`
- **Method**: `GET`
- **权限要求**: 已登录用户
- **请求参数**: 无
- **返回示例**:

```json
{
  "id": 1,
  "username": "admin",
  "tenantId": 1,
  "status": 1
}
```

### 1.3 退出登录

- **URL**: `/api/auth/logout`
- **Method**: `POST`
- **权限要求**: 已登录用户
- **请求参数**: 无
- **返回示例**: HTTP 200 OK

---

## 2. 租户管理接口

### 2.1 获取所有租户列表

- **URL**: `/api/tenants`
- **Method**: `GET`
- **权限要求**: `tenant:view`
- **请求参数**: 无
- **返回示例**:

```json
[
  {
    "id": 1,
    "tenantCode": "default",
    "tenantName": "默认租户",
    "status": "active"
  },
  {
    "id": 2,
    "tenantCode": "TENANT_A",
    "tenantName": "租户A",
    "status": "active"
  }
]
```

### 2.2 根据ID获取租户

- **URL**: `/api/tenants/{id}`
- **Method**: `GET`
- **权限要求**: `tenant:view`
- **路径参数**:
  - `id`: 租户ID
- **返回示例**:

```json
{
  "id": 1,
  "tenantCode": "default",
  "tenantName": "默认租户",
  "status": "active"
}
```

### 2.3 创建新租户

- **URL**: `/api/tenants`
- **Method**: `POST`
- **权限要求**: `tenant:create`
- **请求参数**:

```json
{
  "tenantName": "租户名称",
  "tenantCode": "租户编码",
  "status": "active"
}
```

- **返回示例**:

```json
{
  "id": 3,
  "tenantCode": "TENANT_B",
  "tenantName": "租户B",
  "status": "active"
}
```

### 2.4 更新租户

- **URL**: `/api/tenants/{id}`
- **Method**: `PUT`
- **权限要求**: `tenant:update`
- **路径参数**:
  - `id`: 租户ID
- **请求参数**:

```json
{
  "tenantName": "更新后的租户名称",
  "status": "active"
}
```

- **返回示例**:

```json
{
  "id": 1,
  "tenantCode": "default",
  "tenantName": "更新后的租户名称",
  "status": "active"
}
```

### 2.5 删除租户

- **URL**: `/api/tenants/{id}`
- **Method**: `DELETE`
- **权限要求**: `tenant:delete`
- **路径参数**:
  - `id`: 租户ID
- **返回示例**: HTTP 200 OK

### 2.6 更新租户状态

- **URL**: `/api/tenants/{id}/status`
- **Method**: `PUT`
- **权限要求**: `tenant:update`
- **路径参数**:
  - `id`: 租户ID
- **请求参数**:

```json
{
  "status": "active或inactive"
}
```

- **返回示例**:

```json
{
  "id": 1,
  "tenantCode": "default",
  "tenantName": "默认租户",
  "status": "active"
}
```

---

## 3. 用户管理接口

### 3.1 获取所有用户列表

- **URL**: `/api/users`
- **Method**: `GET`
- **权限要求**: `user:view`
- **请求参数**: 无
- **返回示例**[:
]()
```json
[
  {
    "id": 1,
    "username": "admin",
    "tenantId": 1,
    "status": 1
  }
]
```

### 3.2 根据ID获取用户

- **URL**: `/api/users/{id}`
- **Method**: `GET`
- **权限要求**: `user:view`
- **路径参数**:
  - `id`: 用户ID
- **返回示例**:

```json
{
  "id": 1,
  "username": "admin",
  "tenantId": 1,
  "status": 1
}
```

### 3.3 创建新用户

- **URL**: `/api/users`
- **Method**: `POST`
- **权限要求**: `user:create`
- **请求参数**:

```json
{
  "username": "user01",
  "password": "password",
  "tenantId": 1,
  "status": 1
}
```

- **返回示例**:

```json
{
  "id": 2,
  "username": "user01",
  "tenantId": 1,
  "status": 1
}
```

### 3.4 更新用户

- **URL**: `/api/users/{id}`
- **Method**: `PUT`
- **权限要求**: `user:update`
- **路径参数**:
  - `id`: 用户ID
- **请求参数**:

```json
{
  "username": "updateduser",
  "password": "newpassword",
  "status": 1
}
```

- **返回示例**:

```json
{
  "id": 1,
  "username": "updateduser",
  "tenantId": 1,
  "status": 1
}
```

### 3.5 删除用户

- **URL**: `/api/users/{id}`
- **Method**: `DELETE`
- **权限要求**: `user:delete`
- **路径参数**:
  - `id`: 用户ID
- **返回示例**: HTTP 200 OK

### 3.6 根据租户ID获取用户列表

- **URL**: `/api/users/by-tenant/{tenantId}`
- **Method**: `GET`
- **权限要求**: `user:view`
- **路径参数**:
  - `tenantId`: 租户ID
- **返回示例**:

```json
[
  {
    "id": 1,
    "username": "admin",
    "tenantId": 1,
    "status": 1
  }
]
```

### 3.7 为用户分配角色

- **URL**: `/api/users/{userId}/roles/{roleId}`
- **Method**: `POST`
- **权限要求**: `role:update`
- **路径参数**:
  - `userId`: 用户ID
  - `roleId`: 角色ID
- **返回示例**: HTTP 200 OK

### 3.8 移除用户角色

- **URL**: `/api/users/{userId}/roles/{roleId}`
- **Method**: `DELETE`
- **权限要求**: `user:delete`
- **路径参数**:
  - `userId`: 用户ID
  - `roleId`: 角色ID
- **返回示例**: HTTP 200 OK

---

## 4. 角色管理接口

### 4.1 获取所有角色列表

- **URL**: `/api/roles`
- **Method**: `GET`
- **权限要求**: `role:view`
- **请求参数**: 无
- **返回示例**:

```json
[
  {
    "id": 1,
    "name": "管理员",
    "code": "ADMIN",
    "tenantId": 1,
    "description": "系统管理员"
  }
]
```

### 4.2 根据ID获取角色

- **URL**: `/api/roles/{id}`
- **Method**: `GET`
- **权限要求**: `role:view`
- **路径参数**:
  - `id`: 角色ID
- **返回示例**:

```json
{
  "id": 1,
  "name": "管理员",
  "code": "ADMIN",
  "tenantId": 1,
  "description": "系统管理员"
}
```

### 4.3 创建新角色

- **URL**: `/api/roles`
- **Method**: `POST`
- **权限要求**: `role:create`
- **请求参数**:

```json
{
  "name": "普通用户",
  "code": "USER",
  "tenantId": 1,
  "description": "普通用户角色"
}
```

- **返回示例**:

```json
{
  "id": 2,
  "name": "普通用户",
  "code": "USER",
  "tenantId": 1,
  "description": "普通用户角色"
}
```

### 4.4 更新角色

- **URL**: `/api/roles/{id}`
- **Method**: `PUT`
- **权限要求**: `role:update`
- **路径参数**:
  - `id`: 角色ID
- **请求参数**:

```json
{
  "name": "更新后的角色名称",
  "description": "更新后的描述"
}
```

- **返回示例**:

```json
{
  "id": 1,
  "name": "更新后的角色名称",
  "code": "ADMIN",
  "tenantId": 1,
  "description": "更新后的描述"
}
```

### 4.5 删除角色

- **URL**: `/api/roles/{id}`
- **Method**: `DELETE`
- **权限要求**: `role:delete`
- **路径参数**:
  - `id`: 角色ID
- **返回示例**: HTTP 200 OK

### 4.6 根据用户ID获取角色列表

- **URL**: `/api/roles/by-user/{userId}`
- **Method**: `GET`
- **权限要求**: `role:view`
- **路径参数**:
  - `userId`: 用户ID
- **返回示例**:

```json
[
  {
    "id": 1,
    "name": "管理员",
    "code": "ADMIN",
    "tenantId": 1,
    "description": "系统管理员"
  }
]
```

### 4.7 获取角色的权限列表

- **URL**: `/api/roles/{roleId}/permissions`
- **Method**: `GET`
- **权限要求**: `role:view`
- **路径参数**:
  - `roleId`: 角色ID
- **返回示例**:

```json
[
  {
    "id": 1,
    "name": "用户查询",
    "code": "user:view",
    "urlPattern": "/api/users/**",
    "method": "GET",
    "tenantId": 1
  }
]
```

### 4.8 为角色分配权限

- **URL**: `/api/roles/{roleId}/permissions`
- **Method**: `POST`
- **权限要求**: `permission:assign`
- **路径参数**:
  - `roleId`: 角色ID
- **请求参数**:

```json
{
  "permissionIds": [1, 2, 3, 4]
}
```

- **返回示例**: HTTP 200 OK

---

## 5. 权限管理接口

### 5.1 获取所有权限列表

- **URL**: `/api/permissions`
- **Method**: `GET`
- **权限要求**: `permission:view`
- **请求参数**: 无
- **返回示例**:

```json
[
  {
    "id": 1,
    "name": "用户查询",
    "code": "user:view",
    "urlPattern": "/api/users/**",
    "method": "GET",
    "tenantId": 1
  }
]
```

### 5.2 根据ID获取权限

- **URL**: `/api/permissions/{id}`
- **Method**: `GET`
- **权限要求**: `permission:view`
- **路径参数**:
  - `id`: 权限ID
- **返回示例**:

```json
{
  "id": 1,
  "name": "用户查询",
  "code": "user:view",
  "urlPattern": "/api/users/**",
  "method": "GET",
  "tenantId": 1
}
```

### 5.3 创建新权限

- **URL**: `/api/permissions`
- **Method**: `POST`
- **权限要求**: `permission:create`
- **请求参数**:

```json
{
  "name": "租户查询",
  "code": "tenant:view",
  "urlPattern": "/api/tenants/**",
  "method": "GET",
  "tenantId": 1
}
```

- **返回示例**:

```json
{
  "id": 2,
  "name": "租户查询",
  "code": "tenant:view",
  "urlPattern": "/api/tenants/**",
  "method": "GET",
  "tenantId": 1
}
```

### 5.4 更新权限

- **URL**: `/api/permissions/{id}`
- **Method**: `PUT`
- **权限要求**: `permission:update`
- **路径参数**:
  - `id`: 权限ID
- **请求参数**:

```json
{
  "name": "更新后的权限名称",
  "urlPattern": "/api/users/updated/**"
}
```

- **返回示例**:

```json
{
  "id": 1,
  "name": "更新后的权限名称",
  "code": "user:view",
  "urlPattern": "/api/users/updated/**",
  "method": "GET",
  "tenantId": 1
}
```

### 5.5 删除权限

- **URL**: `/api/permissions/{id}`
- **Method**: `DELETE`
- **权限要求**: `permission:delete`
- **路径参数**:
  - `id`: 权限ID
- **返回示例**: HTTP 200 OK

### 5.6 根据角色ID获取权限列表

- **URL**: `/api/permissions/by-role/{roleId}`
- **Method**: `GET`
- **权限要求**: `permission:view`
- **路径参数**:
  - `roleId`: 角色ID
- **返回示例**:

```json
[
  {
    "id": 1,
    "name": "用户查询",
    "code": "user:view",
    "urlPattern": "/api/users/**",
    "method": "GET",
    "tenantId": 1
  }
]
```

### 5.7 根据用户ID获取权限列表

- **URL**: `/api/permissions/by-user/{userId}`
- **Method**: `GET`
- **权限要求**: `permission:view`
- **路径参数**:
  - `userId`: 用户ID
- **返回示例**:

```json
[
  {
    "id": 1,
    "name": "用户查询",
    "code": "user:view",
    "urlPattern": "/api/users/**",
    "method": "GET",
    "tenantId": 1
  }
]
```

### 5.8 获取所有角色列表（用于权限分配）

- **URL**: `/api/permissions/roles`
- **Method**: `GET`
- **权限要求**: `permission:view`
- **请求参数**: 无
- **返回示例**:

```json
[
  {
    "id": 1,
    "name": "管理员",
    "code": "ADMIN",
    "tenantId": 1,
    "description": "系统管理员"
  }
]
```

### 5.9 为角色分配权限

- **URL**: `/api/permissions/assign`
- **Method**: `POST`
- **权限要求**: `permission:assign`
- **请求参数**:

```json
{
  "roleId": 1,
  "permissionIds": [1, 2, 3]
}
```

- **返回示例**: HTTP 200 OK

### 5.10 获取角色的权限列表

- **URL**: `/api/permissions/role/{roleId}`
- **Method**: `GET`
- **权限要求**: `permission:view`
- **路径参数**:
  - `roleId`: 角色ID
- **返回示例**:

```json
[
  {
    "id": 1,
    "name": "用户查询",
    "code": "user:view",
    "urlPattern": "/api/users/**",
    "method": "GET",
    "tenantId": 1
  }
]
```

### 5.11 获取权限的角色列表

- **URL**: `/api/permissions/{permissionId}/roles`
- **Method**: `GET`
- **权限要求**: `permission:view`
- **路径参数**:
  - `permissionId`: 权限ID
- **返回示例**:

```json
[
  {
    "id": 1,
    "name": "管理员",
    "code": "ADMIN",
    "tenantId": 1,
    "description": "系统管理员"
  }
]
```

### 5.12 为权限分配角色

- **URL**: `/api/permissions/{permissionId}/roles`
- **Method**: `POST`
- **权限要求**: `permission:assign`
- **路径参数**:
  - `permissionId`: 权限ID
- **请求参数**:

```json
{
  "roleIds": [1, 2]
}
```

- **返回示例**: HTTP 200 OK

### 5.13 批量为权限分配角色

- **URL**: `/api/permissions/batch-assign`
- **Method**: `POST`
- **权限要求**: `permission:assign`
- **请求参数**:

```json
{
  "permissionIds": [1, 2],
  "roleIds": [1, 2],
  "mode": "replace"
}
```

- **返回示例**: HTTP 200 OK

### 5.14 移除角色权限

- **URL**: `/api/permissions/remove`
- **Method**: `DELETE`
- **权限要求**: `permission:remove`
- **请求参数**:
  - `roleId`: 角色ID
  - `permissionId`: 权限ID
- **返回示例**: HTTP 200 OK

---

## 6. 系统 A 集成接口

### 6.1 获取系统A的受保护数据

- **URL**: `/api/system-a/protected-data`
- **Method**: `GET`
- **权限要求**: `ROLE_USER`角色
- **请求参数**: 无
- **返回示例**:

```json
{
  "data": {
    "message": "这是系统A的受保护数据",
    "timestamp": 1620000000000
  }
}
```

---

## 7. 健康检查接口

### 7.1 系统健康检查

- **URL**: `/api/health`
- **Method**: `GET`
- **权限要求**: 无需权限
- **请求参数**: 无
- **返回示例**:

```json
{
  "status": "UP",
  "service": "system-b",
  "timestamp": 1620000000000
}
```

---

## 8. 电源台账管理接口

### 8.1 分页查询电源设备

- **URL**: `/api/power/devices`
- **Method**: `GET`
- **权限要求**: `power:device:view`
- **请求参数**:
  - `deviceName`: 设备名称（模糊查询）
  - `ipAddress`: IP地址（模糊查询）
  - `location`: 设备位置（模糊查询）
  - `deviceType`: 设备类型
  - `installDateStart`: 安装开始日期（格式：yyyy-MM-dd）
  - `installDateEnd`: 安装结束日期（格式：yyyy-MM-dd）
  - `pageSize`: 每页记录数（默认10）
  - `pageNum`: 当前页码（默认1）
- **返回示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 100,
    "pages": 10,
    "list": [
      {
        "id": 1,
        "deviceName": "UPS-01",
        "ipAddress": "192.168.1.10",
        "location": "机房A-01",
        "deviceType": "UPS",
        "installDate": "2023-05-10 10:00:00",
        "remark": "主要电源设备",
        "createTime": "2023-05-10 10:00:00",
        "updateTime": "2023-05-10 10:00:00",
        "tenantId": 1,
        "tenantName": "默认租户"
      }
    ]
  },
  "success": true
}
```

### 8.2 根据ID查询电源设备

- **URL**: `/api/power/devices/{id}`
- **Method**: `GET`
- **权限要求**: `power:device:view`
- **路径参数**:
  - `id`: 设备ID
- **返回示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "deviceName": "UPS-01",
    "ipAddress": "192.168.1.10",
    "location": "机房A-01",
    "deviceType": "UPS",
    "installDate": "2023-05-10 10:00:00",
    "remark": "主要电源设备",
    "createTime": "2023-05-10 10:00:00",
    "updateTime": "2023-05-10 10:00:00",
    "tenantId": 1,
    "tenantName": "默认租户"
  },
  "success": true
}
```

### 8.3 创建电源设备

- **URL**: `/api/power/devices`
- **Method**: `POST`
- **权限要求**: `power:device:create`
- **请求参数**:

```json
{
  "deviceName": "UPS-02",
  "ipAddress": "192.168.1.11",
  "location": "机房A-02",
  "deviceType": "UPS",
  "installDate": "2023-05-11T10:00:00",
  "remark": "备用电源设备"
}
```

- **返回示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true,
  "success": true
}
```

### 8.4 更新电源设备

- **URL**: `/api/power/devices/{id}`
- **Method**: `PUT`
- **权限要求**: `power:device:update`
- **路径参数**:
  - `id`: 设备ID
- **请求参数**:

```json
{
  "deviceName": "UPS-02-更新",
  "ipAddress": "192.168.1.11",
  "location": "机房A-03",
  "deviceType": "UPS",
  "installDate": "2023-05-11T10:00:00",
  "remark": "更新后的备注"
}
```

- **返回示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true,
  "success": true
}
```

### 8.5 删除电源设备

- **URL**: `/api/power/devices/{id}`
- **Method**: `DELETE`
- **权限要求**: `power:device:delete`
- **路径参数**:
  - `id`: 设备ID
- **返回示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true,
  "success": true
}
```

---

## 9. 电源告警管理接口

### 9.1 获取所有告警类型

- **URL**: `/api/power/alarm-types`
- **Method**: `GET`
- **权限要求**: `power:alarm`
- **请求参数**: 无
- **返回示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "code": "current",
      "name": "电流异常"
    },
    {
      "code": "temperature",
      "name": "温度异常"
    },
    {
      "code": "humidity",
      "name": "湿度异常"
    },
    {
      "code": "door_status",
      "name": "门禁状态"
    },
    {
      "code": "smoke_detection",
      "name": "烟雾检测"
    }
  ],
  "success": true
}
```

### 9.2 获取所有告警级别

- **URL**: `/api/power/alarm-levels`
- **Method**: `GET`
- **权限要求**: `power:alarm`
- **请求参数**: 无
- **返回示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "code": "INFO",
      "name": "信息",
      "level": 1,
      "color": "#2196F3"
    },
    {
      "code": "WARNING",
      "name": "警告",
      "level": 2,
      "color": "#FF9800"
    },
    {
      "code": "CRITICAL",
      "name": "严重",
      "level": 3,
      "color": "#F44336"
    },
    {
      "code": "EMERGENCY",
      "name": "紧急",
      "level": 4,
      "color": "#9C27B0"
    }
  ],
  "success": true
}
```

### 9.3 获取告警统计数据

- **URL**: `/api/power/alarms/statistics`
- **Method**: `GET`
- **权限要求**: `power:alarm:statistics`
- **请求参数**:
  - `alarmTimeStart`: 告警开始时间（格式：yyyy-MM-dd HH:mm:ss）
  - `alarmTimeEnd`: 告警结束时间（格式：yyyy-MM-dd HH:mm:ss）
- **返回示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalAlarms": 150,
    "unprocessedAlarms": 35,
    "resolvedAlarms": 115,
    "alarmLevels": [
      { "name": "信息", "value": 50 },
      { "name": "警告", "value": 60 },
      { "name": "严重", "value": 30 },
      { "name": "紧急", "value": 10 }
    ],
    "alarmTypes": [
      { "name": "电压异常", "value": 70 },
      { "name": "电流异常", "value": 40 },
      { "name": "温度异常", "value": 25 },
      { "name": "其他", "value": 15 }
    ],
    "alarmTrends": [
      { "date": "2023-05-01", "count": 10 },
      { "date": "2023-05-02", "count": 15 },
      { "date": "2023-05-03", "count": 12 }
    ],
    "alarmLocations": [
      { "name": "机房A", "value": 80 },
      { "name": "机房B", "value": 50 },
      { "name": "机房C", "value": 20 }
    ]
  },
  "success": true
}
```

## 状态码说明

- `200 OK`: 请求成功
- `400 Bad Request`: 请求参数错误
- `401 Unauthorized`: 未认证或认证失败
- `403 Forbidden`: 没有权限访问
- `404 Not Found`: 资源不存在
- `500 Internal Server Error`: 服务器内部错误 