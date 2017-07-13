/*
Navicat MySQL Data Transfer

Source Server         : 172.30.103.83(excrawler)
Source Server Version : 50718
Source Host           : 172.30.103.83:3306
Source Database       : excra_meta

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-07-13 15:23:19
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ex_crawler_platform_page_type
-- ----------------------------
DROP TABLE IF EXISTS `ex_crawler_platform_page_type`;
CREATE TABLE `ex_crawler_platform_page_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `describe` varchar(45) DEFAULT NULL COMMENT 'page????????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ex_crawler_platform_page_type
-- ----------------------------
INSERT INTO `ex_crawler_platform_page_type` VALUES ('1', 'listinghtml');
INSERT INTO `ex_crawler_platform_page_type` VALUES ('2', 'datahtml');
INSERT INTO `ex_crawler_platform_page_type` VALUES ('3', 'xml');
INSERT INTO `ex_crawler_platform_page_type` VALUES ('4', 'json');
