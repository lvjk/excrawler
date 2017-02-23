CREATE TABLE `ex_crawler_platform_paser_result_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '类型id',
  `describe` varchar(20) NOT NULL COMMENT '类型说明',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES (1, 'url类型');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES (2, 'string 字符类型');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES (3, 'text 文本类型');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES (4, 'phone 电话类型');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES (5, 'number 数字类型');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES (6, 'date 日期类型');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES (7, '数据表格形式');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES (8, 'PRESETRESULT 预先设置的值');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES (9, 'meta 页面meta 信息');
