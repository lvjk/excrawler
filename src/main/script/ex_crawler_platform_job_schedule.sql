CREATE TABLE `ex_crawler_platform_job_schedule` (
  `jobname` varchar(50) NOT NULL COMMENT 'job  名字',
  `triggerTime` datetime NOT NULL COMMENT '触发 时间',
  `cycleTime` int(11) NOT NULL DEFAULT '0' COMMENT '循环触发周期  单位分 ',
  PRIMARY KEY (`jobname`,`triggerTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='job 调度 信息表';
INSERT INTO `ex_crawler_platform_job_schedule` VALUES ('qichacha', '2016-9-9 11:42:55', 3000);
