
# 配网通信电源管理与告警系统设计方案

## 一、现有数据支持的功能

| 功能模块 | 子功能 | 是否支持 | 说明 |
|----------|--------|----------|------|
| 配网通信电源统计分析 | 按区域、时间统计运行指标 | ✅ 支持 | 需要关联设备区域信息（可扩展设备台账） |
| 告警统计 | 告警数量、类型、时间统计 | ✅ 支持 | `is_alarm`, `alarm_level`, `alarm_desc` 字段可用 |
| 告警管理 | 接收告警（欠压、过压等） | ✅ 支持 | 当前已可用字段支持 |
| 告警管理 | 记录告警参数、时间 | ✅ 支持 | `monitor_name`, `value`, `alarm_time` 已包含 |
| 告警类型定义 | 如电池健康度类 | ✅ 支持 | 可将“电池健康度”作为一个 `monitor_name` 进行记录 |
| 电源台账管理 | 新增、修改、删除 | ✅ 支持（需额外加一张设备表） | 当前 `power_monitor_record` 中的 `device_id` 可关联台账表 |
| 多条件查询 | IP、设备名、区域 | ✅ 支持（需加设备表） | 多条件查询需额外设备信息表 |
| 远程核容记录 | 执行时间、对比、曲线图 | ✅ 部分支持 | 可扩展记录表，添加核容任务与电量记录点即可 |
| 电池健康度趋势 | 记录趋势曲线 | ✅ 支持 | 使用 `monitor_name = '电池健康度'`，随时间记录曲线 |

---

## 二、推荐三表结构设计

### 1. 设备台账表（`power_device`）

```sql
CREATE TABLE power_device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_name VARCHAR(100),
    ip_address VARCHAR(50),
    location VARCHAR(255),
    device_type VARCHAR(50),
    install_date DATETIME,
    remark TEXT
);
```

### 2. 电源监控记录表（`power_monitor_record`）

```sql
CREATE TABLE power_monitor_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id BIGINT,
    monitor_name VARCHAR(100),
    data_type VARCHAR(50),
    value FLOAT,
    threshold_upper FLOAT,
    threshold_lower FLOAT,
    is_alarm BOOLEAN,
    alarm_level VARCHAR(50),
    alarm_desc TEXT,
    standard_param VARCHAR(100),
    collect_time DATETIME,
    FOREIGN KEY (device_id) REFERENCES power_device(id)
);
```

### 3. 远程核容记录表（`power_capacity_test`）

```sql
CREATE TABLE power_capacity_test (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id BIGINT,
    execute_time DATETIME,
    duration_minutes INT,
    result VARCHAR(50),
    capacity_before FLOAT,
    capacity_after FLOAT,
    battery_health FLOAT,
    curve_data TEXT,
    remark TEXT,
    FOREIGN KEY (device_id) REFERENCES power_device(id)
);
```

---

## 三、技术栈

| 组件 | 技术 |
|------|------|
| 后端框架 | Spring Boot |
| ORM 框架 | MyBatis-Plus |
| 数据库 | MySQL 8.x |
| 接口工具 | Swagger + Knife4j |
| 安全 | Spring Security（可选） |
| 前端（选配） | Vue3 + ECharts |

---

## 四、项目目录建议

```
src/
├── controller/
├── service/
├── mapper/
├── entity/
└── dto/
```

---

## 五、功能开发对照表

| 模块 | 实现方式 |
|------|----------|
| 📊 运行指标统计 | 使用 GROUP BY 区域、时间聚合统计 |
| 🚨 告警统计 | 通过告警字段统计告警数量、类型等 |
| ⚙️ 告警记录 | 插入告警记录到监控表中 |
| 📁 告警类型 | 通过 `monitor_name` 控制类型扩展 |
| 📋 台账管理 | CRUD 操作 `power_device` 表 |
| 📦 条件查询 | MyBatisPlus LambdaQueryWrapper |
| 🧪 核容记录 | 插入 `power_capacity_test` 并存储曲线 |
| 📈 趋势分析 | `monitor_name='电池健康度'` 折线图展示 |
