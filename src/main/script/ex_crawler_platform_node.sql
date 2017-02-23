CREATE TABLE `ex_crawler_platform_node` (
  `name` varchar(50) NOT NULL,
  `clusterName` varchar(50) NOT NULL,
  `ip` varchar(45) NOT NULL COMMENT '节点ip',
  `port` int(11) NOT NULL COMMENT '节点服务端口',
  `trafficPort` int(11) NOT NULL COMMENT '节点间通信 端口',
  `user` varchar(45) DEFAULT NULL COMMENT '节点用户名',
  `passwd` varchar(45) DEFAULT NULL COMMENT '节点密码',
  `status` int(11) DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `ex_crawler_platform_node` VALUES ('node1', '测试集群', 'http://192.168.12.83/', 8080, 9080, NULL, NULL, NULL);
