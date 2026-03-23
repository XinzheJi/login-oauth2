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
- [10. 历史故障管理接口](#10-历史故障管理接口)
- [11. AI 预测接口](#11-ai-预测接口)

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


---

## 10. 历史故障管理接口

### 10.1 分页查询历史故障

- **URL**: `/api/power/history-alarms/page`
- **Method**: `POST`
- **权限要求**: `power:alarm:history`
- **请求参数**:

```json
{
  "areaId": 11,
  "alarmResourceId": 301,
  "alarmResourceName": "主站",
  "statusList": [1, 2],
  "levelIds": [7],
  "alarmResourceType": "DEVICE",
  "alarmSource": "SYSTEM",
  "confirmStatus": "CONFIRMED",
  "alarmType": "POWER",
  "alarmStartTime": "2024-05-01 00:00:00",
  "alarmEndTime": "2024-05-07 23:59:59",
  "recoverStartTime": "2024-05-08 00:00:00",
  "recoverEndTime": "2024-05-10 23:59:59",
  "pageNum": 1,
  "pageSize": 10
}
```

- **返回示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "success": true,
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 1,
    "pages": 1,
    "list": [
      {
        "id": 42,
        "name": "电压过高",
        "code": "ALARM-001",
        "status": 2,
        "alarmType": "POWER",
        "alarmSource": "SYSTEM",
        "alarmSourceId": 501,
        "alarmResourceType": "DEVICE",
        "alarmResourceId": 301,
        "alarmResourceName": "主站电源",
        "alarmValue": "250V",
        "createTime": "2024-05-01T10:00:00",
        "reAlarmTime": "2024-05-01T13:00:00",
        "confirmStatus": "CONFIRMED",
        "confirmUser": "运维A",
        "confirmTime": "2024-05-01T11:00:00",
        "reason": "电压波动",
        "suggestion": "检查市电输入",
        "levelId": 7,
        "areaId": 11,
        "remark": "测试告警",
        "alarmResourceDetailsType": "PORT",
        "alarmResourceDetailsName": "输出口1",
        "alarmResourceDetailsId": 901,
        "alarmResourceDetailsIndex": "1",
        "reAlarmSource": "SYSTEM",
        "reAlarmSourceId": 502,
        "reAlarmValue": "220V",
        "createUser": 1001,
        "updateUser": 1002,
        "updateTime": "2024-05-01T12:00:00",
        "confirmUserId": 900,
        "alarmConfigId": 88
      }
    ]
  }
}
```

### 10.2 查询历史故障详情

- **URL**: `/api/power/history-alarms/{alarmId}`
- **Method**: `GET`
- **权限要求**: `power:alarm:history`
- **路径参数**:
  - `alarmId`: 告警ID
- **返回示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "success": true,
  "data": {
    "id": 42,
    "name": "电压过高",
    "code": "ALARM-001",
    "status": 2,
    "alarmType": "POWER",
    "alarmSource": "SYSTEM",
    "alarmSourceId": 501,
    "alarmResourceType": "DEVICE",
    "alarmResourceId": 301,
    "alarmResourceName": "主站电源",
    "alarmValue": "250V",
    "createTime": "2024-05-01T10:00:00",
    "reAlarmTime": "2024-05-01T13:00:00",
    "confirmStatus": "CONFIRMED",
    "confirmUser": "运维A",
    "confirmTime": "2024-05-01T11:00:00",
    "reason": "电压波动",
    "suggestion": "检查市电输入",
    "levelId": 7,
    "areaId": 11,
    "remark": "测试告警",
    "alarmResourceDetailsType": "PORT",
    "alarmResourceDetailsName": "输出口1",
    "alarmResourceDetailsId": 901,
    "alarmResourceDetailsIndex": "1",
    "reAlarmSource": "SYSTEM",
    "reAlarmSourceId": 502,
    "reAlarmValue": "220V",
    "createUser": 1001,
    "updateUser": 1002,
    "updateTime": "2024-05-01T12:00:00",
    "confirmUserId": 900,
    "alarmConfigId": 88
  }
}
```



---

## 11. AI 预测接口

### 11.1 老化预测

- **URL**: `/api/tool/predict/aging`
- **Method**: `POST`
- **权限要求**: 已登录用户（建议结合业务权限控制）
- **可选请求头**: `X-Request-ID`（用于链路追踪）
- **请求参数（JSON）**:

```json
{
  "deviceId": "SW-0001",
  "historyStart": "2024-05-01T00:00:00Z",
  "historyEnd": "2024-05-07T23:59:59Z",
  "historyLength": 336,
  "minimumHistorySize": 120,
  "requestTimestamp": "2024-05-08T00:00:00Z"
}
```

- 字段说明：
  - `deviceId` 必填，设备ID
  - `historyStart` 可选，历史窗口开始时间（ISO-8601）
  - `historyEnd` 可选，历史窗口结束时间（ISO-8601，若同时提供则不得早于 `historyStart`）
  - `historyLength` 可选，历史查询行数上限（>0）
  - `minimumHistorySize` 可选，最小历史样本量（>0，若不足将返回错误）
  - `requestTimestamp` 可选，请求时间戳（用于复现/回溯）

- **返回示例**:

```json
{
  "code": 200,
  "message": "OK",
  "success": true,
  "data": {
    "success": true,
    "result": {
      "device_id": "SW-0001",
      "health_index": 0.78,
      "tte_hours": 240.5,
      "rul_hours": 180.0,
      "risk_level": "MEDIUM",
      "contributors": [
        { "metric": "memoryUsage", "weight": 0.32 },
        { "metric": "temperature", "weight": 0.27 }
      ],
      "prediction_time": "2024-05-08T00:00:00Z"
    },
    "message": "ok"
  }
}
```

> 校验规则：当同时提供 `historyStart` 与 `historyEnd` 时，必须满足 `historyEnd` ≥ `historyStart`，否则返回 400 错误。

> 说明：接口采用统一响应封装（HTTP 200 + 业务码），当外部推理服务不可用或返回错误时，`code` 将为 4xx/5xx，`message` 含错误信息。

---

### 11.2 故障趋势预测

- **URL**: `/api/tool/predict/fault`
- **Method**: `POST`
- **权限要求**: 已登录用户（建议结合业务权限控制）
- **可选请求头**: `X-Request-ID`（用于链路追踪）
- **请求参数（JSON）**:

```json
{
  "deviceId": "SW-0001",
  "historyStart": "2024-05-01T00:00:00Z",
  "historyEnd": "2024-05-07T23:59:59Z",
  "historyLength": 336,
  "minimumHistorySize": 120,
  "requestTimestamp": "2024-05-08T00:00:00Z"
}
```

- 字段说明：同“11.1 老化预测”请求参数。

- **返回示例**:

```json
{
  "code": 200,
  "message": "OK",
  "success": true,
  "data": {
    "success": true,
    "result": {
      "device_id": "SW-0001",
      "prob_7d": 0.62,
      "prob_14d": 0.71,
      "prob_30d": 0.80,
      "risk_level": "HIGH",
      "explanation": "同批次与软件版本先验风险较高，近窗变点概率上升",
      "contributors": [
        { "metric": "batch_risk", "weight": 0.34 },
        { "metric": "sw_version_prior", "weight": 0.28 },
        { "metric": "icmpLoss", "weight": 0.18 }
      ],
      "prediction_time": "2024-05-08T00:00:00Z"
    },
    "message": "ok"
  }
}
```

> 说明：接口返回为统一封装结构，`data.success` 表示外部推理服务是否成功；`result` 为核心预测结果，字段命名以下划线风格输出。


---

### 11.3 批量预测

- URL: `/api/tool/predict/batch`
- Method: `POST`
- 权限要求: 已登录用户
- 请求参数（JSON）:

```json
{
  "deviceIds": ["SW-0001", "SW-0002"],
  "historyStart": "2024-05-01T00:00:00Z",
  "historyEnd": "2024-05-07T23:59:59Z",
  "historyLength": 336,
  "minimumHistorySize": 64,
  "requestTimestamp": "2024-05-08T00:00:00Z",
  "mode": "default"
}
```

- 返回示例:

```json
{
  "code": 200,
  "message": "OK",
  "success": true,
  "data": {
    "results": [
      {
        "device_id": "SW-0001",
        "aging": { "success": true, "result": { "health_index": 0.78 } },
        "fault": { "success": true, "result": { "prob_7d": 0.62 } }
      }
    ],
    "failed": { "SW-0002": "历史样本不足：10/64" },
    "failed_count": 1,
    "cost_ms": 135
  }
}
```

> 说明：`failed`/`failed_count` 合并了本地数据预检失败与外部推理服务失败条目；当 `historyEnd` < `historyStart` 返回 400。

- 兼容性说明：
  - 当外部批量接口校验更严格（如要求历史长度=模型 input_length）导致 4xx（如 422）时，后端将按配置回退为“逐设备单发”再汇总返回，确保 `results` 中包含 `aging`/`fault` 明细。
  - 回退开关：由后端配置管理（默认开启），不影响调用方协议。

---

### 11.4 历史预测结果查询

- URL: `/api/tool/predict/history`
- Method: `GET`
- 权限要求: 已登录用户
- 请求参数（Query）:
  - `deviceId` 必填，设备ID
  - `type` 可选，`AGING` 或 `FAULT`
  - `start` 可选，ISO-8601 起始时间
  - `end` 可选，ISO-8601 截止时间（若同时提供则不得早于 `start`）
  - `page` 可选，页码（默认 1）
  - `size` 可选，页大小（默认 20，最大 200）
  - `asc` 可选，是否按预测时间升序（默认 true）

- 返回示例:

```json
{
  "code": 200,
  "message": "OK",
  "success": true,
  "data": {
    "page": 1,
    "size": 20,
    "total": 128,
    "items": [
      {
        "deviceId": "SW-0001",
        "predictType": "FAULT",
        "predictionTime": "2024-05-08T00:00:00Z",
        "createdAt": "2024-05-08T00:00:00Z",
        "result": { "success": true, "result": { "prob_7d": 0.62, "prob_14d": 0.71, "prob_30d": 0.80 } }
      }
    ]
  }
}
```

> 说明：`result` 字段为保存时的完整预测响应 JSON（AGING 或 FAULT）。

---

### 11.5 运维与指标（仅供观测）

- 说明：以下端点用于运维观测，需要携带认证；仅返回已经产生过的自定义指标。
- **URL**: `/actuator/metrics`
- **示例指标**:
  - `ai_predict.batch.success` 批量任务成功次数（定时任务记录）
  - `ai_predict.batch.failed` 批量任务失败次数（定时任务记录）
  - `ai_predict.batch.duration` 批量任务耗时分布（定时任务记录）
  - `ai_predict.results.saved` 结果入库计数（保存时记录）
> 说明：若指标名返回 404，表示该指标尚未产生记录（未被上报过）。




API 文档（折线图使用指南）

- 服务基础信息
  - 运行：uvicorn app.main:fastapi_app --host 0.0.0.0 --port 8000
  - 文档：http://<host>:<port>/api/docs
  - 路径前缀：/api/v1
- 接口响应适配：在 vue/src/api/ 里给 /api/tool/predict/aging 和 /fault 的返回类型加上 forecast 数组（元素含 timestamp、temperature，老化多
  health_index，故障多 fault_probability）。timemoe 暂未返回 forecast_interval_hours/forecast_horizon_hours，后续若补上同样透传即可。
  - 折线图数据源：
    - 老化页：x 轴用 forecast[].timestamp；y 轴两条线：温度(temperature)、健康指数(health_index)。
    - 故障页：x 轴同上；y 轴两条线：温度、故障概率(fault_probability，可乘 100% 显示百分比)；把 prob_7d/14d/30d 作为标注/tooltip 参考。
  - 判空和降级：forecast 可能为空或缺失，先判空，为空时显示“暂无预测序列”并只展示当前指标，避免图表报错。


  修改前端：
  1) 更新 /api/tool/predict/aging 和 /fault 的响应类型，新增字段 forecast（数组，元素包含 timestamp、temperature，aging 还包含 health_index，fault 包
     含 fault_probability）。
  2) 老化页面的折线图使用 resp.data.result.forecast：
    - x 轴 timestamp
    - y 轴两条线：temperature、health_index
  3) 故障页面折线图：
    - x 轴 timestamp
    - y 轴两条线：temperature、fault_probability（0~1，显示可乘 100%）
    - 在 tooltip 或图注里标出 prob_7d/prob_14d/prob_30d
  4) forecast 缺失时展示“暂无预测序列”，不要渲染图，避免报错；其他数据照旧显示。
  5) 如果后端后续返回 forecast_interval_hours/forecast_horizon_hours，则一并透传，并可用 interval 控制刻度显示。

---

## 状态码说明

- `200 OK`: 请求成功
- `400 Bad Request`: 请求参数错误
- `401 Unauthorized`: 未认证或认证失败
- `403 Forbidden`: 没有权限访问
- `404 Not Found`: 资源不存在
- `500 Internal Server Error`: 服务器内部错误 
