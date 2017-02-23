CREATE TABLE `ex_crawler_platform_job_type` (
  `id` int(11) NOT NULL COMMENT 'job type id',
  `describe` varchar(50) NOT NULL COMMENT 'job type描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `ex_crawler_platform_job_type` VALUES (1, '网页html job 类型');
