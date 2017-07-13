/*
Navicat MySQL Data Transfer

Source Server         : 172.30.103.83(excrawler)
Source Server Version : 50718
Source Host           : 172.30.103.83:3306
Source Database       : excra_meta

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-07-13 15:23:07
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ex_crawler_platform_node
-- ----------------------------
DROP TABLE IF EXISTS `ex_crawler_platform_node`;
CREATE TABLE `ex_crawler_platform_node` (
  `name` varchar(50) NOT NULL,
  `clusterName` varchar(50) NOT NULL,
  `ip` varchar(45) NOT NULL COMMENT '?ڵ?ip',
  `port` int(11) NOT NULL COMMENT '?ڵ??????˿',
  `trafficPort` int(11) NOT NULL COMMENT '?ڵ???ͨ?? ?˿',
  `user` varchar(45) DEFAULT NULL COMMENT '?ڵ??û???',
  `passwd` varchar(45) DEFAULT NULL COMMENT '?ڵ????',
  `status` int(11) DEFAULT NULL COMMENT '״̬',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ex_crawler_platform_node
-- ----------------------------
INSERT INTO `ex_crawler_platform_node` VALUES ('node1', '测试集群', 'http://192.168.12.83/', '8080', '9080', null, null, null);
