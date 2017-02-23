CREATE TABLE `ex_crawler_platform_paser_result` (
  `jobName` varchar(50) NOT NULL,
  `key` varchar(45) NOT NULL,
  `class` varchar(100) NOT NULL,
  PRIMARY KEY (`jobName`,`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `ex_crawler_platform_paser_result` VALUES ('qichachaDataUrl', 'dataUrl', 'java.util.ArrayList');
