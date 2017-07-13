/*
Navicat MySQL Data Transfer

Source Server         : 172.30.103.83(excrawler)
Source Server Version : 50718
Source Host           : 172.30.103.83:3306
Source Database       : excra_meta

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-07-13 15:23:59
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ex_crawler_platform_site_page
-- ----------------------------
DROP TABLE IF EXISTS `ex_crawler_platform_site_page`;
CREATE TABLE `ex_crawler_platform_site_page` (
  `jobName` varchar(50) NOT NULL,
  `jobSnapshotId` varchar(50) NOT NULL,
  `siteCode` varchar(20) NOT NULL COMMENT '页面所属的 站点code',
  `pageKey` varchar(32) NOT NULL COMMENT '页面pageKey',
  `pageUrl` varchar(500) DEFAULT NULL,
  `pageSrc` mediumtext,
  `data` mediumblob NOT NULL COMMENT '页面对象序列化bytes',
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` int(11) NOT NULL DEFAULT '1' COMMENT '数据版本号',
  PRIMARY KEY (`siteCode`,`pageKey`,`jobSnapshotId`,`jobName`),
  KEY `site_code_foreign_key_in` (`siteCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='爬虫平台,列表页面表';
