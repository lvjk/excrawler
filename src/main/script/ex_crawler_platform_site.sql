CREATE TABLE `ex_crawler_platform_site` (
  `code` varchar(20) NOT NULL COMMENT '站点 code',
  `mainurl` varchar(45) NOT NULL COMMENT '主页 url',
  `proxy_enable` int(1) NOT NULL DEFAULT '0' COMMENT '是否需要 代理 0不需要  1需要',
  `localAddress_enable` int(1) NOT NULL DEFAULT '0',
  `downerType` int(1) NOT NULL DEFAULT '1',
  `describe` varchar(200) DEFAULT NULL COMMENT '站点描述',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `ex_crawler_platform_site` VALUES ('cqfcglj', 'http://www.cqgtfw.gov.cn/', 0, 0, 2, '重启房产管理局');
INSERT INTO `ex_crawler_platform_site` VALUES ('fangdd', 'http://www.fangdd.com', 0, 0, 1, '房多多 房产信息网站');
INSERT INTO `ex_crawler_platform_site` VALUES ('fangtianxia', 'http://www1.fang.com/', 0, 0, 1, '房天下     房产信息网站');
INSERT INTO `ex_crawler_platform_site` VALUES ('lianjia', 'http://www.lianjia.com/', 0, 0, 1, '链家     房产信息网站');
INSERT INTO `ex_crawler_platform_site` VALUES ('qichacha', 'http://www.qichacha.com/', 0, 0, 2, '企查查 企业信息网站');
INSERT INTO `ex_crawler_platform_site` VALUES ('test_code', 'http://www.test.com/', 0, 0, 1, '测试站点');
