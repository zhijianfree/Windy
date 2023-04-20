#执行模版表
CREATE TABLE `execute_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_id` varchar(64) NOT NULL COMMENT '特性id',
  `template_type` int DEFAULT 1 COMMENT '模版类型',
  `service` varchar(500) DEFAULT NULL COMMENT '特性执行类名',
  `method` varchar(100) DEFAULT NULL COMMENT '特性执行方法名',
  `name` varchar(100) DEFAULT NULL COMMENT '特性执行名称',
  `author` varchar(100) DEFAULT NULL COMMENT '创建人',
  `description` varchar(100) DEFAULT NULL COMMENT '特性描述',
  `source` varchar(100) DEFAULT NULL COMMENT '特性原路径',
  `param` varchar(1000) DEFAULT NULL,
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

#执行点表
CREATE TABLE `execute_point` (
  `id` bigint DEFAULT NULL,
  `point_id` varchar(64) NOT NULL COMMENT '用例Id',
  `execute_type` int DEFAULT 1 COMMENT '执行类型',
  `feature_id` varchar(64) NOT NULL COMMENT '用例Id',
  `description` varchar(200) DEFAULT NULL  COMMENT '执行点描述',
  `feature_info` text  NOT NULL COMMENT '特性运行信息',
  `compare_define` text  COMMENT '特性执行结果比较',
  `variables` varchar(1000)  COMMENT '执行响应结果参数',
  `test_stage` int NOT NULL COMMENT '用例阶段',
  `sort_order` int DEFAULT NULL COMMENT '特性排序',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` varchar(100) DEFAULT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

#用例详情表
CREATE TABLE `feature_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `test_case_id` varchar(100)  DEFAULT NULL COMMENT '测试用例集ID',
  `feature_id` varchar(100)  DEFAULT NULL COMMENT '用例Id',
  `feature_name` varchar(100)  DEFAULT NULL COMMENT '用例名称',
  `author` varchar(100) DEFAULT NULL COMMENT '创建者',
  `modify` varchar(100) DEFAULT NULL COMMENT '修改人',
  `test_step` varchar(100) DEFAULT NULL COMMENT '测试步骤',
  `parent_id` varchar(100)  DEFAULT NULL COMMENT '父节点Id',
  `feature_type` int  DEFAULT 1 COMMENT '用例类型',
  `status` int DEFAULT NULL COMMENT '用例类型',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

#执行点执行记录表
CREATE TABLE `execute_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `execute_record_id` varchar(64) DEFAULT NULL COMMENT '执行点记录',
  `history_id` varchar(64) DEFAULT NULL COMMENT '历史记录ID',
  `status` int(11) DEFAULT NULL COMMENT '执行点运行状态',
  `execute_result` text COMMENT '执行结果',
  `execute_point_name` varchar(100) DEFAULT NULL COMMENT '执行点名称',
  `execute_point_id` varchar(100) DEFAULT NULL COMMENT '执行点ID',
  `execute_type` int(11) DEFAULT NULL COMMENT '执行类型',
  `test_stage` int(11) DEFAULT NULL COMMENT '测试阶段',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

#用例历史执行记录表
CREATE TABLE `feature_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `history_id` varchar(100) NOT NULL COMMENT '历史Id',
  `feature_id` varchar(64) NOT NULL,
  `feature_name` varchar(100) DEFAULT NULL COMMENT '用例名称',
  `record_id` varchar(100)  DEFAULT NULL COMMENT '执行记录Id',
  `executor` varchar(100) DEFAULT NULL COMMENT '执行人',
  `execute_status` int DEFAULT NULL COMMENT '执行状态',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

#测试集表
CREATE TABLE `test_case` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `test_case_id` varchar(64) NOT NULL COMMENT '用例集id',
  `author` varchar(100) DEFAULT NULL COMMENT '创建人',
  `service_id` varchar(100) DEFAULT NULL COMMENT '服务Id',
  `test_case_name` varchar(100) DEFAULT NULL COMMENT '用例集名称',
  `description` varchar(100) DEFAULT NULL COMMENT '用例集描述',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

#测试用例配置表
CREATE TABLE `test_case_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_id` varchar(100)  DEFAULT NULL COMMENT '配置ID',
  `union_id` varchar(100)  DEFAULT NULL COMMENT '关联Id',
  `parent_id` varchar(100)  DEFAULT NULL COMMENT '父节点Id',
  `type`  int DEFAULT NULL COMMENT '节点类型',
  `param_key` varchar(100) DEFAULT NULL COMMENT '参数key',
  `param_type` varchar(100) DEFAULT NULL COMMENT '参数类型',
  `value` varchar(100)  DEFAULT NULL COMMENT '参数值',
  `sort_order` varchar(500) DEFAULT NULL COMMENT '排序',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

#任务详情表
CREATE TABLE `task_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` varchar(100)  DEFAULT NULL COMMENT '任务ID',
  `user_id` varchar(100)  DEFAULT NULL COMMENT '创建者ID',
  `service_id` varchar(100)  DEFAULT NULL COMMENT '服务Id',
  `test_case_id` varchar(100)  DEFAULT NULL COMMENT '测试集Id',
  `task_name`  varchar(100) NOT NULL COMMENT '任务名称',
  `description` varchar(100) DEFAULT NULL COMMENT '任务描述',
  `task_config` varchar(2000) DEFAULT NULL COMMENT '任务执行参数',
  `machines` varchar(100)  DEFAULT NULL COMMENT '执行机器列表',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

#任务记录表
CREATE TABLE `task_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `record_id` varchar(100)  DEFAULT NULL COMMENT '任务记录ID',
  `task_id` varchar(100)  DEFAULT NULL COMMENT '任务ID',
  `user_id` varchar(100)  DEFAULT NULL COMMENT '执行者ID',
  `test_case_id` varchar(100)  DEFAULT NULL COMMENT '测试集Id',
  `status` int  DEFAULT NULL COMMENT '执行状态',
  `task_name`  varchar(100) NOT NULL COMMENT '任务名称',
  `task_config` varchar(2000) DEFAULT NULL COMMENT '任务执行参数',
  `machines` varchar(100)  DEFAULT NULL COMMENT '执行机器列表',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

#用例标签表
CREATE TABLE `feature_tag` (
  `feature_id` varchar(64) DEFAULT NULL COMMENT '用例Id',
  `tag_value` varchar(100) NOT NULL COMMENT '标签值',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
