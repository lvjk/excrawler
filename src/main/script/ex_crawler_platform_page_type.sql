CREATE TABLE `ex_crawler_platform_page_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `describe` varchar(45) DEFAULT NULL COMMENT 'page类型描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
INSERT INTO `ex_crawler_platform_page_type` VALUES (1, 'listinghtml');
INSERT INTO `ex_crawler_platform_page_type` VALUES (2, 'datahtml');
INSERT INTO `ex_crawler_platform_page_type` VALUES (3, 'xml');
INSERT INTO `ex_crawler_platform_page_type` VALUES (4, 'json');
