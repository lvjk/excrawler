/*
Navicat MySQL Data Transfer

Source Server         : 172.30.103.83(excrawler)
Source Server Version : 50718
Source Host           : 172.30.103.83:3306
Source Database       : excra_meta

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-07-13 15:23:41
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ex_crawler_platform_paser_result_type
-- ----------------------------
DROP TABLE IF EXISTS `ex_crawler_platform_paser_result_type`;
CREATE TABLE `ex_crawler_platform_paser_result_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '类型id',
  `describe` varchar(20) NOT NULL COMMENT '类型说明',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ex_crawler_platform_paser_result_type
-- ----------------------------
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES ('1', 'url类型');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES ('2', 'string 字符类型');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES ('3', 'text 文本类型');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES ('4', 'phone 电话类型');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES ('5', 'number 数字类型');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES ('6', 'date 日期类型');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES ('7', '数据表格形式');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES ('8', 'PRESETRESULT 预先设置的值');
INSERT INTO `ex_crawler_platform_paser_result_type` VALUES ('9', 'meta 页面meta 信息');
