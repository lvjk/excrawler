/*
Navicat MySQL Data Transfer

Source Server         : 172.30.103.83(excrawler)
Source Server Version : 50718
Source Host           : 172.30.103.83:3306
Source Database       : excra_meta

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-07-13 15:21:42
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ex_crawler_platform_http_proxy
-- ----------------------------
DROP TABLE IF EXISTS `ex_crawler_platform_http_proxy`;
CREATE TABLE `ex_crawler_platform_http_proxy` (
  `host` varchar(100) NOT NULL,
  `port` int(11) NOT NULL,
  `type` int(11) NOT NULL DEFAULT '1',
  `userName` varchar(100) DEFAULT NULL,
  `passWord` varchar(100) DEFAULT NULL,
  `expire` varchar(100) DEFAULT NULL,
  `describe` varchar(200) DEFAULT NULL,
  `version` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`host`,`port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ex_crawler_platform_http_proxy
-- ----------------------------
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('114.115.214.209', '8888', '0', '', null, '', '', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.209.216', '8888', '0', '', null, '', '华为华东区exmind006', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.209.4', '8888', '0', '', null, '', '华为华东区exmind010', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.210.132', '8888', '0', 'excrawler', '123456', '', '华为华东爬虫服务器', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.210.177', '8888', '0', '', null, '', '华为华东区exmind007', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.210.198', '8888', '0', '', null, '', '华为华东区exmind008', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.210.241', '8888', '0', '', null, '', '华为华东区exmind010', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.210.242', '8888', '0', '', null, '', '华为华东区exmind008', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.210.246', '8888', '0', '', null, '', '华为华东区exmind007', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.210.247', '8888', '0', '', null, '', '华为华东区exmind006', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.214.232', '8888', '0', 'excrawler', '123456', '', '华为华东爬虫服务器', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.214.233', '8888', '0', 'excrawler', '123456', '', '华为华东爬虫服务器', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.214.245', '8888', '0', '', null, '', '', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.215.188', '8888', '0', '', null, '', '', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.215.215', '8888', '0', '', null, '', '', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.215.22', '8888', '0', '', null, '', '', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.215.222', '8888', '0', '', null, '', '', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('122.112.215.223', '8888', '0', '', null, '', '', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('139.159.214.210', '8888', '0', '', null, '', '华为华南区exmind009', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('139.159.217.72', '8888', '0', '', null, '', '华为华南区exmind002', '1');
INSERT INTO `ex_crawler_platform_http_proxy` VALUES ('139.159.219.67', '8888', '0', '', null, '', '华为华南区exmind002', '1');
