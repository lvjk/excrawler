CREATE TABLE `ex_crawler_platform_paser_component` (
  `name` varchar(45) NOT NULL,
  `jobName` varchar(50) NOT NULL,
  `serialNub` int(11) NOT NULL DEFAULT '1',
  `output` int(11) NOT NULL DEFAULT '1' COMMENT '0 不输出 1输出 结果',
  `type` int(11) NOT NULL COMMENT '结果类型',
  `paserClassName` varchar(200) NOT NULL COMMENT '解析类 名',
  `resultKey` varchar(45) NOT NULL COMMENT '结果key',
  `mustHaveResult` int(11) NOT NULL DEFAULT '0' COMMENT '是否 必须有值 1是的  0否',
  `depth` int(11) NOT NULL DEFAULT '0' COMMENT '网站解析深度',
  `describe` varchar(50) DEFAULT NULL,
  `pageType` int(11) NOT NULL COMMENT '解析的页面类型 LISTING(1), DATA(2), XML(3), JSON(4);',
  `isList` int(11) NOT NULL DEFAULT '0' COMMENT '0 非集合  1集合',
  PRIMARY KEY (`name`,`jobName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='解析组件信息表';
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('address', 'qichacha', 4, 1, 2, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'address', 0, 1, '解析公司地址', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('businessType', 'chongqihouse', 6, 1, 0, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'businessType', 0, 1, '业务类型', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('city', 'qichacha', 12, 1, 9, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'city', 0, 0, '解析公司所在城市', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('companyName', 'chongqihouse', 3, 1, 0, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'companyName', 0, 1, '单位名称', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('corporater', 'qichacha', 2, 1, 2, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'corporater', 0, 1, '解析公司法定人', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('dataUrl', 'chongqihouse', 0, 0, 0, 'six.com.crawler.work.processor.paser.HtmlDataUrlPaserProcessor', 'dataUrl', 0, 1, '解析房产预售内容页', 1, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('dataUrl', 'qichacha', 1, 0, 1, 'six.com.crawler.work.processor.paser.HtmlDataUrlPaserProcessor', 'newUrl', 0, 0, '解析数据页面Url', 1, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('dataUrl', 'qichachaDataUrl', 1, 0, 1, 'six.com.crawler.work.processor.paser.HtmlDataUrlPaserProcessor', 'newUrl', 0, 1, '解析数据页面Url', 1, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('developerName', 'qichacha', 1, 1, 2, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'developerName', 1, 1, '解析公司名字', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('foundDate', 'qichacha', 6, 1, 6, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'foundDate', 0, 1, '解析公司注册日期', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('houseAddress', 'chongqihouse', 4, 1, 0, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'houseAddress', 0, 1, '房屋地址', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('issueDate', 'chongqihouse', 7, 1, 0, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'issueDate', 0, 1, '发证日期', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('issueDate', 'qichacha', 9, 1, 2, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'issueDate', 0, 1, '发照日期', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('nextUrl', 'chongqihouse', 1, 0, 0, 'six.com.crawler.work.processor.paser.HtmlNextUrlPaserProcessor', 'nextUrl', 0, 1, '解析房产预售列表页下一页', 1, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('nextUrl', 'qichacha', 1, 0, 1, 'six.com.crawler.work.processor.paser.HtmlNextUrlPaserProcessor', 'nextUrl', 0, 0, '获取企查查列表页下一页', 1, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('operateRange', 'qichacha', 5, 1, 2, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'operateRange', 0, 1, '公司经营范围', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('operatingPeriod', 'qichacha', 8, 1, 2, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'operatingPeriod', 0, 1, '公司经营期限', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('phone', 'qichacha', 3, 1, 4, 'six.com.crawler.work.processor.paser.HtmlTelPhonePaserProcessor', 'phone', 0, 1, '解析公司电话', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('preSaleCode', 'chongqihouse', 5, 1, 0, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'preSaleCode', 0, 1, '预售证号', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('preSalePeriod', 'chongqihouse', 8, 1, 0, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'preSalePeriod', 0, 1, '预售期限', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('projectName', 'chongqihouse', 2, 1, 0, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'projectName', 0, 1, '项目名称', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('registeredCapital', 'qichacha', 7, 1, 2, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'registeredCapital', 0, 1, '公司注册资本', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('status', 'qichacha', 10, 1, 2, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'status', 0, 1, '解析公司状态', 2, 0);
INSERT INTO `ex_crawler_platform_paser_component` VALUES ('website', 'qichacha', 11, 1, 1, 'six.com.crawler.work.processor.paser.HtmlStringPaserProcessor', 'website', 0, 1, '解析公司主页', 2, 0);
