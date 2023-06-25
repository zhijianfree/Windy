--
-- Table structure for table `code_change`
--

DROP TABLE IF EXISTS `code_change`;
CREATE TABLE `code_change` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `change_id` varchar(64) NOT NULL COMMENT '变更Id',
  `change_name` varchar(50) DEFAULT NULL COMMENT '变更名称',
  `change_desc` varchar(500) DEFAULT NULL COMMENT '变更描述',
  `change_branch` varchar(500) NOT NULL COMMENT '变更分支',
  `relation_id` varchar(100) DEFAULT NULL COMMENT '关联ID 每次的变更触发可以与需求或者是bug或者是一个优化项关联，通过这个关联的ID就可以在后续的代码工作中串联起来 达到观察工作流的作用',
  `creator` varchar(100) DEFAULT NULL COMMENT '创建人',
  `service_id` varchar(100) NOT NULL COMMENT '服务Id',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_change_id` (`change_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `environment`
--

DROP TABLE IF EXISTS `environment`;
CREATE TABLE `environment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `env_name` varchar(50) NOT NULL COMMENT '环境名称',
  `env_host` varchar(50) DEFAULT NULL COMMENT '环境的IP',
  `env_port` varchar(6) DEFAULT NULL COMMENT '环境端口',
  `env_status` int(11) DEFAULT NULL COMMENT '环境状态 1 正常 2 暂停 3 已删除',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `execute_point`
--

DROP TABLE IF EXISTS `execute_point`;
CREATE TABLE `execute_point` (
  `id` bigint(20) DEFAULT NULL,
  `point_id` varchar(64) NOT NULL COMMENT '用例Id',
  `execute_type` int(11) DEFAULT '1' COMMENT '执行类型',
  `feature_id` varchar(64) NOT NULL COMMENT '用例Id',
  `description` varchar(200) DEFAULT NULL COMMENT '执行点描述',
  `feature_info` text NOT NULL COMMENT '特性运行信息',
  `compare_define` text COMMENT '特性执行结果比较',
  `variables` varchar(1000) DEFAULT NULL COMMENT '执行响应结果参数',
  `test_stage` int(11) NOT NULL COMMENT '用例阶段',
  `sort_order` int(11) DEFAULT NULL COMMENT '排序',
  `template_id` varchar(64) DEFAULT NULL COMMENT '模版Id',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_feature_id` (`feature_id`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_point_id` (`point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `execute_record`
--

DROP TABLE IF EXISTS `execute_record`;
CREATE TABLE `execute_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `execute_record_id` varchar(64) DEFAULT NULL COMMENT '执行点记录',
  `history_id` varchar(64) DEFAULT NULL COMMENT '历史记录ID',
  `status` int(2) DEFAULT NULL COMMENT '执行点运行状态',
  `execute_result` text COMMENT '执行结果',
  `execute_point_name` varchar(100) DEFAULT NULL COMMENT '执行点名称',
  `execute_point_id` varchar(64) DEFAULT NULL COMMENT '执行点ID',
  `execute_type` int(2) DEFAULT NULL COMMENT '执行类型',
  `test_stage` int(2) DEFAULT NULL COMMENT '测试阶段',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_history_id` (`history_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `execute_template`
--

DROP TABLE IF EXISTS `execute_template`;
CREATE TABLE `execute_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `template_id` varchar(64) NOT NULL COMMENT '特性id',
  `template_type` int(2) DEFAULT '1' COMMENT '模版类型',
  `service` varchar(500) DEFAULT NULL COMMENT '特性执行类名',
  `method` varchar(100) DEFAULT NULL COMMENT '特性执行方法名',
  `name` varchar(100) DEFAULT NULL COMMENT '特性执行名称',
  `author` varchar(100) DEFAULT NULL COMMENT '创建人',
  `description` varchar(100) DEFAULT NULL COMMENT '特性描述',
  `source` varchar(100) DEFAULT NULL COMMENT '特性原路径',
  `param` varchar(1000) DEFAULT NULL,
  `invoke_type` int(2) DEFAULT NULL COMMENT '执行调用方式 1 本地方法  2 Http',
  `header` text COMMENT 'invoke_type为http时，请求Header',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `feature_history`
--

DROP TABLE IF EXISTS `feature_history`;
CREATE TABLE `feature_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `history_id` varchar(100) NOT NULL COMMENT '历史Id',
  `feature_id` varchar(64) NOT NULL,
  `feature_name` varchar(100) DEFAULT NULL COMMENT '用例名称',
  `record_id` varchar(100) DEFAULT NULL COMMENT '执行记录Id',
  `executor` varchar(100) DEFAULT NULL COMMENT '执行人',
  `execute_status` int(2) DEFAULT NULL COMMENT '执行状态',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_history_id` (`history_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `feature_info`
--

DROP TABLE IF EXISTS `feature_info`;
CREATE TABLE `feature_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `test_case_id` varchar(100) DEFAULT NULL COMMENT '测试用例集ID',
  `feature_id` varchar(100) DEFAULT NULL COMMENT '用例Id',
  `feature_name` varchar(100) DEFAULT NULL COMMENT '用例名称',
  `author` varchar(100) DEFAULT NULL COMMENT '创建者',
  `modify` varchar(100) DEFAULT NULL COMMENT '修改人',
  `test_step` varchar(100) DEFAULT NULL COMMENT '测试步骤',
  `parent_id` varchar(100) DEFAULT NULL COMMENT '父节点Id',
  `feature_type` int(2) DEFAULT '1' COMMENT '用例类型',
  `status` int(2) DEFAULT NULL COMMENT '用例状态',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_test_case_id` (`test_case_id`),
  KEY `idx_feature_id` (`feature_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `feature_tag`
--

DROP TABLE IF EXISTS `feature_tag`;
CREATE TABLE `feature_tag` (
  `feature_id` varchar(64) DEFAULT NULL COMMENT '用例Id',
  `tag_value` varchar(100) NOT NULL COMMENT '标签值',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `git_bind`
--

DROP TABLE IF EXISTS `git_bind`;
CREATE TABLE `bind_branch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bind_id` varchar(64) NOT NULL COMMENT '绑定Id',
  `git_branch` varchar(100) NOT NULL COMMENT '绑定分支',
  `git_url` varchar(2000) NOT NULL COMMENT 'git地址',
  `pipeline_id` varchar(64) NOT NULL COMMENT '流水线Id',
  `is_choose` int(2) NOT NULL COMMENT '是否被选中',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pipeline_id` (`pipeline_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `microservice`
--

DROP TABLE IF EXISTS `microservice`;
CREATE TABLE `microservice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_id` varchar(64) DEFAULT NULL COMMENT '服务Id',
  `git_url` varchar(1000) DEFAULT NULL COMMENT '服务git地址',
  `service_name` varchar(100) NOT NULL COMMENT '服务名称',
  `description` varchar(300) NOT NULL COMMENT '服务描述',
  `owner` varchar(100) NOT NULL COMMENT '服务拥有者',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `node_bind`
--

DROP TABLE IF EXISTS `node_bind`;
CREATE TABLE `node_bind` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `node_id` varchar(100) NOT NULL COMMENT '执行点ID',
  `node_name` varchar(100) NOT NULL COMMENT '配置名称',
  `user_id` varchar(100) DEFAULT NULL COMMENT '创建人',
  `description` varchar(256) DEFAULT NULL COMMENT '描述',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `node_config`
--

DROP TABLE IF EXISTS `node_config`;
CREATE TABLE `node_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_id` varchar(64) NOT NULL COMMENT '配置ID',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `type` int(2) DEFAULT NULL COMMENT '配置类型',
  `config_detail` text COMMENT '配置详情',
  `sort_order` int(2) DEFAULT NULL COMMENT '排序',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `node_record`
--

DROP TABLE IF EXISTS `node_record`;
CREATE TABLE `node_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `node_id` varchar(64) NOT NULL COMMENT '节点Id',
  `record_id` varchar(64) NOT NULL COMMENT '记录Id',
  `history_id` varchar(64) NOT NULL COMMENT '历史流水线记录Id',
  `code` int(4) DEFAULT NULL COMMENT '处理结果状态码',
  `result` text COMMENT '任务处理结果',
  `status` int(2) DEFAULT NULL COMMENT '任务状态',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_history_id` (`history_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `pipeline`
--

DROP TABLE IF EXISTS `pipeline`;
CREATE TABLE `pipeline` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pipeline_id` varchar(64) NOT NULL,
  `pipeline_name` varchar(100) DEFAULT NULL COMMENT '流水线名称',
  `service_id` varchar(100) NOT NULL COMMENT '服务Id',
  `pipeline_type` int(2) DEFAULT NULL COMMENT '流水线类型 1 发布流水线 2 每日构建流水线 3 个人流水线',
  `pipeline_config` text COMMENT '流水线配置信息',
  `execute_type` int(2) DEFAULT NULL COMMENT '执行方式 1 手动执行 2push 3merge',
  `pipeline_status` int(2) DEFAULT '1' COMMENT '流水线状态',
  `creator` varchar(50) DEFAULT NULL COMMENT '流水线创建者',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_pipeline_id` (`pipeline_id`),
  KEY `idx_service_id` (`service_id`),
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `pipeline_action`
--

DROP TABLE IF EXISTS `pipeline_action`;
CREATE TABLE `pipeline_action` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action_id` varchar(100) NOT NULL COMMENT '执行点ID',
  `action_name` varchar(100) NOT NULL COMMENT '配置名称',
  `user_id` varchar(100) DEFAULT NULL COMMENT '创建人',
  `node_id` varchar(100) DEFAULT NULL COMMENT '执行点类型',
  `action_url` varchar(100) NOT NULL COMMENT '请求地址',
  `param_detail` text COMMENT '配置详情',
  `query_url` varchar(100) NOT NULL COMMENT '请求地址',
  `result` varchar(256) DEFAULT NULL COMMENT '响应结果比较',
  `description` varchar(256) DEFAULT NULL COMMENT '执行点描述',
  `execute_type` varchar(10) NOT NULL COMMENT '执行类型',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_action_id` (`action_id`),
  KEY `idx_node_id` (`node_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `pipeline_history`
--

DROP TABLE IF EXISTS `pipeline_history`;
CREATE TABLE `pipeline_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `history_id` varchar(64) NOT NULL COMMENT '执行历史Id',
  `pipeline_id` varchar(100) NOT NULL COMMENT '流水线id',
  `pipeline_name` varchar(100) DEFAULT NULL COMMENT '流水线名称',
  `branch` varchar(100) NOT NULL COMMENT '流水线运行的分支',
  `pipeline_config` text NOT NULL COMMENT '流水线执行的配置',
  `pipeline_status` int(2) NOT NULL COMMENT '流水线执行结果',
  `executor` varchar(50) DEFAULT NULL COMMENT '执行人',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_history_id` (`history_id`)
  KEY `idx_pipeline_id` (`pipeline_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流水线执行历史';

--
-- Table structure for table `pipeline_node`
--

DROP TABLE IF EXISTS `pipeline_node`;
CREATE TABLE `pipeline_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `node_id` varchar(64) NOT NULL COMMENT 'nodeId',
  `stage_id` varchar(64) NOT NULL COMMENT '阶段Id',
  `pipeline_id` varchar(64) NOT NULL COMMENT '流水线Id',
  `node_name` varchar(100) NOT NULL COMMENT '节点名称',
  `type` int(2) DEFAULT NULL COMMENT '节点类型',
  `config_detail` varchar(1000) DEFAULT NULL COMMENT '节点配置',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `pipeline_stage`
--

DROP TABLE IF EXISTS `pipeline_stage`;


CREATE TABLE `pipeline_stage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `stage_id` varchar(64) DEFAULT NULL COMMENT '阶段Id',
  `pipeline_id` varchar(64) NOT NULL COMMENT '流水线Id',
  `config_id` varchar(100) DEFAULT NULL COMMENT '关联的配置Id',
  `stage_name` varchar(100) NOT NULL COMMENT '阶段名称',
  `type` int(2) DEFAULT '1' COMMENT '阶段类型',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `system_config`
--

DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_id` varchar(100) NOT NULL COMMENT '配置ID',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `parent_id` varchar(100) DEFAULT NULL COMMENT '父节点Id',
  `type` int(2) DEFAULT NULL COMMENT '配置类型',
  `config_detail` text COMMENT '配置详情',
  `sort` int(2) DEFAULT NULL COMMENT '排序',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `task_info`
--

DROP TABLE IF EXISTS `task_info`;
CREATE TABLE `task_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` varchar(100) DEFAULT NULL COMMENT '任务ID',
  `user_id` varchar(100) DEFAULT NULL COMMENT '创建者ID',
  `service_id` varchar(100) DEFAULT NULL COMMENT '服务Id',
  `test_case_id` varchar(100) DEFAULT NULL COMMENT '测试集Id',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `description` varchar(100) DEFAULT NULL COMMENT '任务描述',
  `task_config` varchar(2000) DEFAULT NULL COMMENT '任务执行参数',
  `machines` varchar(100) DEFAULT NULL COMMENT '执行机器列表',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_task_id` (`task_id`),
  KEY `idx_service_id` (`service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `task_record`
--

DROP TABLE IF EXISTS `task_record`;
CREATE TABLE `task_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `record_id` varchar(64) DEFAULT NULL COMMENT '任务记录ID',
  `task_id` varchar(64) DEFAULT NULL COMMENT '任务ID',
  `user_id` varchar(64) DEFAULT NULL COMMENT '执行者ID',
  `test_case_id` varchar(64) DEFAULT NULL COMMENT '测试集Id',
  `status` int(2) DEFAULT NULL COMMENT '执行状态',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_config` varchar(2000) DEFAULT NULL COMMENT '任务执行参数',
  `machines` varchar(100) DEFAULT NULL COMMENT '执行机器列表',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_record_id` (`record_id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `test_case`
--

DROP TABLE IF EXISTS `test_case`;
CREATE TABLE `test_case` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `test_case_id` varchar(64) NOT NULL COMMENT '用例集id',
  `author` varchar(100) DEFAULT NULL COMMENT '创建人',
  `service_id` varchar(64) DEFAULT NULL COMMENT '服务Id',
  `test_case_name` varchar(100) DEFAULT NULL COMMENT '用例集名称',
  `description` varchar(100) DEFAULT NULL COMMENT '用例集描述',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_test_case_id` (`test_case_id`),
  KEY `idx_service_id` (`service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `test_case_config`
--

DROP TABLE IF EXISTS `test_case_config`;
CREATE TABLE `test_case_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_id` varchar(100) DEFAULT NULL COMMENT '配置ID',
  `union_id` varchar(100) DEFAULT NULL COMMENT '关联Id',
  `parent_id` varchar(100) DEFAULT NULL COMMENT '父节点Id',
  `type` int(2) DEFAULT NULL COMMENT '节点类型',
  `param_key` varchar(100) DEFAULT NULL COMMENT '参数key',
  `param_type` varchar(100) DEFAULT NULL COMMENT '参数类型',
  `value` varchar(100) DEFAULT NULL COMMENT '参数值',
  `sort_order` int(2) DEFAULT NULL COMMENT '排序',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `dispatch_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `log_id` varchar(64) NOT NULL COMMENT '日志Id',
  `log_type` int(11) NOT NULL COMMENT '任务类型 1 pipeline  2 feature',
  `source_id` varchar(64) NOT NULL COMMENT '任务日志触发源',
  `source_name` varchar(100) DEFAULT NULL COMMENT '触发源名称',
  `node_ip` varchar(100) DEFAULT NULL COMMENT '任务执行master节点Ip',
  `log_status` int(11) NOT NULL DEFAULT '1' COMMENT '任务日志状态',
  `lock_version` int(11) DEFAULT NULL COMMENT '乐观锁版本号',
  `source_record_id` varchar(100) DEFAULT NULL COMMENT '任务来源记录id',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_log_id` (`log_id`),
  KEY `idx_source_id` (`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

## 流水线默认配置
INSERT INTO windy.system_config (config_id,config_name,parent_id,`type`,config_detail,sort,create_time,update_time) VALUES
	 ('1','流水线默认配置',NULL,1,'[{"id":"0","name":"开始","status":"success","root":true,"group":"0","disable":true,"next":[{"index":1,"weight":0}]},{"id":"1","name":"结束","disable":true,"status":"success","group":"1","root":true}]',1,1,1);

## 添加默认用例模版
INSERT INTO devops.execute_template(id, template_id, template_type, service, `method`, name, author, description, source, param, create_time, update_time)
VALUES(1, '71a26960299f49c3aa26b687ea2fbdb2', 2, NULL, 'for', 'for循环', 'admin', '循环查询', NULL, NULL, 1, 1);
INSERT INTO devops.execute_template(id, template_id, template_type, service, `method`, name, author, description, source, param, create_time, update_time)
VALUES(2, '71a26960299f49c3aa26b687ea2fbdb3', 2, NULL, 'if', 'if判断', 'admin', '条件执行', NULL, NULL, 1, 2);
INSERT INTO devops.execute_template
(id, template_id, template_type, service, `method`, name, author, description, source, param, create_time, update_time)
VALUES(3, '71a26960299f49c3aa26b687ea2fbdba', 1, 'com.zj.feature.ability.mysql.MysqlFeature', 'executeQuery', 'mysql查询', 'admin', '执行mysql语句', NULL, '[{"defaultValue":{"defaultValue":"NULL"},"description":"mysql连接地址","paramKey":"connect","type":0},{"defaultValue":{"defaultValue":"NULL"},"description":"数据库名称","paramKey":"dbName","type":0},{"defaultValue":{"defaultValue":"NULL"},"description":"数据库用户","paramKey":"user","type":0},{"defaultValue":{"defaultValue":"NULL"},"description":"用户密码","paramKey":"password","type":0},{"defaultValue":{"defaultValue":""},"description":"执行的sql","paramKey":"sql","type":0}]', 1670927202853, 1675219942024);
INSERT INTO devops.execute_template
(id, template_id, template_type, service, `method`, name, author, description, source, param, create_time, update_time)
VALUES(4, 'f18a546c54fe4f70bae3a0b529e46908', 1, 'com.zj.feature.ability.http.HttpFeature', 'startHttp', 'Http请求', 'admin', '简单的http请求', NULL, '[{"defaultValue":{"defaultValue":""},"description":"请求的url","paramKey":"url","type":0},{"defaultValue":{"defaultValue":"","range":["GET","POST","PUT","DELETE"]},"description":"请求的Http方法","paramKey":"method","type":2},{"defaultValue":{"defaultValue":""},"description":"http请求的header","paramKey":"headers","type":1},{"defaultValue":{"defaultValue":""},"description":"请求的body内容","paramKey":"body","type":0}]', 1671003944283, 1675230014305);
INSERT INTO devops.execute_template
(id, template_id, template_type, service, `method`, name, author, description, source, param, create_time, update_time)
VALUES(5, '75196a0088be47cea7c58452f432d6e5', 1, 'com.zj.feature.ability.atop.AtopFeature', 'atopRequest', 'atop接口请求', 'admin', '发起atop接口请求', NULL, '[{"defaultValue":{"defaultValue":"${remoteUrl}"},"description":"atop接口域名","paramKey":"url","type":0},{"defaultValue":{"defaultValue":""},"description":"请求的atop接口","paramKey":"api","type":0},{"defaultValue":{"defaultValue":""},"description":"接口版本号","paramKey":"version","type":0},{"defaultValue":{"defaultValue":"${clientId}"},"description":"客户端唯一标识ID","paramKey":"clientId","type":0},{"defaultValue":{"defaultValue":"${secret}"},"description":"客户端密钥","paramKey":"secret","type":0},{"defaultValue":{"defaultValue":""},"description":"请求的参数，json字符串格式","paramKey":"postData","type":0},{"defaultValue":{"defaultValue":"${sessionId}"},"description":"用户sessionId","paramKey":"sid","type":0}]', 1672900982693, 1677494163700);
INSERT INTO devops.execute_template
(id, template_id, template_type, service, `method`, name, author, description, source, param, create_time, update_time)
VALUES(6, '566635e07a0d4e62a39abec42f3cd610', 1, 'com.zj.feature.ability.highway.HighwayFeature', 'requestHighway', 'Highway请求', 'admin', '发起Highway接口请求', NULL, '[{"defaultValue":{"defaultValue":"${remoteUrl}"},"description":"highway接口url","paramKey":"url","type":0},{"defaultValue":{"defaultValue":"","range":["GET","PUT","DELETE","POST"]},"description":"http请求方法","paramKey":"method","type":2},{"defaultValue":{"defaultValue":""},"description":"请求的内容","paramKey":"body","type":0},{"defaultValue":{"defaultValue":"${clientId}"},"description":"请求的clientId(IOT开放平台)","paramKey":"appKey","type":0},{"defaultValue":{"defaultValue":"${secret}"},"description":"请求的secret(IOT开放平台)","paramKey":"appSecret","type":0},{"defaultValue":{"defaultValue":""},"description":"请求的token，需要时填写","paramKey":"accessToken","type":0},{"defaultValue":{"defaultValue":""},"description":"http请求的header","paramKey":"header","type":1}]', 1672998035742, 1677227401010);
INSERT INTO devops.execute_template
(id, template_id, template_type, service, `method`, name, author, description, source, param, create_time, update_time)
VALUES(7, '9dcb1e34d902487fa2e5698a14dbb552', 1, 'com.zj.feature.ability.kafka.KafkaFeature', 'startConsume', 'kafka消费', 'guyuelan', '消费kafka消息', NULL, '[{"description":"kafka地址ip:port格式","paramKey":"address","type":0},{"description":"kafka消费topic","paramKey":"topic","type":0},{"description":"kafka消费群组","paramKey":"group","type":0}]', 1673421517596, 1673421517596);
INSERT INTO devops.execute_template
(id, template_id, template_type, service, `method`, name, author, description, source, param, create_time, update_time)
VALUES(8, 'ef4fa4c63cc8488c801590eca36580d4', 1, 'com.zj.feature.ability.kafka.KafkaFeature', 'produceMessage', '发送Kafak消息', 'guyuelan', '发送kafka消息', NULL, '[{"description":"发送的topic","paramKey":"topic","type":0},{"description":"发送消息的key","paramKey":"key","type":0},{"description":"发送kafka消息内容","paramKey":"value","type":0},{"description":"发送超时时间，单位秒","paramKey":"timeout","type":3},{"description":"kafka地址格式ip:port","paramKey":"address","type":0}]', 1673422627042, 1673423118348);
INSERT INTO devops.execute_template
(id, template_id, template_type, service, `method`, name, author, description, source, param, create_time, update_time)
VALUES(9, 'd9329caeea7340f4bec38d5727e521c8', 1, 'com.zj.feature.ability.redis.RedisFeature', 'setValue', 'Redis设置Value', 'guyuelan', '设置redis值', NULL, '[{"description":"redis实例IP","paramKey":"ip","type":0},{"description":"redis实例端口","paramKey":"port","type":3},{"description":"设置Key","paramKey":"key","type":0},{"description":"设置value","paramKey":"value","type":0},{"description":"超时时间","paramKey":"timeout","type":3}]', 1673431790811, 1673431790811);
INSERT INTO devops.execute_template
(id, template_id, template_type, service, `method`, name, author, description, source, param, create_time, update_time)
VALUES(10, 'a145661e75e040d0bb7be75fedf6c60e', 1, 'com.zj.feature.ability.redis.RedisFeature', 'getValue', '获取Redis值', 'guyuelan', '获取redis值', NULL, '[{"description":"redis实例ip","paramKey":"ip","type":0},{"description":"redis实例端口","paramKey":"port","type":3},{"description":"key","paramKey":"key","type":0}]', 1673431883618, 1673431883618);

