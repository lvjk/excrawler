CREATE TABLE `ex_crawler_platform_paser_path` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL COMMENT 'name 为一类 可以对应多个',
  `siteCode` varchar(20) NOT NULL COMMENT '网站code',
  `ranking` int(11) NOT NULL DEFAULT '1' COMMENT '排名',
  `type` int(11) NOT NULL,
  `path` varchar(200) DEFAULT NULL COMMENT '规则路径',
  `filterPath` varchar(45) NOT NULL DEFAULT 'empty' COMMENT '需要过滤的 path',
  `reslutAttName` varchar(45) DEFAULT NULL COMMENT '获取值得 属性名字',
  `appendHead` varchar(45) DEFAULT NULL COMMENT '头部追加字符',
  `appendEnd` varchar(45) DEFAULT NULL COMMENT '尾部追加字符',
  `containKeyWord` varchar(45) DEFAULT NULL COMMENT '包含某个 字词',
  `replaceWord` varchar(45) DEFAULT NULL COMMENT '需要替换的字词',
  `replaceValue` varchar(45) DEFAULT NULL COMMENT '替换的值',
  `depth` int(11) DEFAULT '0' COMMENT '深度',
  `emptyExtractCount` int(11) NOT NULL DEFAULT '0' COMMENT '抽取结果为空的次数',
  `describe` varchar(20) NOT NULL COMMENT '规则描述',
  `compareAttName` varchar(45) DEFAULT NULL COMMENT '比较 的att name',
  PRIMARY KEY (`id`),
  KEY `sitecode_for_key_idx` (`siteCode`),
  KEY `type_for_key_idx` (`type`),
  CONSTRAINT `sitecode_for_key` FOREIGN KEY (`siteCode`) REFERENCES `ex_crawler_platform_site` (`code`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `type_for_key` FOREIGN KEY (`type`) REFERENCES `ex_crawler_platform_paser_result_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;
INSERT INTO `ex_crawler_platform_paser_path` VALUES (1, 'dataUrl', 'qichacha', 1, 1, 'div[class=col-md-12]>section>a', '', 'href', NULL, NULL, NULL, NULL, NULL, 0, 0, '企查查 列表页抽取 内容页url', NULL);
INSERT INTO `ex_crawler_platform_paser_path` VALUES (2, 'nextUrl', 'qichacha', 1, 1, 'div[class=text-left m-t-lg m-b-lg]>ul>li>a[class=next]', '', 'href', NULL, NULL, NULL, NULL, NULL, 0, 0, '企查查 列表页抽取下一页 url', NULL);
INSERT INTO `ex_crawler_platform_paser_path` VALUES (3, 'developerName', 'qichacha', 1, 2, 'span[class=clear]>span[class=text-big font-bold]', '', 'text', NULL, NULL, NULL, NULL, NULL, 1, 0, '抽取企查查 内容页  开发商 字符', NULL);
INSERT INTO `ex_crawler_platform_paser_path` VALUES (4, 'phone', 'qichacha', 1, 4, 'span[class=clear]>small', '', 'text', NULL, NULL, NULL, NULL, NULL, 1, 0, '抽取企查查 内容页  联系电话 字符', NULL);
INSERT INTO `ex_crawler_platform_paser_path` VALUES (5, 'website', 'qichacha', 1, 1, 'span[class=clear]>small[class=clear text-ellipsis m-t-xs text-md text-black]>a(2)', '', 'href', NULL, NULL, NULL, NULL, NULL, 1, 0, '抽取企查查 内容页  公司主页 字符', NULL);
INSERT INTO `ex_crawler_platform_paser_path` VALUES (6, 'corporater', 'qichacha', 1, 3, 'ul[class=company-base]>li', '', 'text', NULL, NULL, '法定代表：', '法定代表：', NULL, 1, 0, '抽取企查查 内容页  公司法定人', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (7, 'status', 'qichacha', 1, 3, 'ul[class=company-base]>li', '', 'text', NULL, NULL, '经营状态：', '经营状态：', NULL, 1, 0, '抽取企查查 内容页  公司经营状态：', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (8, 'foundDate', 'qichacha', 1, 3, 'ul[class=company-base]>li', '', 'text', NULL, NULL, '成立日期：', '成立日期：', NULL, 1, 0, '抽取企查查 内容页  公司成立日期：', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (9, 'registeredCapital', 'qichacha', 1, 3, 'ul[class=company-base]>li', '', 'text', NULL, NULL, '注册资本：', '注册资本：', NULL, 1, 0, '抽取企查查 内容页  公司注册资本：', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (10, 'address', 'qichacha', 1, 3, 'ul[class=company-base]>li', '', 'text', NULL, NULL, '企业地址：', '企业地址：', NULL, 1, 0, '抽取企查查 内容页  公司企业地址：', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (11, 'operateRange', 'qichacha', 1, 3, 'ul[class=company-base]>li', '', 'text', NULL, NULL, '经营范围：', '经营范围：', NULL, 1, 0, '抽取企查查 内容页  公司经营范围：', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (12, 'operatingPeriod', 'qichacha', 1, 3, 'ul[class=company-base]>li', '', 'text', NULL, NULL, '营业期限：', '营业期限：', NULL, 1, 0, '抽取企查查 内容页  公司营业期限：', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (13, 'issueDate', 'qichacha', 1, 3, 'ul[class=company-base]>li', '', 'text', NULL, NULL, '发照日期：', '发照日期：', NULL, 1, 0, '抽取企查查 内容页  公司发照日期：', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (14, 'dataUrl', 'cqfcglj', 1, 3, 'table[class=table1 table2]>tbody>tr>td>a', 'empty', 'href', NULL, NULL, NULL, NULL, NULL, 1, 0, '抽取重庆房产管理局商品房预售许可 内容页', '');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (15, 'projectName', 'cqfcglj', 1, 3, 'div[class=wrap-c-m]>table>tbody>tr>td>table>tbody>tr', 'empty', 'text', NULL, NULL, '项目名称', '项目名称', NULL, 0, 0, '项目名称', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (16, 'companyName', 'cqfcglj', 1, 3, 'div[class=wrap-c-m]>table>tbody>tr>td>table>tbody>tr', 'empty', 'text', NULL, NULL, '售房单位名称', '售房单位名称', NULL, 0, 0, '售房单位名称', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (17, 'houseAddress', 'cqfcglj', 1, 3, 'div[class=wrap-c-m]>table>tbody>tr>td>table>tbody>tr', 'empty', 'text', NULL, NULL, '房屋坐落', '房屋坐落', NULL, 0, 0, '房屋坐落', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (18, 'preSaleCode', 'cqfcglj', 1, 3, 'div[class=wrap-c-m]>table>tbody>tr>td>table>tbody>tr', 'empty', 'text', NULL, NULL, '预售证号', '预售证号', NULL, 0, 0, '预售证号', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (19, 'businessType', 'cqfcglj', 1, 3, 'div[class=wrap-c-m]>table>tbody>tr>td>table>tbody>tr', 'empty', 'text', NULL, NULL, '业务类型', '业务类型', NULL, 0, 0, '业务类型', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (20, 'issueDate', 'cqfcglj', 1, 3, 'div[class=wrap-c-m]>table>tbody>tr>td>table>tbody>tr', 'empty', 'text', NULL, NULL, '发证日期', '发证日期', NULL, 0, 0, '发证日期', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (21, 'preSalePeriod', 'cqfcglj', 1, 3, 'div[class=wrap-c-m]>table>tbody>tr>td>table>tbody>tr', 'empty', 'text', NULL, NULL, '预售期限', '预售期限', NULL, 0, 0, '预售期限', 'text');
INSERT INTO `ex_crawler_platform_paser_path` VALUES (23, 'nextUrl', 'cqfcglj', 1, 3, 'div[class=page]>a', 'empty', 'href', NULL, NULL, '下一页', NULL, NULL, 0, 0, '', 'text');
