CREATE TABLE `ex_crawler_platform_job` (
  `name` varchar(50) NOT NULL COMMENT 'job 名字',
  `type` int(11) NOT NULL COMMENT 'job类型',
  `nodeName` varchar(45) NOT NULL COMMENT 'job 属于哪个节点 的节点名字',
  `level` int(11) NOT NULL COMMENT 'job 级别',
  `needNodes` int(11) NOT NULL DEFAULT '1' COMMENT 'job工作需要的节点数',
  `threads` int(11) NOT NULL DEFAULT '1' COMMENT 'job单节点上运行的线程数',
  `everyProcessDelayTime` int(11) NOT NULL DEFAULT '100' COMMENT 'job每次处理时间的阈值 默认100毫秒',
  `user` varchar(45) NOT NULL DEFAULT 'admin',
  `describe` varchar(45) DEFAULT NULL COMMENT '任务描述',
  `queueName` varchar(45) DEFAULT NULL COMMENT '消息队列名字',
  `isTrigger` int(11) NOT NULL DEFAULT '0' COMMENT '0 不开启触发 1开启触发',
  `state` int(11) NOT NULL DEFAULT '0' COMMENT '//job 状态 0 空闲 1待执行任务  2正在执行任务 ',
  `workerClass` varchar(100) NOT NULL DEFAULT 'sss',
  PRIMARY KEY (`name`),
  KEY `job_type_idx` (`type`),
  CONSTRAINT `job_type` FOREIGN KEY (`type`) REFERENCES `ex_crawler_platform_job_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `ex_crawler_platform_job` VALUES ('chongqihouse', 1, 'node1', 1, 2, 1, 2000, 'admin', '重庆房产管理局', 'chongqihouse', 0, 0, 'six.com.crawler.work.HtmlJobWorker');
INSERT INTO `ex_crawler_platform_job` VALUES ('qichacha', 1, 'node1', 1, 3, 1, 6000, 'admin', '采集 企查查 企业信息', 'qichacha', 0, 0, 'six.com.crawler.work.HtmlJobWorker');
INSERT INTO `ex_crawler_platform_job` VALUES ('qichachaDataUrl', 1, 'node1', 1, 1, 1, 2000, 'admin', '采集 企查查 企业信息内容页url', 'qichacha', 0, 0, 'six.com.crawler.work.HtmlJobWorker');
INSERT INTO `ex_crawler_platform_job` VALUES ('testJob', 1, 'node-test', 1, 1, 1, 222, 'admin', '测试任务', 'test', 1, 0, 'six.com.crawler.work.HtmlJobWorker');
