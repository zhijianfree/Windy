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
  UNIQUE KEY `code_change_change_id_IDX` (`change_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4


CREATE TABLE `environment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `env_name` varchar(50) NOT NULL COMMENT '环境名称',
  `env_host` varchar(50) DEFAULT NULL COMMENT '环境的IP',
  `env_port` varchar(6) DEFAULT NULL COMMENT '环境端口',
  `env_status` int DEFAULT NULL COMMENT '环境状态 1 正常 2 暂停 3 已删除',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4

CREATE TABLE `pipeline` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pipeline_id` varchar(64) NOT NULL,
  `pipeline_name` varchar(100) DEFAULT NULL COMMENT '流水线名称',
  `service_id` varchar(100) NOT NULL COMMENT '服务Id',
  `pipeline_type` int(2) DEFAULT NULL COMMENT '流水线类型 1 发布流水线 2 每日构建流水线 3 个人流水线',
  `pipeline_config` text COMMENT '流水线配置信息',
  `pipeline_status` int(11) DEFAULT '1' COMMENT '流水线状态',
  `creator` varchar(50) DEFAULT NULL COMMENT '流水线创建者',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `pipeline_pipeline_id_IDX` (`pipeline_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `pipeline_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `history_id` varchar(64) NOT NULL COMMENT '执行历史Id',
  `pipeline_id` varchar(100) NOT NULL COMMENT '流水线id',
  `pipeline_name` varchar(100) DEFAULT NULL COMMENT '流水线名称',
  `branch` varchar(100)  NOT NULL COMMENT '流水线运行的分支',
  `pipeline_config` text NOT NULL COMMENT '流水线执行的配置',
  `pipeline_result` varchar(100) NOT NULL COMMENT '流水线执行结果',
  `executor` varchar(50) DEFAULT NULL COMMENT '执行人',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `pipeline_history_history_id_IDX` (`history_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流水线执行历史'


CREATE TABLE `git_bind` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bind_id` varchar(64) NOT NULL COMMENT '绑定Id',
  `git_branch` varchar(100) NOT NULL COMMENT '绑定分支',
  `git_url` varchar(2000) NOT NULL COMMENT 'git地址',
  `pipeline_id` varchar(100) NOT NULL COMMENT '流水线Id',
  `bind_type` int(11) NOT NULL,
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `git_bind_bind_id_IDX` (`bind_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `node_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_id` varchar(100) NOT NULL COMMENT '配置ID',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `type` int DEFAULT NULL COMMENT '配置类型',
  `config_detail` text COMMENT '配置详情',
  `order` int DEFAULT NULL COMMENT '排序',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4


CREATE TABLE `pipeline_stage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `stage_id` varchar(64) NOT NULL COMMENT '阶段Id',
  `pipeline_id` varchar(64) NOT NULL COMMENT '流水线Id',
  `stage_name` varchar(100) NOT NULL COMMENT '阶段名称',
  `type` int(11) DEFAULT '0',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB

CREATE TABLE `pipeline_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `node_id` varchar(64) NOT NULL COMMENT 'nodeId',
  `stage_id` varchar(64) NOT NULL COMMENT '阶段Id',
  `pipeline_id` varchar(64) NOT NULL COMMENT '流水线Id',
  `node_name` varchar(100) NOT NULL COMMENT '节点名称',
  `type` int(11) DEFAULT NULL COMMENT '节点类型',
  `config_detail` varchar(1000) DEFAULT NULL COMMENT '节点配置',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4


CREATE TABLE `node_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `record_id` varchar(64) NOT NULL COMMENT '记录Id',
  `task_id` varchar(64) NOT NULL COMMENT '任务Id',
  `code` int(11) DEFAULT NULL COMMENT '处理结果状态码',
  `result` varchar(2000) DEFAULT NULL COMMENT '任务处理结果',
  `status` int(11) DEFAULT NULL COMMENT '任务状态',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4


CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_id` varchar(100) NOT NULL COMMENT '配置ID',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `parent_id` varchar(100)  DEFAULT NULL COMMENT '父节点Id',
  `type` int DEFAULT NULL COMMENT '配置类型',
  `config_detail` text COMMENT '配置详情',
  `sort` int DEFAULT NULL COMMENT '排序',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4