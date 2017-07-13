/*
Navicat MySQL Data Transfer

Source Server         : 172.30.103.83(excrawler)
Source Server Version : 50718
Source Host           : 172.30.103.83:3306
Source Database       : excra_meta

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-07-13 15:23:50
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ex_crawler_platform_site
-- ----------------------------
DROP TABLE IF EXISTS `ex_crawler_platform_site`;
CREATE TABLE `ex_crawler_platform_site` (
  `code` varchar(20) NOT NULL COMMENT '站点 code',
  `mainurl` varchar(100) NOT NULL COMMENT '主页 url',
  `visitFrequency` int(11) NOT NULL COMMENT '站点访问频率',
  `describe` varchar(200) DEFAULT NULL COMMENT '站点描述',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ex_crawler_platform_site
-- ----------------------------
INSERT INTO `ex_crawler_platform_site` VALUES ('cq315house', 'http://www.cq315house.com/', '0', '重启网上房地产');
INSERT INTO `ex_crawler_platform_site` VALUES ('cqgtfw', 'http://www.cqgtfw.gov.cn/', '0', '重启房产管理局');
INSERT INTO `ex_crawler_platform_site` VALUES ('czfdc', 'http://www.czfdc.com.cn/', '0', '常州房地产信息网');
INSERT INTO `ex_crawler_platform_site` VALUES ('dldc_gx', 'http://218.25.171.244/InfoLayOut_GX/default.html', '0', '大连高新区地产网站');
INSERT INTO `ex_crawler_platform_site` VALUES ('dldc_jz', 'http://www.fczw.cn', '0', '大连金州地产网站');
INSERT INTO `ex_crawler_platform_site` VALUES ('dldc_ls', 'http://www.lsfcjy.gov.cn/default.html', '0', '大连旅顺地产网站');
INSERT INTO `ex_crawler_platform_site` VALUES ('dldc_sq', 'http://www.dlfd.gov.cn', '0', '大连市内四区地产网站');
INSERT INTO `ex_crawler_platform_site` VALUES ('fangdd', 'http://www.fangdd.com', '0', '房多多 房产信息网站');
INSERT INTO `ex_crawler_platform_site` VALUES ('fangtianxia', 'http://www1.fang.com/', '0', '房天下     房产信息网站');
INSERT INTO `ex_crawler_platform_site` VALUES ('lianjia', 'http://www.lianjia.com/', '0', '链家     房产信息网站');
INSERT INTO `ex_crawler_platform_site` VALUES ('nb_cnnbfdc', 'http://newhouse.cnnbfdc.com/', '3000', '宁波 住宅与房地产网');
INSERT INTO `ex_crawler_platform_site` VALUES ('qichacha', 'http://www.qichacha.com/', '0', '企查查 企业信息网站');
INSERT INTO `ex_crawler_platform_site` VALUES ('sh_fangdi', 'http://www.fangdi.com.cn/', '0', '上海网上房地产');
INSERT INTO `ex_crawler_platform_site` VALUES ('szpl_gov', 'http://www.szpl.gov.cn/', '0', '深圳市规划和国土资源委员会');
INSERT INTO `ex_crawler_platform_site` VALUES ('test_code', 'http://www.test.com/', '0', '测试站点');
INSERT INTO `ex_crawler_platform_site` VALUES ('tjfdc', 'http://www.tjfdc.com.cn/', '0', '天津市综合房地产信息网');
INSERT INTO `ex_crawler_platform_site` VALUES ('tmsf', 'http://www.tmsf.com/', '100', '杭州透明售房网');
INSERT INTO `ex_crawler_platform_site` VALUES ('xianfang99', 'http://www.fang99.com/', '0', '西安市房产资讯网');
INSERT INTO `ex_crawler_platform_site` VALUES ('zqfgzx', 'http://www.zqfgzx.org.cn/', '0', '章丘区房屋管理服务中心');
