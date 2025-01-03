CREATE TABLE `microservice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_id` varchar(64) DEFAULT NULL COMMENT '服务Id',
  `git_url` varchar(1000) DEFAULT NULL COMMENT '服务git地址',
  `service_name` varchar(100) NOT NULL COMMENT '服务名称',
  `description` varchar(300) NOT NULL COMMENT '服务描述',
  `owner` varchar(100) NOT NULL COMMENT '服务拥有者',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4