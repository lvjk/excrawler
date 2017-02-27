CREATE TABLE `ex_crawler_platform_seed_page` (
  `siteCode` varchar(20) NOT NULL COMMENT '页面所属的 站点code',
  `urlMd5` varchar(32) NOT NULL COMMENT 'url md5 32位',
  `meta` varchar(500) DEFAULT NULL COMMENT '页面的 meta key value json',
  `update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `depth` int(11) NOT NULL DEFAULT '0' COMMENT '页面深度',
  `state` int(1) NOT NULL DEFAULT '1' COMMENT '列表也状态  1 表示有效 0表示无效',
  `originalUrl` varchar(500) DEFAULT NULL,
  `firstUrl` varchar(500) DEFAULT NULL,
  `finalUrl` varchar(500) DEFAULT NULL,
  `ancestorUrl` varchar(500) DEFAULT NULL,
  `referer` varchar(500) DEFAULT NULL,
  `pageNum` int(11) DEFAULT NULL,
  `charset` varchar(45) DEFAULT NULL,
  `downerType` int(11) NOT NULL DEFAULT '1' COMMENT '1为 httpclient 2为chrome 3为phantomjs(3暂时不支持)',
  `waitJsLoadElement` varchar(200) DEFAULT NULL,
  `type` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`siteCode`,`urlMd5`),
  KEY `site_code_foreign_key_in` (`siteCode`),
  CONSTRAINT `site_code_foreign_key_out` FOREIGN KEY (`siteCode`) REFERENCES `ex_crawler_platform_site` (`code`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='爬虫平台,列表页面表';
INSERT INTO `ex_crawler_platform_seed_page` VALUES ('cqfcglj', '6FEFA6C64B1F9D496AC509EEAD660621', NULL, '2016-9-20 16:07:51', 0, 1, 'http://www.cqgtfw.gov.cn/spjggs/fw/spfysxk/index.htm', 'http://www.cqgtfw.gov.cn/spjggs/fw/spfysxk/index.htm', 'http://www.cqgtfw.gov.cn/spjggs/fw/spfysxk/index.htm', 'null', 'null', 1, 'null', 2, 'null', 1);
