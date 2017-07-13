/*
Navicat MySQL Data Transfer

Source Server         : 172.30.103.83(excrawler)
Source Server Version : 50718
Source Host           : 172.30.103.83:3306
Source Database       : excra_meta

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-07-13 15:22:28
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ex_crawler_platform_job_relationship
-- ----------------------------
DROP TABLE IF EXISTS `ex_crawler_platform_job_relationship`;
CREATE TABLE `ex_crawler_platform_job_relationship` (
  `currentJobName` varchar(100) NOT NULL COMMENT '当前job ',
  `nextJobName` varchar(100) NOT NULL DEFAULT '' COMMENT '下一个job',
  `executeType` int(1) DEFAULT '1' COMMENT '触发类型',
  `version` int(1) NOT NULL DEFAULT '0',
  `status` int(255) DEFAULT '1' COMMENT '判断是否生效，生效为1，失效为0',
  PRIMARY KEY (`currentJobName`,`nextJobName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ex_crawler_platform_job_relationship
-- ----------------------------
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('dldc_gx_project_info', 'dldc_gx_unit_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('dldc_gx_project_url', 'dldc_gx_project_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('dldc_gx_room_state_info', 'dldc_gx_room_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('dldc_gx_unit_info', 'dldc_gx_room_state_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('dldc_sq_project_info', 'dldc_sq_unit_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('dldc_sq_project_url', 'dldc_sq_project_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('dldc_sq_unit_info', 'dldc_sq_room_state_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('nb_cnnbfdc_presell_list', 'nb_cnnbfdc_presell_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('nb_cnnbfdc_presell_list', 'nb_cnnbfdc_presell_list_monitor', '2', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('nb_cnnbfdc_project_info', 'nb_cnnbfdc_unit_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('nb_cnnbfdc_project_list', 'nb_cnnbfdc_project_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('nb_cnnbfdc_project_list', 'nb_cnnbfdc_project_list_monitor', '2', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('nb_cnnbfdc_room_state_info', 'nb_cnnbfdc_room_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('nb_cnnbfdc_unit_info', 'nb_cnnbfdc_room_state_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('tmsf_house_status', 'tmsf_project_info_1', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('tmsf_house_status_1', 'tmsf_house_info', '1', '0', '0');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('tmsf_house_url', 'tmsf_house_status', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('tmsf_presell_info', 'tmsf_house_url', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('tmsf_presell_info_1', 'tmsf_house_status_1', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('tmsf_presell_url', 'tmsf_presell_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('tmsf_presell_url_1', 'tmsf_presell_info_1', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('tmsf_project_info', 'tmsf_presell_url', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('tmsf_project_info_1', 'tmsf_presell_url_1', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('tmsf_project_list', 'tmsf_project_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('zqfgzx_presell_info', 'zqfgzx_unit_info', '1', '0', '1');
INSERT INTO `ex_crawler_platform_job_relationship` VALUES ('zqfgzx_unit_info', 'zqfgzx_room_state_info', '1', '0', '1');
