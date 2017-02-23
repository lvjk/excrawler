CREATE TABLE `ex_crawler_platform_job_parameter` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `jobName` varchar(50) NOT NULL,
  `attName` varchar(45) NOT NULL,
  `attValue` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (3, 'qichacha', 'resultStoreClass', 'six.com.crawler.work.processor.store.DBStoreProcessor');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (4, 'qichacha', 'sendHttpUrl', 'http://172.18.84.241:8080/crm/v1/developer/orginal/add');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (5, 'qichachaDataUrl', 'seedPageMd5', '54502186FB45EE4E6FDE87F9B4CB2486');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (6, 'qichacha', 'siteCode', 'qichacha');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (7, 'qichachaDataUrl', 'resultStoreClass', 'six.com.crawler.work.processor.store.ConsoleStoreProcessor');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (8, 'qichacha', 'sendHttpMethod', 'post');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (9, 'qichacha', 'batchSize', '1');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (10, 'qichachaDataUrl', 'siteCode', 'qichacha');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (11, 'chongqihouse', 'siteCode', 'cqfcglj');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (12, 'chongqihouse', 'seedPageMd5', '6FEFA6C64B1F9D496AC509EEAD660621');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (13, 'chongqihouse', 'resultStoreClass', 'six.com.crawler.work.processor.store.DBStoreProcessor');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (14, 'chongqihouse', 'dbSql', 'insert into ex_crawler_dc_chongqihouse(projectName,companyName,houseAddress,preSaleCode,businessType,issueDate,preSalePeriod,collectionDate,originUrl) value(?,?,?,?,?,?,?,?,?)');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (15, 'chongqihouse', 'dbDriverClassName', 'com.mysql.jdbc.Driver');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (16, 'chongqihouse', 'dbUser', 'root');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (17, 'chongqihouse', 'dbPasswd', '123456');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (18, 'chongqihouse', 'dbUrl', 'jdbc:mysql://172.18.84.44:3306/test?user=root&password=123456&useUnicode=true&characterEncoding=UTF8');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (19, 'chongqihouse', 'batchSize', '1');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (20, 'qichacha', 'dbSql', 'insert into ex_crawler_dc_realty_test(developer,corporater,tel,address,operate_range,found_date,registered_capital,operating_period,issue_date,`status`,company_url,city,collect_date,origin_url) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (21, 'qichacha', 'dbDriverClassName', 'com.mysql.jdbc.Driver');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (22, 'qichacha', 'dbUser', 'root');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (23, 'qichacha', 'dbPasswd', '123456');
INSERT INTO `ex_crawler_platform_job_parameter` VALUES (24, 'qichacha', 'dbUrl', 'jdbc:mysql://172.18.84.44:3306/test?user=root&password=123456&useUnicode=true&characterEncoding=UTF8');
