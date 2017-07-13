/*
Navicat MySQL Data Transfer

Source Server         : 172.30.103.83(excrawler)
Source Server Version : 50718
Source Host           : 172.30.103.83:3306
Source Database       : excra_meta

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-07-13 15:22:36
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ex_crawler_platform_job_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `ex_crawler_platform_job_snapshot`;
CREATE TABLE `ex_crawler_platform_job_snapshot` (
  `id` varchar(200) NOT NULL,
  `name` varchar(200) NOT NULL,
  `tableName` varchar(200) DEFAULT NULL COMMENT '任务存储数据表名',
  `startTime` datetime NOT NULL,
  `endTime` datetime NOT NULL,
  `status` int(1) NOT NULL DEFAULT '1' COMMENT '状态:\nREADY(1)//准备\nWAITING_EXECUTED(2)//等待执行\nEXECUTING(3)//执行\nSUSPEND(4)//暂停\nSTOP(5)//停止\nFINISHED(6)//完成',
  `totalProcessCount` int(11) NOT NULL DEFAULT '0',
  `totalResultCount` int(11) NOT NULL DEFAULT '0',
  `totalProcessTime` int(11) DEFAULT NULL,
  `avgProcessTime` int(11) NOT NULL DEFAULT '0',
  `maxProcessTime` int(11) NOT NULL DEFAULT '0',
  `minProcessTime` int(11) NOT NULL DEFAULT '0',
  `errCount` int(11) NOT NULL DEFAULT '0',
  `downloadState` int(11) DEFAULT NULL COMMENT '0-未下载,1-下载完成',
  `runtimeParams` text,
  `version` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='任务执行快照信息表';

-- ----------------------------
-- Records of ex_crawler_platform_job_snapshot
-- ----------------------------
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707021000', 'zqfgzx_presell_info', null, '2017-07-07 02:10:00', '2017-07-07 02:11:59', '6', '90', '1787', '93156', '1035', '1346', '671', '0', null, '{\"table\":\"ex_dc_zqfgzx_presell_info_20170707021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707021000', 'zqfgzx_room_state_info', null, '2017-07-07 03:03:55', '2017-07-07 03:08:44', '6', '1783', '115367', '41226', '207', '1465', '58', '0', null, '{\"table\":\"ex_dc_zqfgzx_room_state_info_20170707021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707021000', 'zqfgzx_unit_info', null, '2017-07-07 02:11:59', '2017-07-07 03:03:55', '6', '1868', '1804', '3092670', '14950', '100256', '42', '85', null, '{\"table\":\"ex_dc_zqfgzx_unit_info_20170707021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707024000', 'zqfgzx_project_info', null, '2017-07-07 02:40:00', '2017-07-07 02:58:11', '6', '98', '1914', '1068516', '10903', '65052', '533', '2', null, '{\"table\":\"ex_dc_zqfgzx_project_info_20170707024000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707061000', 'nb_cnnbfdc_project_info', null, '2017-07-07 06:18:52', '2017-07-07 06:29:34', '6', '1660', '1647', '262073', '2407', '65122', '287', '13', null, '{\"table\":\"ex_dc_nb_cnnbfdc_project_info_20170707061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707061000', 'nb_cnnbfdc_project_list', null, '2017-07-07 06:10:00', '2017-07-07 06:18:52', '6', '92', '0', '419865', '4563', '6946', '626', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707061000', 'nb_cnnbfdc_project_list_monitor', null, '2017-07-07 06:10:00', '2017-07-07 06:25:37', '6', '3', '0', '115', '38', '51', '20', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707061000', 'nb_cnnbfdc_room_info', null, '2017-07-07 10:58:48', '2017-07-07 11:03:11', '3', '257', '228', '144753', '24497', '96094', '116', '29', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_info\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707061000', 'nb_cnnbfdc_room_state_info', null, '2017-07-07 06:41:38', '2017-07-07 10:58:48', '3', '18664', '852302', '14802705', '14283', '264301', '42', '64', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_state_info_20170707061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707061000', 'nb_cnnbfdc_unit_info', null, '2017-07-07 06:29:35', '2017-07-07 06:41:38', '6', '1646', '18604', '84760', '463', '6142', '77', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_unit_info_20170707061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707061100', 'nb_cnnbfdc_presell_info', null, '2017-07-07 06:25:27', '2017-07-07 06:45:45', '6', '2173', '2165', '542396', '2272', '132253', '112', '8', null, '{\"table\":\"ex_dc_nb_cnnbfdc_presell_info_20170707061100\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707061100', 'nb_cnnbfdc_presell_list', null, '2017-07-07 06:11:00', '2017-07-07 06:25:27', '6', '146', '0', '613423', '4201', '65106', '516', '1', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707061100', 'nb_cnnbfdc_presell_list_monitor', null, '2017-07-07 06:11:00', '2017-07-07 06:26:52', '6', '4', '0', '146', '36', '43', '20', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707120810', 'nb_cnnbfdc_room_info', null, '2017-07-07 12:09:31', '2017-07-07 12:09:32', '6', '0', '0', '0', '0', '0', '999999999', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_info\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170707120810', 'nb_cnnbfdc_room_state_info', null, '2017-07-07 12:08:10', '2017-07-07 12:09:31', '6', '5', '4', '27247', '27247', '80076', '999999999', '4', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_state_info_20170707061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170708021000', 'zqfgzx_presell_info', null, '2017-07-08 02:10:00', '2017-07-08 02:11:46', '6', '90', '1787', '96819', '1075', '1365', '707', '0', null, '{\"table\":\"ex_dc_zqfgzx_presell_info_20170708021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170708021000', 'zqfgzx_room_state_info', null, '2017-07-08 03:05:54', '2017-07-08 03:10:42', '6', '1783', '115367', '41625', '209', '1408', '60', '0', null, '{\"table\":\"ex_dc_zqfgzx_room_state_info_20170708021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170708021000', 'zqfgzx_unit_info', null, '2017-07-08 02:11:46', '2017-07-08 03:05:54', '6', '1856', '1804', '3205646', '15585', '99672', '42', '73', null, '{\"table\":\"ex_dc_zqfgzx_unit_info_20170708021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170708024000', 'zqfgzx_project_info', null, '2017-07-08 02:40:00', '2017-07-08 02:54:01', '6', '96', '1914', '806199', '8397', '18287', '562', '0', null, '{\"table\":\"ex_dc_zqfgzx_project_info_20170708024000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170708061000', 'nb_cnnbfdc_project_info', null, '2017-07-08 06:18:34', '2017-07-08 06:28:51', '6', '1656', '1647', '237080', '2186', '65113', '299', '9', null, '{\"table\":\"ex_dc_nb_cnnbfdc_project_info_20170708061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170708061000', 'nb_cnnbfdc_project_list', null, '2017-07-08 06:10:00', '2017-07-08 06:18:34', '6', '92', '0', '445766', '4845', '6649', '856', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170708061000', 'nb_cnnbfdc_project_list_monitor', null, '2017-07-08 06:10:00', '2017-07-08 06:34:00', '6', '3', '0', '93', '31', '42', '22', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170708061000', 'nb_cnnbfdc_room_info', null, '2017-07-08 11:56:35', '2017-07-08 11:56:36', '6', '0', '0', '0', '0', '0', '999999999', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_info\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170708061000', 'nb_cnnbfdc_room_state_info', null, '2017-07-08 06:41:03', '2017-07-08 11:56:35', '6', '18654', '852313', '18216826', '11721', '142539', '29', '54', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_state_info_20170708061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170708061000', 'nb_cnnbfdc_unit_info', null, '2017-07-08 06:28:51', '2017-07-08 06:41:03', '6', '1646', '18604', '87570', '479', '5113', '69', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_unit_info_20170708061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170708061100', 'nb_cnnbfdc_presell_info', null, '2017-07-08 06:24:12', '2017-07-08 06:42:09', '6', '2175', '2165', '381971', '1594', '65106', '103', '10', null, '{\"table\":\"ex_dc_nb_cnnbfdc_presell_info_20170708061100\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170708061100', 'nb_cnnbfdc_presell_list', null, '2017-07-08 06:11:00', '2017-07-08 06:24:12', '6', '145', '0', '468801', '3233', '8388', '528', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170708061100', 'nb_cnnbfdc_presell_list_monitor', null, '2017-07-08 06:11:00', '2017-07-08 06:36:27', '6', '5', '0', '169', '33', '42', '20', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170709021000', 'zqfgzx_presell_info', null, '2017-07-09 02:10:00', '2017-07-09 02:12:08', '6', '90', '1787', '92777', '1030', '1399', '685', '0', null, '{\"table\":\"ex_dc_zqfgzx_presell_info_20170709021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170709021000', 'zqfgzx_room_state_info', null, '2017-07-09 03:07:01', '2017-07-09 03:11:47', '6', '1783', '115367', '40406', '203', '1394', '55', '0', null, '{\"table\":\"ex_dc_zqfgzx_room_state_info_20170709021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170709021000', 'zqfgzx_unit_info', null, '2017-07-09 02:12:08', '2017-07-09 03:07:01', '6', '1860', '1804', '3235396', '15697', '114661', '43', '77', null, '{\"table\":\"ex_dc_zqfgzx_unit_info_20170709021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170709024000', 'zqfgzx_project_info', null, '2017-07-09 02:40:00', '2017-07-09 02:55:54', '6', '97', '1914', '904286', '9322', '65106', '510', '1', null, '{\"table\":\"ex_dc_zqfgzx_project_info_20170709024000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170709061000', 'nb_cnnbfdc_project_info', null, '2017-07-09 06:18:26', '2017-07-09 06:29:23', '6', '1661', '1647', '285955', '2625', '65121', '248', '14', null, '{\"table\":\"ex_dc_nb_cnnbfdc_project_info_20170709061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170709061000', 'nb_cnnbfdc_project_list', null, '2017-07-09 06:10:00', '2017-07-09 06:18:26', '6', '92', '0', '452186', '4915', '6750', '577', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170709061000', 'nb_cnnbfdc_project_list_monitor', null, '2017-07-09 06:10:00', '2017-07-09 06:22:47', '6', '3', '0', '97', '32', '40', '23', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170709061000', 'nb_cnnbfdc_room_info', null, '2017-07-09 12:03:56', '2017-07-09 12:03:57', '6', '0', '0', '0', '0', '0', '999999999', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_info\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170709061000', 'nb_cnnbfdc_room_state_info', null, '2017-07-09 06:41:18', '2017-07-09 12:03:56', '6', '18689', '852187', '18585617', '11939', '200136', '25', '89', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_state_info_20170709061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170709061000', 'nb_cnnbfdc_unit_info', null, '2017-07-09 06:29:23', '2017-07-09 06:41:18', '6', '1646', '18604', '84825', '463', '6943', '60', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_unit_info_20170709061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170709061100', 'nb_cnnbfdc_presell_info', null, '2017-07-09 06:24:57', '2017-07-09 06:46:35', '6', '2179', '2165', '562297', '2343', '86499', '120', '14', null, '{\"table\":\"ex_dc_nb_cnnbfdc_presell_info_20170709061100\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170709061100', 'nb_cnnbfdc_presell_list', null, '2017-07-09 06:11:00', '2017-07-09 06:24:57', '6', '145', '0', '567038', '3910', '31601', '532', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170709061100', 'nb_cnnbfdc_presell_list_monitor', null, '2017-07-09 06:11:00', '2017-07-09 06:31:03', '6', '5', '0', '190', '38', '45', '22', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710021000', 'zqfgzx_presell_info', null, '2017-07-10 02:10:00', '2017-07-10 02:12:13', '6', '90', '1787', '95302', '1058', '3225', '711', '0', null, '{\"table\":\"ex_dc_zqfgzx_presell_info_20170710021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710021000', 'zqfgzx_room_state_info', null, '2017-07-10 03:06:11', '2017-07-10 03:11:01', '6', '1783', '115367', '41560', '209', '1450', '58', '0', null, '{\"table\":\"ex_dc_zqfgzx_room_state_info_20170710021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710021000', 'zqfgzx_unit_info', null, '2017-07-10 02:12:13', '2017-07-10 03:06:11', '6', '1856', '1804', '3223753', '15652', '87485', '42', '73', null, '{\"table\":\"ex_dc_zqfgzx_unit_info_20170710021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710024000', 'zqfgzx_project_info', null, '2017-07-10 02:40:00', '2017-07-10 02:58:09', '6', '97', '1914', '1062764', '10956', '65099', '595', '1', null, '{\"table\":\"ex_dc_zqfgzx_project_info_20170710024000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710061000', 'nb_cnnbfdc_project_info', null, '2017-07-10 06:18:28', '2017-07-10 06:28:54', '6', '1660', '1647', '244731', '2253', '65116', '259', '13', null, '{\"table\":\"ex_dc_nb_cnnbfdc_project_info_20170710061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710061000', 'nb_cnnbfdc_project_list', null, '2017-07-10 06:10:00', '2017-07-10 06:18:28', '6', '92', '0', '446304', '4851', '6373', '815', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710061000', 'nb_cnnbfdc_project_list_monitor', null, '2017-07-10 06:10:00', '2017-07-10 06:30:20', '6', '3', '0', '123', '41', '55', '23', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710061000', 'nb_cnnbfdc_room_info', null, '2017-07-10 11:58:54', '2017-07-10 11:58:55', '3', '0', '0', '0', '0', '0', '999999999', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_info\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710061000', 'nb_cnnbfdc_room_state_info', null, '2017-07-10 06:41:03', '2017-07-10 11:58:54', '3', '18667', '849966', '18393805', '11825', '154190', '29', '69', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_state_info_20170710061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710061000', 'nb_cnnbfdc_unit_info', null, '2017-07-10 06:28:54', '2017-07-10 06:41:03', '6', '1646', '18604', '86148', '471', '5622', '65', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_unit_info_20170710061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710061100', 'nb_cnnbfdc_presell_info', null, '2017-07-10 06:25:13', '2017-07-10 06:44:43', '6', '2173', '2165', '471238', '1959', '77273', '112', '8', null, '{\"table\":\"ex_dc_nb_cnnbfdc_presell_info_20170710061100\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710061100', 'nb_cnnbfdc_presell_list', null, '2017-07-10 06:11:00', '2017-07-10 06:25:13', '6', '146', '0', '570232', '3905', '65098', '308', '1', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710061100', 'nb_cnnbfdc_presell_list_monitor', null, '2017-07-10 06:11:00', '2017-07-10 06:37:02', '6', '8', '0', '317', '39', '48', '23', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710145053', 'nb_cnnbfdc_room_info', null, '2017-07-10 14:56:14', '2017-07-10 14:56:15', '6', '0', '0', '0', '0', '0', '999999999', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_info\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170710145053', 'nb_cnnbfdc_room_state_info', null, '2017-07-10 14:50:53', '2017-07-10 14:56:14', '6', '21', '2647', '106980', '37473', '120363', '29', '16', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_state_info_20170710061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711021000', 'zqfgzx_presell_info', null, '2017-07-11 02:10:00', '2017-07-11 02:11:59', '6', '90', '1787', '99317', '1103', '2292', '775', '0', null, '{\"table\":\"ex_dc_zqfgzx_presell_info_20170711021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711021000', 'zqfgzx_room_state_info', null, '2017-07-11 03:01:19', '2017-07-11 03:06:07', '6', '1783', '115367', '41453', '208', '1196', '62', '0', null, '{\"table\":\"ex_dc_zqfgzx_room_state_info_20170711021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711021000', 'zqfgzx_unit_info', null, '2017-07-11 02:11:59', '2017-07-11 03:01:18', '6', '1859', '1804', '2943673', '14283', '124248', '41', '76', null, '{\"table\":\"ex_dc_zqfgzx_unit_info_20170711021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711024000', 'zqfgzx_project_info', null, '2017-07-11 02:40:00', '2017-07-11 02:54:27', '6', '96', '1914', '844249', '8794', '15647', '561', '0', null, '{\"table\":\"ex_dc_zqfgzx_project_info_20170711024000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711061000', 'nb_cnnbfdc_project_info', null, '2017-07-11 06:18:37', '2017-07-11 06:29:18', '6', '1664', '1650', '265128', '2446', '65115', '242', '14', null, '{\"table\":\"ex_dc_nb_cnnbfdc_project_info_20170711061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711061000', 'nb_cnnbfdc_project_list', null, '2017-07-11 06:10:00', '2017-07-11 06:18:37', '6', '92', '0', '431208', '4687', '6554', '860', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711061000', 'nb_cnnbfdc_project_list_monitor', null, '2017-07-11 06:10:00', '2017-07-11 06:31:42', '6', '5', '0', '257', '51', '106', '16', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711061000', 'nb_cnnbfdc_room_info', null, '2017-07-11 12:10:38', '2017-07-11 12:19:01', '3', '2228', '2217', '123184', '1011', '65106', '82', '11', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_info\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711061000', 'nb_cnnbfdc_room_state_info', null, '2017-07-11 06:41:24', '2017-07-11 12:10:38', '3', '18687', '859730', '19184068', '12321', '241892', '36', '62', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_state_info_20170711061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711061000', 'nb_cnnbfdc_unit_info', null, '2017-07-11 06:29:18', '2017-07-11 06:41:24', '6', '1649', '18626', '83388', '454', '4920', '71', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_unit_info_20170711061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711061100', 'nb_cnnbfdc_presell_info', null, '2017-07-11 06:24:26', '2017-07-11 06:42:44', '6', '2175', '2168', '377031', '1569', '65112', '111', '7', null, '{\"table\":\"ex_dc_nb_cnnbfdc_presell_info_20170711061100\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711061100', 'nb_cnnbfdc_presell_list', null, '2017-07-11 06:11:00', '2017-07-11 06:24:26', '6', '145', '0', '527189', '3635', '20594', '535', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711061100', 'nb_cnnbfdc_presell_list_monitor', null, '2017-07-11 06:11:00', '2017-07-11 06:28:14', '6', '4', '0', '130', '32', '45', '21', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711124010', 'nb_cnnbfdc_room_info', null, '2017-07-11 12:42:11', '2017-07-11 12:42:12', '6', '0', '0', '0', '0', '0', '999999999', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_info\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170711124010', 'nb_cnnbfdc_room_state_info', null, '2017-07-11 12:40:10', '2017-07-11 12:42:11', '6', '3', '2706', '15960', '15960', '120781', '999999999', '1', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_state_info_20170711061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170712021000', 'zqfgzx_presell_info', null, '2017-07-12 02:10:00', '2017-07-12 02:12:10', '6', '90', '1787', '93721', '1041', '1491', '789', '0', null, '{\"table\":\"ex_dc_zqfgzx_presell_info_20170712021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170712021000', 'zqfgzx_room_state_info', null, '2017-07-12 03:02:00', '2017-07-12 03:06:43', '6', '1783', '115367', '40683', '205', '1161', '59', '0', null, '{\"table\":\"ex_dc_zqfgzx_room_state_info_20170712021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170712021000', 'zqfgzx_unit_info', null, '2017-07-12 02:12:10', '2017-07-12 03:02:00', '6', '1857', '1804', '2899172', '14118', '87012', '38', '74', null, '{\"table\":\"ex_dc_zqfgzx_unit_info_20170712021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170712024000', 'zqfgzx_project_info', null, '2017-07-12 02:40:00', '2017-07-12 02:54:35', '6', '96', '1914', '850004', '8854', '20093', '571', '0', null, '{\"table\":\"ex_dc_zqfgzx_project_info_20170712024000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170712061000', 'nb_cnnbfdc_project_info', null, '2017-07-12 06:18:43', '2017-07-12 06:29:03', '6', '1666', '1652', '239220', '2200', '65128', '253', '14', null, '{\"table\":\"ex_dc_nb_cnnbfdc_project_info_20170712061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170712061000', 'nb_cnnbfdc_project_list', null, '2017-07-12 06:10:00', '2017-07-12 06:18:43', '6', '92', '0', '447764', '4867', '6793', '853', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170712061000', 'nb_cnnbfdc_project_list_monitor', null, '2017-07-12 06:10:00', '2017-07-12 06:33:33', '6', '5', '0', '195', '39', '46', '21', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170712061000', 'nb_cnnbfdc_room_info', null, '2017-07-12 11:59:48', '2017-07-12 12:05:09', '6', '1295', '1286', '103815', '1565', '65126', '106', '9', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_info\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170712061000', 'nb_cnnbfdc_room_state_info', null, '2017-07-12 06:41:21', '2017-07-12 11:59:48', '6', '18662', '861016', '18472851', '11880', '170746', '23', '28', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_state_info_20170712061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170712061000', 'nb_cnnbfdc_unit_info', null, '2017-07-12 06:29:03', '2017-07-12 06:41:21', '6', '1651', '18635', '83552', '455', '7391', '70', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_unit_info_20170712061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170712061100', 'nb_cnnbfdc_presell_info', null, '2017-07-12 06:24:50', '2017-07-12 06:43:49', '6', '2181', '2170', '403046', '1677', '65120', '109', '11', null, '{\"table\":\"ex_dc_nb_cnnbfdc_presell_info_20170712061100\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170712061100', 'nb_cnnbfdc_presell_list', null, '2017-07-12 06:11:00', '2017-07-12 06:24:50', '6', '146', '0', '543076', '3719', '65118', '467', '1', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170712061100', 'nb_cnnbfdc_presell_list_monitor', null, '2017-07-12 06:11:00', '2017-07-12 06:32:00', '6', '5', '0', '191', '38', '44', '22', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170713021000', 'zqfgzx_presell_info', null, '2017-07-13 02:10:00', '2017-07-13 02:11:59', '6', '90', '1787', '93604', '1040', '1314', '700', '0', null, '{\"table\":\"ex_dc_zqfgzx_presell_info_20170713021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170713021000', 'zqfgzx_room_state_info', null, '2017-07-13 03:01:08', '2017-07-13 03:06:01', '6', '1783', '115367', '41214', '207', '1247', '59', '0', null, '{\"table\":\"ex_dc_zqfgzx_room_state_info_20170713021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170713021000', 'zqfgzx_unit_info', null, '2017-07-13 02:11:59', '2017-07-13 03:01:07', '6', '1853', '1804', '2888825', '14063', '86475', '42', '70', null, '{\"table\":\"ex_dc_zqfgzx_unit_info_20170713021000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170713024000', 'zqfgzx_project_info', null, '2017-07-13 02:40:00', '2017-07-13 02:55:19', '6', '96', '1914', '894914', '9322', '17390', '550', '0', null, '{\"table\":\"ex_dc_zqfgzx_project_info_20170713024000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170713061000', 'nb_cnnbfdc_project_info', null, '2017-07-13 06:19:08', '2017-07-13 06:29:19', '6', '1665', '1652', '239827', '2203', '65120', '271', '13', null, '{\"table\":\"ex_dc_nb_cnnbfdc_project_info_20170713061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170713061000', 'nb_cnnbfdc_project_list', null, '2017-07-13 06:10:00', '2017-07-13 06:19:08', '6', '93', '0', '479265', '5153', '35452', '830', '1', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170713061000', 'nb_cnnbfdc_project_list_monitor', null, '2017-07-13 06:10:00', '2017-07-13 06:25:29', '6', '3', '0', '151', '50', '85', '23', '0', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170713061000', 'nb_cnnbfdc_room_info', null, '2017-07-13 12:03:14', '2017-07-13 12:03:15', '6', '0', '0', '0', '0', '0', '999999999', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_info\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170713061000', 'nb_cnnbfdc_room_state_info', null, '2017-07-13 06:41:27', '2017-07-13 12:03:14', '6', '18661', '860923', '18785762', '12083', '168799', '35', '28', null, '{\"table\":\"ex_dc_nb_cnnbfdc_room_state_info_20170713061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170713061000', 'nb_cnnbfdc_unit_info', null, '2017-07-13 06:29:19', '2017-07-13 06:41:27', '6', '1651', '18634', '82047', '447', '5124', '69', '0', null, '{\"table\":\"ex_dc_nb_cnnbfdc_unit_info_20170713061000\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170713061100', 'nb_cnnbfdc_presell_info', null, '2017-07-13 06:25:50', '2017-07-13 06:45:03', '6', '2183', '2170', '483402', '2005', '72767', '104', '13', null, '{\"table\":\"ex_dc_nb_cnnbfdc_presell_info_20170713061100\"}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170713061100', 'nb_cnnbfdc_presell_list', null, '2017-07-13 06:11:00', '2017-07-13 06:25:50', '6', '146', '0', '571637', '3915', '40357', '426', '1', null, '{}', '0');
INSERT INTO `ex_crawler_platform_job_snapshot` VALUES ('20170713061100', 'nb_cnnbfdc_presell_list_monitor', null, '2017-07-13 06:11:00', '2017-07-13 06:35:20', '6', '4', '0', '278', '69', '147', '999999999', '0', null, '{}', '0');
