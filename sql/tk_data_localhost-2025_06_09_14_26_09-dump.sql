-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: tk_data
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `menu`
--

DROP TABLE IF EXISTS `menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '路由路径',
  `component` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `order_num` int DEFAULT '0' COMMENT '排序号',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `hidden` tinyint DEFAULT '0' COMMENT '是否隐藏：0-显示，1-隐藏',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `permission_id` bigint DEFAULT NULL COMMENT '关联权限ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `menu`
--

LOCK TABLES `menu` WRITE;
/*!40000 ALTER TABLE `menu` DISABLE KEYS */;
INSERT INTO `menu` VALUES (1,'系统管理','/system','Layout','system',1,0,0,1,NULL,'2025-05-13 07:16:18','2025-05-13 07:16:18'),(2,'用户管理','/system/user','system/user/index','user',1,1,0,1,NULL,'2025-05-13 07:16:18','2025-05-13 07:16:18'),(3,'角色管理','/system/role','system/role/index','role',2,1,0,1,NULL,'2025-05-13 07:16:18','2025-05-13 07:16:18'),(4,'权限管理','/system/permission','system/permission/index','permission',3,1,0,1,NULL,'2025-05-13 07:16:18','2025-05-13 07:16:18');
/*!40000 ALTER TABLE `menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `url_pattern` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'URL模式',
  `method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求方法',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_tenant` (`code`,`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
INSERT INTO `permission` VALUES (1,'用户查询','USER:VIEW','/api/users/**','GET',1,'2025-05-13 07:16:18','2025-05-13 07:16:18'),(2,'用户新增','USER:ADD','/api/users','POST',1,'2025-05-13 07:16:18','2025-05-13 07:16:18'),(3,'用户编辑','USER:EDIT','/api/users/**','PUT',1,'2025-05-13 07:16:18','2025-05-13 07:16:18'),(4,'用户删除','USER:DELETE','/api/users/**','DELETE',1,'2025-05-13 07:16:18','2025-05-13 07:16:18'),(5,'角色查询','ROLE:VIEW','/api/roles/**','GET',1,'2025-05-13 07:16:18','2025-05-13 07:16:18'),(6,'角色管理','ROLE:MANAGE','/api/roles/**','*',1,'2025-05-13 07:16:18','2025-05-13 07:16:18'),(7,'租户查询','tenant:view','/api/tenants/**','GET',1,'2025-05-15 10:31:18','2025-05-15 10:31:18'),(8,'创建新角色','role:create','/api/roles/**','Post',1,'2025-05-16 01:08:38','2025-05-16 01:08:38'),(9,'查看权限','permission:view','/api/permissions/**','Get',1,'2025-05-16 02:08:57','2025-05-16 02:08:57'),(13,'用户创建','user:create','/api/users','POST',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(14,'用户更新','user:update','/api/users/**','PUT',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(15,'用户角色分配','user:assign-role','/api/users/*/roles/*','POST',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(16,'用户角色移除','user:remove-role','/api/users/*/roles/*','DELETE',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(17,'角色更新','role:update','/api/roles/**','PUT',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(18,'角色删除','role:delete','/api/roles/**','DELETE',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(19,'权限创建','permission:create','/api/permissions','POST',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(20,'权限更新','permission:update','/api/permissions/**','PUT',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(21,'权限删除','permission:delete','/api/permissions/**','DELETE',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(22,'权限分配','permission:assign','/api/permissions/assign','POST',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(23,'权限移除','permission:remove','/api/permissions/remove','DELETE',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(24,'租户创建','tenant:create','/api/tenants','POST',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(25,'租户更新','tenant:update','/api/tenants/**','PUT',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(26,'租户删除','tenant:delete','/api/tenants/**','DELETE',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(27,'租户A数据查看','tenant_a:data:view','/api/tenant-a/data/**','GET',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(28,'租户A数据管理','tenant_a:data:manage','/api/tenant-a/data/**','*',1,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(29,'租户B数据查看','tenant_b:data:view','/api/tenant-b/data/**','GET',2,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(30,'租户B数据管理','tenant_b:data:manage','/api/tenant-b/data/**','*',2,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(31,'电源设备查看','power:device','/api/power/devices/**','*',1,'2025-05-26 08:19:27','2025-05-26 09:31:50'),(34,'电源设备警告','power:alarm','/api/power/**','*',1,'2025-05-26 09:30:04','2025-05-26 09:31:19');
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `power_capacity_test`
--

DROP TABLE IF EXISTS `power_capacity_test`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `power_capacity_test` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `execute_time` datetime NOT NULL COMMENT '执行时间',
  `duration_minutes` int DEFAULT NULL COMMENT '持续时间（分钟）',
  `result` varchar(50) DEFAULT NULL COMMENT '测试结果',
  `capacity_before` float DEFAULT NULL COMMENT '测试前容量',
  `capacity_after` float DEFAULT NULL COMMENT '测试后容量',
  `battery_health` float DEFAULT NULL COMMENT '电池健康度',
  `curve_data` text COMMENT '曲线数据（JSON格式）',
  `remark` text COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_execute_time` (`execute_time`),
  KEY `idx_tenant_id` (`tenant_id`),
  CONSTRAINT `fk_capacity_device` FOREIGN KEY (`device_id`) REFERENCES `power_device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='电源远程核容测试表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `power_capacity_test`
--

LOCK TABLES `power_capacity_test` WRITE;
/*!40000 ALTER TABLE `power_capacity_test` DISABLE KEYS */;
/*!40000 ALTER TABLE `power_capacity_test` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `power_device`
--

DROP TABLE IF EXISTS `power_device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `power_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_name` varchar(100) NOT NULL COMMENT '设备名称',
  `ip_address` varchar(50) NOT NULL COMMENT 'IP地址',
  `location` varchar(255) DEFAULT NULL COMMENT '设备位置',
  `device_type` varchar(50) DEFAULT NULL COMMENT '设备类型',
  `install_date` datetime DEFAULT NULL COMMENT '安装日期',
  `remark` text COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备编号（字符串）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ip_tenant` (`ip_address`,`tenant_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_power_device_device_id` (`device_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2006 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='电源设备表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `power_device`
--

LOCK TABLES `power_device` WRITE;
/*!40000 ALTER TABLE `power_device` DISABLE KEYS */;
INSERT INTO `power_device` VALUES (1,'电源设备A','192.168.1.101','机房A-1楼','PDU','2024-03-15 00:00:00','运行正常','2025-05-19 03:11:22','2025-05-19 03:11:22',1,'DEV-000001'),(2,'电源设备B','192.168.1.102','数据中心B区','UPS','2023-11-20 00:00:00','维护中','2025-05-19 03:11:22','2025-05-19 03:11:22',1,'DEV-000002'),(3,'电源设备C','192.168.1.103','服务器机柜C','智能插座','2025-01-10 00:00:00','新部署','2025-05-19 03:11:22','2025-05-19 03:11:22',1,'DEV-000003'),(4,'电源设备D','192.168.1.104','测试实验室','PDU','2024-08-05 00:00:00','性能良好','2025-05-19 03:11:22','2025-05-19 03:11:22',1,'DEV-000004'),(5,'电源设备E','192.168.1.105','备用机房','UPS','2024-02-28 00:00:00','待升级固件','2025-05-19 03:11:22','2025-05-19 03:11:22',1,'DEV-000005'),(7,'测试','192.168.101.130','测试','其他','2025-05-19 11:47:38','测试','2025-05-19 11:47:42','2025-05-19 11:47:42',1,'DEV-000007'),(1001,'机房主电源','192.168.10.101','数据中心1','电源设备',NULL,NULL,'2025-05-26 10:01:23','2025-05-26 10:01:23',1,'DEV20250001'),(1002,'服务器电源A','192.168.10.102','配电室A','电源设备',NULL,NULL,'2025-05-26 10:01:23','2025-05-26 10:01:23',1,'DEV20250002'),(1003,'备用电源B','192.168.10.103','设备机房B','电源设备',NULL,NULL,'2025-05-26 10:01:23','2025-05-26 10:01:23',1,'DEV20250003'),(1004,'监控系统电源','192.168.10.104','监控中心','电源设备',NULL,NULL,'2025-05-26 10:01:23','2025-05-26 10:01:23',1,'DEV20250004'),(1005,'远程站点供电','192.168.10.105','远程站点','电源设备',NULL,NULL,'2025-05-26 10:01:23','2025-05-26 10:01:23',1,'DEV20250005'),(2001,'Main Power','192.168.10.201','Data Center 1','Power Device',NULL,NULL,'2025-05-26 10:05:35','2025-05-26 10:05:35',1,'TEST2001'),(2002,'Server Power A','192.168.10.202','Power Room A','Power Device',NULL,NULL,'2025-05-26 10:05:35','2025-05-26 10:05:35',1,'TEST2002'),(2003,'Backup Power B','192.168.10.203','Equipment Room B','Power Device',NULL,NULL,'2025-05-26 10:05:35','2025-05-26 10:05:35',1,'TEST2003'),(2004,'Monitor Power','192.168.10.204','Monitoring Center','Power Device',NULL,NULL,'2025-05-26 10:05:35','2025-05-26 10:05:35',1,'TEST2004'),(2005,'Remote Site Power','192.168.10.205','Remote Site','Power Device',NULL,NULL,'2025-05-26 10:05:35','2025-05-26 10:05:35',1,'TEST2005');
/*!40000 ALTER TABLE `power_device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `power_monitor_record`
--

DROP TABLE IF EXISTS `power_monitor_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `power_monitor_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `monitor_name` varchar(100) NOT NULL COMMENT '监控项名称',
  `data_type` varchar(50) DEFAULT NULL COMMENT '数据类型',
  `value` float DEFAULT NULL COMMENT '监控值',
  `threshold_upper` float DEFAULT NULL COMMENT '上限阈值',
  `threshold_lower` float DEFAULT NULL COMMENT '下限阈值',
  `is_alarm` tinyint(1) DEFAULT '0' COMMENT '是否告警',
  `alarm_level` varchar(50) DEFAULT NULL COMMENT '告警级别',
  `alarm_desc` text COMMENT '告警描述',
  `standard_param` varchar(100) DEFAULT NULL COMMENT '标准参数',
  `collect_time` datetime NOT NULL COMMENT '采集时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `is_processed` tinyint(1) DEFAULT '0' COMMENT '是否已处理',
  `process_time` datetime DEFAULT NULL COMMENT '处理时间',
  `process_by` varchar(100) DEFAULT NULL COMMENT '处理人',
  `process_description` text COMMENT '处理描述',
  `process_method` varchar(50) DEFAULT NULL COMMENT '处理方式',
  `resolved` tinyint(1) DEFAULT '0' COMMENT '是否已解决',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_is_alarm` (`is_alarm`),
  KEY `idx_collect_time` (`collect_time`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_power_monitor_is_alarm` (`is_alarm`),
  KEY `idx_power_monitor_is_processed` (`is_processed`),
  KEY `idx_power_monitor_alarm_level` (`alarm_level`),
  KEY `idx_power_monitor_collect_time` (`collect_time`),
  CONSTRAINT `fk_monitor_device` FOREIGN KEY (`device_id`) REFERENCES `power_device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20046 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='电源监控记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `power_monitor_record`
--

LOCK TABLES `power_monitor_record` WRITE;
/*!40000 ALTER TABLE `power_monitor_record` DISABLE KEYS */;
INSERT INTO `power_monitor_record` VALUES (20001,2001,'温度过高','温度',35.5,45,10,1,'信息','Temperature slightly high','标准温度值','2025-05-20 10:11:28','2025-05-20 10:11:28',1,1,'2025-05-21 10:11:28','admin','已确认并处理','远程调整',1),(20002,2002,'温度过高','温度',36.2,45,10,1,'信息','Temperature slightly high','标准温度值','2025-05-20 10:11:28','2025-05-20 10:11:28',1,1,'2025-05-21 10:11:28','admin','已确认并处理','远程调整',1),(20003,2003,'温度过高','温度',34.8,45,10,1,'信息','Temperature slightly high','标准温度值','2025-05-20 10:11:28','2025-05-20 10:11:28',1,0,NULL,NULL,NULL,NULL,0),(20004,2004,'温度过高','温度',35.1,45,10,1,'信息','Temperature slightly high','标准温度值','2025-05-21 10:11:28','2025-05-21 10:11:28',1,1,'2025-05-22 10:11:28','admin','已确认并处理','远程调整',1),(20005,2005,'温度过高','温度',36,45,10,1,'信息','Temperature slightly high','标准温度值','2025-05-21 10:11:28','2025-05-21 10:11:28',1,0,NULL,NULL,NULL,NULL,0),(20011,2001,'欠压告警','电压',241,240,200,1,'警告','Voltage above normal range','标准电压值','2025-05-21 10:11:28','2025-05-21 10:11:28',1,1,'2025-05-22 10:11:28','admin','已确认并处理','人工检查',1),(20012,2002,'欠压告警','电压',199,240,200,1,'警告','Voltage below normal range','标准电压值','2025-05-22 10:11:28','2025-05-22 10:11:28',1,0,NULL,NULL,NULL,NULL,0),(20013,2003,'过压告警','电压',243,240,200,1,'警告','Voltage above normal range','标准电压值','2025-05-22 10:11:28','2025-05-22 10:11:28',1,1,'2025-05-23 10:11:28','admin','已确认并处理','人工检查',1),(20014,2004,'过压告警','电压',198,240,200,1,'警告','Voltage below normal range','标准电压值','2025-05-23 10:11:28','2025-05-23 10:11:28',1,0,NULL,NULL,NULL,NULL,0),(20015,2005,'过压告警','电压',242,240,200,1,'警告','Voltage above normal range','标准电压值','2025-05-23 10:11:28','2025-05-23 10:11:28',1,1,'2025-05-24 10:11:28','admin','已确认并处理','人工检查',1),(20021,2001,'电源断电','功率',2050,2000,500,1,'严重','Power exceeds threshold','标准功率值','2025-05-23 10:11:28','2025-05-23 10:11:28',1,1,'2025-05-24 10:11:28','admin','已处理电源波动问题','现场维修',1),(20022,2002,'电源断电','功率',2100,2000,500,1,'严重','Power exceeds threshold','标准功率值','2025-05-24 10:11:28','2025-05-24 10:11:28',1,0,NULL,NULL,NULL,NULL,0),(20023,2003,'电源断电','功率',2150,2000,500,1,'严重','Power exceeds threshold','标准功率值','2025-05-24 10:11:28','2025-05-24 10:11:28',1,1,'2025-05-25 10:11:28','admin','已处理电源波动问题','现场维修',1),(20024,2004,'电源断电','功率',480,2000,500,1,'严重','Power near lower threshold','标准功率值','2025-05-25 10:11:28','2025-05-25 10:11:28',1,0,NULL,NULL,NULL,NULL,0),(20025,2005,'电源断电','功率',2200,2000,500,1,'严重','Power exceeds threshold','标准功率值','2025-05-25 10:11:28','2025-05-25 10:11:28',1,1,'2025-05-25 22:11:28','admin','已处理电源波动问题','现场维修',1),(20031,2004,'通信失败','通信',300,30,0,1,'紧急','Communication lost for 5+ minutes','标准通信值','2025-05-25 10:11:28','2025-05-25 10:11:28',1,0,NULL,NULL,NULL,NULL,0),(20032,2005,'通信失败','通信',350,30,0,1,'紧急','Communication lost for 5+ minutes','标准通信值','2025-05-25 10:11:28','2025-05-25 10:11:28',1,0,NULL,NULL,NULL,NULL,0),(20033,2003,'电流异常','电流',26,20,1,1,'紧急','Current severely exceeds threshold','标准电流值','2025-05-26 10:11:28','2025-05-26 10:11:28',1,0,NULL,NULL,NULL,NULL,0),(20041,2001,'温度过高','温度',36.5,45,10,1,'信息','Temperature slightly high','标准温度值','2025-05-26 10:11:28','2025-05-26 10:11:28',1,0,NULL,NULL,NULL,NULL,0),(20042,2002,'欠压告警','电压',245,240,200,1,'警告','Voltage above normal range','标准电压值','2025-05-26 10:11:28','2025-05-26 10:11:28',1,0,NULL,NULL,NULL,NULL,0),(20043,2003,'电源断电','功率',2180,2000,500,1,'严重','Power exceeds threshold','标准功率值','2025-05-26 10:11:28','2025-05-26 10:11:28',1,0,NULL,NULL,NULL,NULL,0),(20044,2004,'通信失败','通信',320,30,0,1,'紧急','Communication lost for 5+ minutes','标准通信值','2025-05-26 10:11:28','2025-05-26 10:11:28',1,0,NULL,NULL,NULL,NULL,0),(20045,2005,'电流异常','电流',27.5,20,1,1,'紧急','Current severely exceeds threshold','标准电流值','2025-05-26 10:11:28','2025-05-26 10:11:28',1,0,NULL,NULL,NULL,NULL,0);
/*!40000 ALTER TABLE `power_monitor_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pub_model`
--

DROP TABLE IF EXISTS `pub_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pub_model` (
  `id` bigint NOT NULL COMMENT '主键',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `is_deleted` int DEFAULT NULL COMMENT '是否已删除',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '型号名称',
  `status` int DEFAULT '0' COMMENT '状态：1启用0禁用',
  `category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类：字典值',
  `sub_category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '子分类：字典值，要通过字典子项，与catgory联动',
  `snmp_key` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '型号匹配KEY',
  `ext_config` json DEFAULT NULL COMMENT '额外配置项：json数组，让用户添加项（key value）的方式保存',
  `manufacturer_id` bigint DEFAULT NULL COMMENT '厂商id',
  `board_switch` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否为板卡交换机(0:否 1:是)',
  `alias` text COLLATE utf8mb4_unicode_ci COMMENT '别名(分号隔开)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `is_optical_bypass` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '光旁路设备标识：1是 0否',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='型号表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pub_model`
--

LOCK TABLES `pub_model` WRITE;
/*!40000 ALTER TABLE `pub_model` DISABLE KEYS */;
INSERT INTO `pub_model` VALUES (3881444718658560,3792370627823610,'2018-06-25 13:54:06',3792370627823610,'2018-06-25 13:54:06',NULL,0,'机架42U',1,'public','rack',NULL,NULL,3813999643247621,'0',NULL,1,'0'),(3881804926469120,4956048713290752,'2018-05-08 14:39:05',2,'2023-10-27 14:26:19',NULL,0,'X300-2M',0,'industrialEthernet','switch',NULL,NULL,3813999634957313,'0',NULL,1,'0'),(3882048844022784,3792370627823610,'2018-05-08 18:47:12',2,'2023-10-27 14:26:00',NULL,0,'KYLAND SICOM3005',0,'industrialEthernet','switch',NULL,NULL,3813999634711552,'0',NULL,1,'1'),(3882084022731776,3792370627823610,'2018-05-08 19:23:00',2,'2023-10-27 14:25:52',NULL,0,'SICOM3016',0,'industrialEthernet','switch',NULL,NULL,3813999635055617,'0',NULL,1,'0'),(3882899106563072,4956048713290752,'2018-05-09 09:12:08',2,'2023-10-27 14:25:58',NULL,0,'KYLAND SICOM6424M',0,'industrialEthernet','switch',NULL,NULL,3813999634711552,'0',NULL,1,'0'),(3882928599352320,3792370627823610,'2018-05-09 09:42:08',4956050145495040,'2020-06-09 09:23:24',NULL,0,'X204-2LD',1,'industrialEthernet','switch',NULL,NULL,3813999634957313,'0',NULL,1,'1'),(3882957287769088,3792370627823610,'2018-06-01 11:51:05',2,'2024-11-26 17:43:32',NULL,0,'SDG IAC1728-4G-16-DC',1,'industrialEthernet','switch','1.3.6.1.4.1.17431.1.6,1.3.6.1.4.1.34751.1.3751.0,1.3.6.1.4.1.17431.1.24',NULL,3813999643247620,'0','IAC1728-4G-16-DC',1,'0'),(3882968122196992,4956048713290752,'2018-05-09 10:22:21',2,'2023-10-27 14:25:52',NULL,0,'RS20-0800',0,'industrialEthernet','switch',NULL,NULL,3813999635055617,'0',NULL,1,'0'),(3882984738178048,3792370627823610,'2018-05-09 10:39:15',2,'2023-10-27 14:25:51',NULL,0,'RS20-0900',0,'industrialEthernet','switch',NULL,NULL,3813999635055617,'0',NULL,1,'1'),(3882994370364416,4956048713290752,'2018-05-09 10:49:03',2,'2023-10-27 14:26:37',NULL,0,'IETH804-H',0,'industrialEthernet','switch',NULL,NULL,3813999634957312,'0',NULL,1,'0'),(3883003801699328,3792370627823610,'2018-05-09 10:58:38',2,'2023-10-27 14:26:36',NULL,0,'IETH8008',0,'industrialEthernet','switch',NULL,NULL,3813999634957312,'0',NULL,1,'1'),(3883013752767488,3792370627823610,'2018-05-09 11:08:46',2,'2023-12-12 15:35:03',NULL,0,'Gazelle S1010i',1,'industrialEthernet','switch','1.3.6.1.4.1.8886.26.101',NULL,3813999635072002,'0','S1010i;Gazelle S1010i',1,'1'),(3883024404136960,4956048713290752,'2018-05-09 11:19:36',2,'2023-10-27 14:26:41',NULL,0,'OtCom 3009',0,'industrialEthernet','switch',NULL,NULL,3813999655420929,'0',NULL,1,'1'),(3883042359886848,4956048713290752,'2018-05-09 11:37:52',2,'2023-10-27 14:27:18',NULL,0,'RS900',0,'industrialEthernet','switch',NULL,NULL,3813999655420930,'0',NULL,1,'0'),(3883043918087168,3792370627823610,'2018-06-01 15:33:29',2,'2023-12-18 15:26:07',NULL,0,'SDG IAC1408-4',1,'industrialEthernet','switch','1.3.6.1.4.1.17431.1,1.3.6.1.4.1.34751.1.3115.0,1.3.6.1.4.1.17431.1.1',NULL,3813999643247620,'0','IAC1408-4;IAC-1408;IAC-1408-4;ISC1408-4;IAC1408-4-4-IPS4;LAC-1408;IAC1480-4;lAC1408-4',1,'0'),(3883076874454016,4956048713290752,'2018-05-09 12:12:58',2,'2023-10-27 14:27:10',NULL,0,'WS-C3560G-48TS-S',0,'industrialEthernet','switch',NULL,NULL,3813999655420928,'0',NULL,1,'1'),(3883190373991424,4956037523889152,'2023-04-11 17:09:46',2,'2023-12-18 15:26:58',NULL,0,'SDG IAC1724-12-DCB',1,'industrialEthernet','switch','1.3.6.1.4.1.17431.1.10',NULL,3813999643247620,'0','IAC1724-12-DCB;lAC1724-12-DCB;IAC-1724-12-DCB;特发IAC1724-12-DCB',1,'1'),(3883220595852288,3792370627823610,'2018-05-09 14:39:10',2,'2023-10-27 14:27:05',NULL,0,'Catalyst 3750',0,'industrialEthernet','switch',NULL,NULL,3813999655420928,'0',NULL,1,'0'),(3883246607270912,3792370627823610,'2018-05-29 18:42:03',3792370627823610,'2018-05-29 18:42:03',NULL,0,'ODF_FC_12',1,'fiber','odf',NULL,NULL,3813999643247621,'1',NULL,1,'1'),(3883258975863808,3792370627823610,'2018-05-09 15:18:13',3792370627823610,'2018-05-09 15:18:13',NULL,0,'ODF_FC_24',1,'public','odf',NULL,NULL,3813999643247621,'1',NULL,1,'0'),(3883271606223872,3792370627823610,'2018-05-09 15:31:04',3792370627823610,'2018-05-09 17:18:26',NULL,0,'ODF_FC_36',1,'public','odf',NULL,NULL,3813999643247621,'1',NULL,1,'0'),(3883285071250432,3792370627823610,'2018-05-09 15:44:46',3792370627823610,'2018-05-09 15:44:46',NULL,0,'ODF_FC_48',1,'public','odf',NULL,NULL,3813999643247621,'1',NULL,1,'1'),(3883290307429376,3792370627823610,'2018-06-08 10:52:15',3792370627823610,'2018-06-08 10:52:15',NULL,0,'ODF_FC_72',1,'fiber','odf',NULL,NULL,3813999643247621,'1',NULL,1,'0'),(3883352911119360,3792370627823610,'2018-05-09 16:53:46',3792370627823610,'2018-08-09 15:39:04',NULL,0,'H3C S3100V2',1,'industrialEthernet','switch','1.3.6.1.4.1.25506.1.653',NULL,3813999632630781,'0',NULL,1,'0'),(3883369418179584,3792370627823610,'2018-05-09 17:10:34',2,'2023-10-30 11:11:17',NULL,0,'Quidway S2700-9TP-EI',0,'industrialEthernet','switch','1.3.6.1.4.1.2011.2.23.114',NULL,3813999632630782,'0',NULL,1,'0'),(3883395970483200,3792370627823610,'2018-05-09 17:37:34',3792370627823610,'2018-08-09 15:38:45',NULL,0,'H3C S3100-52P',1,'industrialEthernet','switch',NULL,NULL,3813999632630781,'0',NULL,1,'0'),(3886204672836608,3792370627823610,'2018-05-29 18:42:21',3792370627823610,'2018-05-29 18:42:21',NULL,0,'ODF_FC_12',1,'fiber','odf',NULL,NULL,3813999643247621,'1',NULL,1,'1'),(3886239118377984,3792370627823610,'2018-05-11 17:49:46',3792370627823610,'2018-05-11 17:49:46',NULL,0,'ODF_FC_96',1,'public','odf',NULL,NULL,3813999643247621,'1',NULL,1,'0'),(3915849093792768,3792370627823610,'2018-06-01 15:50:36',2,'2023-12-18 15:47:31',NULL,0,'SDG IAC1412-4-4C-AC',1,'industrialEthernet','switch','1.3.6.1.4.1.17431.1.18,1.3.6.1.4.1.34751.1.3116.0',NULL,3813999643247620,'0','',1,'1'),(3915931607188480,3792370627823610,'2018-06-01 17:14:32',2,'2023-12-18 15:52:52','1.3.6.1.4.1.34751.1.3751.0,',0,'SDG IAC1428-4G-12-AC',1,'industrialEthernet','switch','1.3.6.1.4.1.17431.1.3',NULL,3813999643247620,'0','SDG IAC1428-4G-12',1,'1');
/*!40000 ALTER TABLE `pub_model` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_tenant` (`code`,`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'系统管理员','ADMIN',1,'系统管理员角色','2025-05-13 07:16:18','2025-05-13 07:16:18'),(2,'普通用户','USER',1,'普通用户角色','2025-05-13 07:16:18','2025-05-13 07:16:18'),(3,'测试','3',1,'测试','2025-05-16 01:15:38','2025-05-16 01:15:38'),(4,'超级管理员','SUPER_ADMIN',1,NULL,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(5,'系统管理员','SYSTEM_ADMIN',1,NULL,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(6,'租户A管理员','TENANT_A_ADMIN',1,NULL,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(7,'租户A普通用户','TENANT_A_USER',1,NULL,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(8,'租户B管理员','TENANT_B_ADMIN',2,NULL,'2025-05-16 06:36:53','2025-05-16 06:36:53'),(9,'租户B普通用户','TENANT_B_USER',2,NULL,'2025-05-16 06:36:53','2025-05-16 06:36:53');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_permission`
--

DROP TABLE IF EXISTS `role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permission` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`role_id`,`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_permission`
--

LOCK TABLES `role_permission` WRITE;
/*!40000 ALTER TABLE `role_permission` DISABLE KEYS */;
INSERT INTO `role_permission` VALUES (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,13),(1,14),(1,15),(1,16),(1,17),(1,18),(1,19),(1,20),(1,21),(1,22),(1,23),(1,24),(1,25),(1,26),(1,27),(1,28),(1,31),(1,34),(2,1),(2,5),(3,14),(4,1),(4,2),(4,3),(4,4),(4,5),(4,6),(4,7),(4,8),(4,9),(4,13),(4,14),(4,15),(4,16),(4,17),(4,18),(4,19),(4,20),(4,21),(4,22),(4,23),(4,24),(4,25),(4,26),(4,27),(4,28),(4,31),(4,34);
/*!40000 ALTER TABLE `role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schedule_lldp_config`
--

DROP TABLE IF EXISTS `schedule_lldp_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schedule_lldp_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `auth_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `community` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `priv_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `snmp_version` int DEFAULT NULL,
  `target_service_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schedule_lldp_config`
--

LOCK TABLES `schedule_lldp_config` WRITE;
/*!40000 ALTER TABLE `schedule_lldp_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `schedule_lldp_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tenant`
--

DROP TABLE IF EXISTS `tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租户ID',
  `tenant_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '租户编码',
  `tenant_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '租户名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `tenant_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `tenant_code` (`tenant_code`),
  KEY `idx_tenant_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenant`
--

LOCK TABLES `tenant` WRITE;
/*!40000 ALTER TABLE `tenant` DISABLE KEYS */;
INSERT INTO `tenant` VALUES (1,'default','默认租户','2025-05-13 07:16:18','2025-05-26 09:18:44',1,'default'),(2,'TENANT_A','租户A','2025-05-16 06:26:42','2025-05-26 09:18:44',1,'TENANT_A'),(3,'TENANT_B','租户B','2025-05-16 06:26:42','2025-05-26 09:18:44',1,'TENANT_B');
/*!40000 ALTER TABLE `tenant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trs_device`
--

DROP TABLE IF EXISTS `trs_device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trs_device` (
  `id` bigint NOT NULL COMMENT '主键',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `is_deleted` smallint DEFAULT '0' COMMENT '是否已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `area_id` bigint NOT NULL COMMENT '区域id',
  `dscode` varchar(50) DEFAULT NULL COMMENT '所属地市',
  `name` varchar(100) NOT NULL COMMENT '设备名称',
  `code` varchar(100) DEFAULT NULL COMMENT '设备资产编码',
  `pinyin` varchar(255) NOT NULL COMMENT '名称拼音',
  `spec_name` varchar(255) NOT NULL COMMENT '规范名称（区域+站点(若有)+机房(若有)+机架(若有)＋名称组成）',
  `spec_code` varchar(255) DEFAULT NULL COMMENT '规范编码（仅作保留字段）',
  `spec_pinyin` varchar(255) NOT NULL COMMENT '规范名称拼音',
  `gather_name` varchar(100) DEFAULT NULL COMMENT '采集名称',
  `station_id` bigint DEFAULT NULL COMMENT '站点ID',
  `deviceroom_id` bigint DEFAULT NULL COMMENT '机房ID',
  `rack_id` bigint DEFAULT NULL COMMENT '机架ID',
  `rack_u` smallint DEFAULT NULL COMMENT '机架位置（U）',
  `position_full_name` varchar(255) DEFAULT NULL COMMENT '所处位置（仅作页面回显）',
  `model_id` bigint DEFAULT NULL COMMENT '型号ID',
  `model_full_name` varchar(255) DEFAULT NULL COMMENT '设备型号（仅作页面回显）',
  `operating_state` char(1) DEFAULT NULL COMMENT '投运状态：0未投运1已投运2已退运3临时停运',
  `operating_time` datetime DEFAULT NULL COMMENT '投运时间：（默认今天）',
  `device_role` varchar(10) DEFAULT '3' COMMENT '设备性质：1三层核心交换机,2三层汇聚交换机,3二层接入交换机,4三层接入交换机',
  `device_type` varchar(10) DEFAULT NULL COMMENT '设备分类（equ_type_category\r\n的子项）：\r\nswitch交换机\r\nroute路由器\r\nfirewall防火墙',
  `important` char(1) DEFAULT NULL COMMENT '是否重要设备：1是0否',
  `ip` varchar(30) DEFAULT NULL COMMENT 'IP地址',
  `ipv6` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'ipv6地址',
  `mac_address` varchar(255) DEFAULT NULL COMMENT 'MAC地址',
  `gather_config` json DEFAULT NULL COMMENT '配置项\r\n{\r\n    frequency: {\r\n        status_delay: 1,\r\n        performance_delay: 10\r\n    },\r\n    snmp: {\r\n        version: 1,\r\n        readCommunity: public,\r\n        writeCommunity: private,\r\n        snmpV3: {\r\n            snmpUser: public,\r\n            securityLevel: 1,\r\n            authAlgorithm: 1,\r\n            authPassword: admin,\r\n            privacyAlgorithm: 1,\r\n            privacyPassword: admin\r\n        }\r\n    },\r\n    telnet: {\r\n        user: admin,\r\n        pwd: admin\r\n    },\r\n    ssh: {\r\n        user: admin,\r\n        pwd: admin\r\n    },\r\n    enable: {\r\n       pwd:admin\r\n    }\r\n}',
  `ext_config` json DEFAULT NULL COMMENT '其他配置项',
  `source` char(1) DEFAULT NULL COMMENT '来源：0手工录入1自动发现2文件导入',
  `domain_id` bigint DEFAULT NULL COMMENT '数据域ID',
  `ne_or_terminal` bigint DEFAULT '0' COMMENT '0:网元 1:终端',
  `use_count` smallint DEFAULT NULL COMMENT '使用端口数量',
  `port_count` smallint DEFAULT NULL COMMENT '端口总数量',
  `use_radio` decimal(20,5) DEFAULT NULL COMMENT '端口使用率',
  `source_id` varchar(32) DEFAULT NULL COMMENT '来源ID，代码从生产系统、GIS系统的Id',
  `source_name` varchar(255) DEFAULT NULL COMMENT '来源名称',
  `electrics_port_count` int DEFAULT NULL COMMENT '电口总数',
  `electrics_port_use_count` int DEFAULT NULL COMMENT '电口使用数',
  `electrics_port_use_ratio` decimal(20,5) DEFAULT NULL COMMENT '电口使用率',
  `fiber_port_count` int DEFAULT NULL COMMENT '光口总数',
  `fiber_port_use_count` int DEFAULT NULL COMMENT '光口使用数',
  `fiber_port_use_ratio` decimal(20,0) DEFAULT NULL COMMENT '光口使用率',
  `asset` smallint DEFAULT '1' COMMENT '台账资产',
  `asset_unique_code` varchar(64) DEFAULT NULL COMMENT '台账资产唯一编码',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `is_optical_bypass` char(1) NOT NULL DEFAULT '0' COMMENT '光旁路设备标识：1是 0否',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_device` (`tenant_id`,`is_deleted`,`operating_state`),
  KEY `is_deleted` (`is_deleted`,`operating_state`),
  KEY `model_id` (`model_id`),
  KEY `idx_optical_bypass` (`tenant_id`,`is_optical_bypass`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='设备表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trs_device`
--

LOCK TABLES `trs_device` WRITE;
/*!40000 ALTER TABLE `trs_device` DISABLE KEYS */;
INSERT INTO `trs_device` VALUES (3949820600845312,3792370627823610,'2018-06-25 15:48:15',3792370627823610,'2018-07-03 17:23:07',1,NULL,3880550794757118,'zss','中港变电站S2',NULL,'zgbdzS2','中山供电局/火炬供电分局/中港变电站S2',NULL,'zgbdzS2',NULL,3914463869469696,NULL,NULL,NULL,'中山供电局/火炬供电分局',3935386904757248,NULL,'1','2018-06-25 15:48:15','2','switch','0','172.18.7.21',NULL,NULL,'{\"ssh\": {}, \"snmp\": {\"snmpV3\": {}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {}, \"telnet\": {}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 1}}',NULL,NULL,NULL,0,0,0,NULL,NULL,NULL,0,0,NULL,0,0,NULL,1,NULL,1,'1'),(3951290116523008,3792370627823610,'2018-06-26 16:43:18',3792370627823610,'2018-07-19 16:25:26',1,NULL,3880550795658240,'zss','三角供电公司S1',NULL,'sjgdgsS1','中山供电局/三角供电分局/三角供电公司/三角供电公司S1',NULL,'sjgdgsS1',NULL,3911355987362816,NULL,NULL,NULL,'中山供电局/三角供电分局/三角供电公司',3882957287769088,NULL,'1','2018-06-26 16:43:18','3','switch','0','31.82.8.2',NULL,NULL,'{\"ssh\": {}, \"snmp\": {\"snmpV3\": {}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {}, \"telnet\": {\"pwd\": \"Y0OzfPvNraDwtFB70jzkJg==\", \"user\": \"9g3OIqg9LhePequI5bAnTw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 1}}',NULL,NULL,NULL,0,0,0,NULL,NULL,NULL,0,0,NULL,0,0,NULL,1,NULL,1,'0'),(3955699184567296,3792370627823610,'2018-06-29 19:28:24',3792370627823610,'2018-07-19 16:29:20',1,NULL,3880550794757118,'zss','中港变电站S6',NULL,'zgbdzS6','中山供电局/火炬供电分局/中港变电站S6',NULL,'zgbdzS6',NULL,3914463869469696,NULL,NULL,2,'中山供电局/火炬供电分局',3883043918087168,NULL,'1','2018-06-29 19:28:24','3','switch','0','31.85.29.2',NULL,NULL,'{\"ssh\": {}, \"snmp\": {\"snmpV3\": {}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {}, \"telnet\": {\"pwd\": \"08e8mKvfMqqRTv5P09l/fg==\", \"user\": \"08e8mKvfMqqRTv5P09l/fg==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 1}}',NULL,NULL,NULL,0,0,0,NULL,NULL,NULL,0,0,NULL,0,0,NULL,1,NULL,1,'1'),(3959936382567424,3792370627823610,'2018-07-02 19:18:44',1,'2019-01-29 09:25:01',1,NULL,3880550794757117,'zss','富豪变电站S3',NULL,'fhbdzS3','中山供电局/东区供电分局/富豪变电站/富豪变电站S3',NULL,'fhbdzS3',NULL,3924441972786176,NULL,NULL,2,'中山供电局/东区供电分局/富豪变电站',3935628172461056,NULL,'1','2018-07-02 19:18:44','3','switch','0','31.82.17.5',NULL,NULL,'{\"ssh\": {}, \"snmp\": {\"snmpV3\": {}, \"version\": \"1\", \"readCommunity\": \"3dfMe0dd0jExHEWxIoiIwg==\", \"writeCommunity\": \"\"}, \"enable\": {}, \"telnet\": {\"pwd\": \"\", \"user\": \"\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 1}}',NULL,NULL,NULL,0,0,0,NULL,NULL,NULL,0,0,NULL,0,0,NULL,1,NULL,1,'0'),(3959947147461632,3792370627823610,'2018-07-02 19:29:57',1,'2018-12-25 10:13:08',1,NULL,3880550794757118,'zss','中港变电站S1',NULL,'zgbdzS1','中山供电局/火炬供电分局/中港变电站S1',NULL,'zgbdzS1',NULL,3914463869469696,NULL,NULL,2,'中山供电局/火炬供电分局',3935601341334528,NULL,'1','2018-07-02 19:29:57','2','switch','0','172.18.6.22',NULL,NULL,'{\"ssh\": {}, \"snmp\": {\"snmpV3\": {}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {}, \"telnet\": {\"pwd\": \"\", \"user\": \"\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 1}}',NULL,NULL,NULL,0,0,0,NULL,NULL,NULL,0,0,NULL,0,0,NULL,1,NULL,1,'1'),(3959981772456960,3792370627823610,'2018-07-02 20:04:56',0,'2025-04-27 12:30:02',0,NULL,3880550794757116,'zss','天明线宝兴阁2号A座公用配电站交换机',NULL,'tmxbxg2hAzgypdzjhj','广东电网中山供电局石岐宝兴阁2号A座/天明线宝兴阁2号A座公用配电站交换机',NULL,'gddwzsgdjsqbxg2hAz/tmxbxg2hAzgypdzjhj',NULL,3959723398431744,3959723398743040,4008950741648384,2,'广东电网中山供电局石岐宝兴阁2号A座',3883043918087168,NULL,'1','2018-07-02 20:04:56','3','switch','0','31.85.31.4',NULL,'00:a0:82:19:ab:d1','{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}',NULL,NULL,NULL,0,3,8,0.38000,NULL,NULL,4,1,0.50000,4,2,0,1,NULL,1,'0'),(3959994995246080,3792370627823610,'2018-07-02 20:18:21',0,'2025-04-27 12:30:02',0,NULL,3880550794757116,'zss','时尚一区公用开关站交换机',NULL,'ssyqgykgzjhj','广东电网中山供电局石岐供电分局时尚一区公用开关站/时尚一区公用开关站交换机',NULL,'gddwzsgdjsqgdfjssyqgykgz/ssyqgykgzjhj',NULL,3959728595141632,3959728595174400,4009016915805184,2,'广东电网中山供电局石岐供电分局时尚一区公用开关站',3883043918087168,NULL,'1','2018-07-02 20:18:21','3','switch','0','31.85.8.5',NULL,'00:a0:82:19:aa:28','{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}',NULL,NULL,NULL,0,4,8,0.50000,NULL,NULL,4,2,0.50000,4,2,0,1,NULL,1,'0'),(3960004552639488,3792370627823610,'2018-07-02 20:28:04',0,'2025-04-27 12:30:02',0,NULL,3880550794757116,'zss','天明花园C区第二幢公用配电站交换机',NULL,'tmhyCqdecgypdzjhj','广东电网中山供电局石岐天明花园C区2栋/天明花园C区第二幢公用配电站交换机',NULL,'gddwzsgdjsqtmhyCq2d/tmhyCqdecgypdzjhj',NULL,3959999949915136,3959999949947904,4008946818483200,2,'广东电网中山供电局石岐天明花园C区2栋',3883043918087168,NULL,'1','2018-07-02 20:28:04','3','switch','0','31.85.31.5',NULL,'00:a0:82:19:ab:f3','{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}',NULL,NULL,NULL,0,3,8,0.38000,NULL,NULL,4,1,0.50000,4,2,0,1,NULL,1,'1'),(3960007444792320,3792370627823610,'2018-07-02 20:31:01',0,'2025-04-27 12:30:02',0,NULL,3880550794757116,'zss','天明花园A区二十三幢公用配电站交换机',NULL,'tmhyAqesscgypdzjhj','广东电网中山供电局石岐供电分局天明花园A区二十三幢公用配电站/天明花园A区二十三幢公用配电站交换机','10','gddwzsgdjsqgdfjtmhyAqesscgypdz/tmhyAqesscgypdzjhj',NULL,1714938159600504834,3959726571897856,4008956330099712,2,'广东电网中山供电局石岐供电分局天明花园A区二十三幢公用配电站',3883043918087168,NULL,'1','2018-07-02 20:31:01','3','switch','0','31.85.31.6',NULL,'00:a0:82:19:ab:c0','{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}',NULL,NULL,NULL,0,3,8,0.38000,NULL,NULL,4,1,0.50000,4,2,0,1,NULL,1,'0'),(3960010917856256,3792370627823610,'2018-07-02 20:34:43',0,'2024-02-22 09:07:06',1,NULL,3880550795658240,'zss','高平热电厂侧公用电缆分接箱交换机','0320 T B AYJ FR12 0901146 T','gprdccgydlfjxjhj','广东电网中山供电局三角供电分局高平热电厂侧公用电缆分接箱/高平热电厂侧公用电缆分接箱交换机','1','gddwzsgdjsjgdfjgprdccgydlfjx/gprdccgydlfjxjhj',NULL,3959732681737216,3959732681769984,1704468975703822337,2,'广东电网中山供电局三角供电分局高平热电厂侧公用电缆分接箱',3883043918087168,NULL,'1','2021-07-22 00:00:00','3','switch','0','31.85.29.3',NULL,NULL,'{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"GU/nfo+x48NjmPhktumg2w==\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}',NULL,'3',NULL,0,1,8,0.13000,'499220283115100','高平热电厂侧公用电缆分接箱工业以太网交换机1',4,1,0.00000,4,0,0,1,NULL,1,'0'),(3960031874024448,3792370627823610,'2018-07-02 20:55:51',1895369992887603202,'2025-04-15 17:44:27',0,NULL,3880550796231680,'zss','立泰线马鞍村公用开关站交换机',NULL,'ltxmacgykgzjhj','翠亨供电分局/南朗供电公司/立泰线马鞍村公用开关站交换机',NULL,'chgdfj/nlgdgs/ltxmacgykgzjhj',NULL,3911359994840064,NULL,NULL,2,'翠亨供电分局/南朗供电公司',3915931607188480,NULL,'1','2018-07-02 20:55:51','3','switch','0','31.85.33.66',NULL,NULL,'{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}','[]',NULL,NULL,0,1,28,0.04000,NULL,NULL,12,0,0.06000,16,1,0,1,'A7CA44CA84CD4E1DA91230207BF911CB',1,'0'),(3960035382903808,3792370627823610,'2018-07-02 20:59:24',0,'2025-04-27 12:30:02',0,NULL,3880550794757116,'zss','金润线金鹰广场公用开关站交换机','0320 T B AYJ FR12 0900417 T','jrxjygcgykgzjhj','广东电网中山供电局石岐供电分局金鹰广场公用开关站/金润线金鹰广场公用开关站交换机','1','gddwzsgdjsqgdfjjygcgykgz/jrxjygcgykgzjhj',NULL,3959758072923136,3959758072955904,4922431877616640,2,'广东电网中山供电局石岐供电分局金鹰广场公用开关站',3883043918087168,NULL,'1','2018-07-02 20:59:24','3','switch','0','31.81.69.2',NULL,'30:29:be:a4:78:10','{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}',NULL,NULL,NULL,0,2,8,0.25000,'903220402199000',NULL,4,2,0.00000,4,0,0,1,NULL,1,'0'),(3960061827875840,3792370627823610,'2018-07-02 21:26:18',0,'2025-04-27 12:30:02',0,NULL,3880550796231680,'zss','泰金线泰钢公用开关站交换机',NULL,'tjxtggykgzjhj','广东电网中山供电局南朗南朗供电公司/泰金线泰钢公用开关站交换机',NULL,'gddwzsgdjnlnlgdgs/tjxtggykgzjhj',NULL,3911359994840064,NULL,NULL,2,'广东电网中山供电局南朗南朗供电公司',3883043918087168,NULL,'1','2018-07-02 21:26:18','3','switch','0','31.85.33.68',NULL,'30:29:be:a4:78:32','{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}','[]',NULL,NULL,0,1,8,0.13000,NULL,NULL,4,1,0.00000,4,0,0,1,NULL,1,'0'),(3960084788495360,3792370627823610,'2018-07-02 21:49:40',0,'2025-04-27 12:30:02',0,NULL,3880550795658240,'zss','民众西公用电缆分接箱交换机','0320 T B AYJ FR12 0901595 T','mzxgydlfjxjhj','广东电网中山供电局三角供电分局民众西公用电缆分接箱/民众西公用电缆分接箱交换机',NULL,'gddwzsgdjsjgdfjmzxgydlfjx/mzxgydlfjxjhj',NULL,3960084076856320,NULL,NULL,2,'广东电网中山供电局三角供电分局民众西公用电缆分接箱',3883043918087168,NULL,'1','2018-07-02 21:49:40','3','switch','0','31.85.51.5',NULL,'00:a0:82:17:7c:91','{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}',NULL,NULL,NULL,0,2,8,0.25000,'6E9A3E09BD794BDB86BBD2F52ED126E4',NULL,4,1,0.25000,4,1,0,1,NULL,1,'0'),(3960086324429824,3792370627823610,'2018-07-02 21:51:14',0,'2025-04-27 12:30:02',0,NULL,3880550795658240,'zss','沙栏东公用电缆分接箱交换机','0320 T B AYJ FR12 0901594 T','sldgydlfjxjhj','广东电网中山供电局三角供电分局沙栏东公用电缆分接箱/沙栏东公用电缆分接箱交换机',NULL,'gddwzsgdjsjgdfjsldgydlfjx/sldgydlfjxjhj',NULL,3960079602418688,NULL,NULL,2,'广东电网中山供电局三角供电分局沙栏东公用电缆分接箱',3883043918087168,NULL,'1','2018-07-02 21:51:14','3','switch','0','31.85.51.4',NULL,'00:a0:82:17:7c:c4','{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}',NULL,NULL,NULL,0,3,8,0.38000,'2F0167948F304CFE86D8985ED15D1754',NULL,4,1,0.50000,4,2,0,1,NULL,1,'0'),(3960087655810048,3792370627823610,'2018-07-02 21:52:35',0,'2025-04-27 12:30:02',0,NULL,3880550795658240,'zss','三角益文公用电缆分接箱交换机','0320 T B AYJ FR12 0901591 T','sjywgydlfjxjhj','广东电网中山供电局三角供电分局三角益文公用电缆分接箱/三角益文公用电缆分接箱交换机',NULL,'gddwzsgdjsjgdfjsjywgydlfjx/sjywgydlfjxjhj',NULL,3959779258778624,NULL,NULL,2,'广东电网中山供电局三角供电分局三角益文公用电缆分接箱',3883043918087168,NULL,'1','2018-07-02 21:52:35','3','switch','0','31.85.51.3',NULL,'00:a0:82:17:81:8c','{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}',NULL,NULL,NULL,0,3,8,0.38000,'47046544B16840CDB67C4A975E463E1A',NULL,4,1,0.50000,4,2,0,1,NULL,1,'0'),(3960089134482432,3792370627823610,'2018-07-02 21:54:06',0,'2025-04-27 12:30:02',0,NULL,3880550795658240,'zss','三角蟠龙公用电缆分接箱交换机','0320 T B AYJ FR12 0901590 T','sjplgydlfjxjhj','广东电网中山供电局三角供电分局三角蟠龙公用电缆分接箱/三角蟠龙公用电缆分接箱交换机',NULL,'gddwzsgdjsjgdfjsjplgydlfjx/sjplgydlfjxjhj',NULL,3959779782034432,NULL,NULL,2,'广东电网中山供电局三角供电分局三角蟠龙公用电缆分接箱',3883043918087168,NULL,'1','2018-07-02 21:54:06','3','switch','0','31.85.51.2',NULL,'30:29:be:a4:77:dd','{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}',NULL,NULL,NULL,0,3,8,0.38000,'C4C7F20DBF1549BBB5014CB82E56B567',NULL,4,1,0.50000,4,2,0,1,NULL,1,'0'),(3960091293303808,3792370627823610,'2018-07-02 21:56:19',0,'2025-04-27 12:30:02',0,NULL,3880550796231680,'zss','立泰线立信公用开关站交换机',NULL,'ltxlxgykgzjhj','翠亨供电分局/南朗供电公司/立泰线立信公用开关站交换机',NULL,'chgdfj/nlgdgs/ltxlxgykgzjhj',NULL,3911359994840064,NULL,NULL,2,'翠亨供电分局/南朗供电公司',3883043918087168,NULL,'1','2018-07-02 21:56:19','3','switch','0','31.85.33.67',NULL,'30:29:be:a4:77:00','{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}','[]',NULL,NULL,0,2,8,0.25000,NULL,NULL,4,2,0.00000,4,0,0,1,'3526F33F82DE411CB278B40EC579BA6A',1,'0'),(3960099277767680,3792370627823610,'2018-07-02 22:04:25',0,'2025-04-27 12:30:02',0,NULL,3880550794757116,'zss','时尚三区公用开关站交换机',NULL,'sssqgykgzjhj','广东电网中山供电局石岐时尚三区开关站/时尚三区公用开关站交换机',NULL,'gddwzsgdjsqsssqkgz/sssqgykgzjhj',NULL,3959997567779840,3959997568107520,4009015053075456,2,'广东电网中山供电局石岐时尚三区开关站',3883043918087168,NULL,'1','2018-07-02 22:04:25','3','switch','0','31.85.8.9',NULL,'00:a0:82:19:ac:15','{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}',NULL,NULL,NULL,0,4,8,0.50000,NULL,NULL,4,1,0.75000,4,3,0,1,NULL,1,'0'),(3960101962859520,3792370627823610,'2018-07-02 22:07:08',0,'2025-04-27 12:30:02',0,NULL,3880550794757116,'zss','美林春天一期公用开关站交换机','0320 T B AYJ FR12 0900025 T','mlctyqgykgzjhj','广东电网中山供电局石岐供电分局美林春天一期公用开关站/美林春天一期公用开关站交换机',NULL,'gddwzsgdjsqgdfjmlctyqgykgz/mlctyqgykgzjhj',NULL,3959967072273408,3959967072306176,4009006275232768,2,'广东电网中山供电局石岐供电分局美林春天一期公用开关站',3883043918087168,NULL,'1','2018-07-02 22:07:08','3','switch','0','31.85.8.7',NULL,'00:a0:82:19:aa:39','{\"ssh\": {\"pwd\": \"\", \"user\": \"\"}, \"snmp\": {\"snmpV3\": {\"snmpUser\": \"\", \"authPassword\": \"\", \"authAlgorithm\": \"\", \"securityLevel\": \"\", \"privacyPassword\": \"\", \"privacyAlgorithm\": \"\"}, \"version\": \"1\", \"readCommunity\": \"5fynyFU2Vik+sEwXGb01rw==\", \"writeCommunity\": \"\"}, \"enable\": {\"pwd\": \"\"}, \"telnet\": {\"pwd\": \"k5QDtc2/41RoKyEIpZ7Ing==\", \"user\": \"UnUkoBeFdGsSvtHcc+qjFw==\"}, \"frequency\": {\"status_delay\": 1, \"performance_delay\": 10}}',NULL,NULL,NULL,0,2,8,0.25000,'903220380025900',NULL,4,1,0.25000,4,1,0,1,NULL,1,'1');
/*!40000 ALTER TABLE `trs_device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username_tenant` (`username`,`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','$2a$10$7SFDUykS1r0vPZVenmGNs.fba6bBPaZOg.19k/GfMc5p.PG1Oo7FW',1,1,'2025-05-14 13:58:36','2025-05-14 06:47:45');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,1);
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-09 14:26:10
