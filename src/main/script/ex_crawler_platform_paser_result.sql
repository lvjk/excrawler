/*
Navicat MySQL Data Transfer

Source Server         : 172.30.103.83(excrawler)
Source Server Version : 50718
Source Host           : 172.30.103.83:3306
Source Database       : excra_meta

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-07-13 15:23:30
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ex_crawler_platform_paser_result
-- ----------------------------
DROP TABLE IF EXISTS `ex_crawler_platform_paser_result`;
CREATE TABLE `ex_crawler_platform_paser_result` (
  `jobName` varchar(50) NOT NULL,
  `key` varchar(45) NOT NULL,
  `class` varchar(100) NOT NULL,
  PRIMARY KEY (`jobName`,`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ex_crawler_platform_paser_result
-- ----------------------------
INSERT INTO `ex_crawler_platform_paser_result` VALUES ('qichachaDataUrl', 'dataUrl', 'java.util.ArrayList');
