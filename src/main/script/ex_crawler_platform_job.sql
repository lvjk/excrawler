/*
Navicat MySQL Data Transfer

Source Server         : 172.30.103.83(excrawler)
Source Server Version : 50718
Source Host           : 172.30.103.83:3306
Source Database       : excra_meta

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-07-13 15:22:08
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ex_crawler_platform_job
-- ----------------------------
DROP TABLE IF EXISTS `ex_crawler_platform_job`;
CREATE TABLE `ex_crawler_platform_job` (
  `name` varchar(100) NOT NULL COMMENT 'job名字',
  `level` int(1) NOT NULL,
  `designatedNodeName` varchar(100) NOT NULL,
  `needNodes` int(11) NOT NULL DEFAULT '1' COMMENT '执行节点数',
  `threads` int(11) NOT NULL COMMENT '执行任务的线程数',
  `isScheduled` int(1) NOT NULL DEFAULT '0' COMMENT '是否调度',
  `cronTrigger` varchar(100) DEFAULT NULL,
  `workFrequency` int(11) NOT NULL DEFAULT '100' COMMENT 'job每次处理时间的阈值 默认100毫秒',
  `workerClass` varchar(100) NOT NULL,
  `workSpaceName` varchar(100) DEFAULT NULL COMMENT '工作空间名称',
  `user` varchar(45) NOT NULL DEFAULT 'admin',
  `describe` varchar(45) DEFAULT NULL COMMENT '任务描述',
  `version` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ex_crawler_platform_job
-- ----------------------------
INSERT INTO `ex_crawler_platform_job` VALUES ('cq315house_house_info', '1', '', '3', '0', '0', null, '3000', 'six.com.crawler.work.plugs.Cq315houseHouseInfoWorker', 'cq315house_house_info', 'admin', '重庆网上房地产房屋信息', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('cq315house_house_state', '1', '', '1', '0', '0', null, '2000', 'six.com.crawler.work.plugs.Cq315houseHouseStateWorker', 'cq315house_house_state', 'admin', '抓取重庆网上房地产 单元信息', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('cq315house_presell_1', '1', '', '1', '0', '0', null, '2000', 'six.com.crawler.work.plugs.Cq315housePresell1Worker', 'cq315house_presell_1', 'admin', '重庆网上房地产 pre_sale_1信息', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('cq315house_presell_2', '1', '', '3', '0', '0', null, '2000', 'six.com.crawler.work.plugs.Cq315housePresell2Worker', 'cq315house_presell_2', 'admin', '重庆网上房地产 pre_sale_2信息', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('cqgtfw_presell_info', '1', '', '3', '0', '0', '0 0 4 * * ? *', '500', 'six.com.crawler.work.plugs.ChongqiCqgtfwGovPresellInfoWorker', 'chongqi_cqgtfw_gov_presell_info', 'admin', '重庆房产管理局预售信息采集', '2');
INSERT INTO `ex_crawler_platform_job` VALUES ('cqgtfw_presell_url', '1', '', '1', '0', '0', '0 0 1 * * ? *', '500', 'six.com.crawler.work.plugs.ChongqiCqgtfwGovPresellUrlWorker', 'chongqi_cqgtfw_gov_presell_url', 'admin', '重庆房产管理局预售信息url采集', '3');
INSERT INTO `ex_crawler_platform_job` VALUES ('czfdc_newhouse_info', '1', '', '1', '0', '0', '0 0 5 * * ? *', '12000', 'six.com.crawler.work.CzfdcNewhouseInfoWorker', 'czfdc_newhouse_info', 'admin', '常州房地产信息网新房信息抓取', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('czfdc_newhouse_url', '1', '', '1', '0', '0', '0 0 4 * * ? *', '12000', 'six.com.crawler.work.CzfdcProjectUrlWorker', 'czfdc_newhouse_info', 'admin', '常州房地产信息网新房url抓取', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('czfdc_presale_info', '1', '', '1', '0', '0', '', '12000', 'six.com.crawler.work.CzfdcPresaleInfoWorker', 'czfdc_presale_info', 'admin', '常州房地产信息网预售信息l抓取', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_gx_project_info', '1', '', '1', '1', '0', '', '100', 'six.com.crawler.work.plugs.DLDCGXProjectInfoWorker', 'dldc_gx_project_info', 'admin', '大连高新区项目信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_gx_project_url', '1', '', '1', '1', '0', '', '100', 'six.com.crawler.work.plugs.DLDCGXProjectListWorker', 'dldc_gx_project_url', 'admin', '大连高新区项目列表信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_gx_room_info', '1', '', '3', '6', '0', '', '100', 'six.com.crawler.work.CommonCrawlWorker', 'dldc_gx_room_info', 'admin', '大连高新区房间信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_gx_room_state_info', '1', '', '3', '3', '0', '', '100', 'six.com.crawler.work.plugs.DLDCGXRoomStateInfoWorker', 'dldc_gx_room_state_info', 'admin', '大连高新区房间状态信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_gx_unit_info', '1', '', '3', '3', '0', '', '100', 'six.com.crawler.work.plugs.DLDCGXUnitInfoWorker', 'dldc_gx_unit_info', 'admin', '大连高新区项目楼栋信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_jz_project_info', '1', '', '1', '1', '0', '', '100', 'six.com.crawler.work.plugs.DLDCJZProjectInfoWorker', 'dldc_jz_project_info', 'admin', '大连金州项目信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_jz_project_url', '1', '', '1', '1', '0', '', '100', 'six.com.crawler.work.plugs.DLDCJZProjectListWorker', 'dldc_jz_project_url', 'admin', '大连金州项目URL采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_jz_room_state_info', '1', '', '1', '1', '0', '', '100', 'six.com.crawler.work.plugs.DLDCJZRoomStateInfoWorker', 'dldc_jz_room_state_info', 'admin', '大连金州房间状态信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_jz_unit_info', '1', '', '1', '1', '0', '', '100', 'six.com.crawler.work.plugs.DLDCJZUnitInfoWorker', 'dldc_jz_unit_info', 'admin', '大连金州楼栋信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_ls_project_info', '1', '', '1', '1', '0', '', '100', 'six.com.crawler.work.plugs.DLDCLSProjectInfoWorker', 'dldc_ls_project_info', 'admin', '大连旅顺区项目信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_ls_project_url', '1', '', '1', '1', '0', '', '100', 'six.com.crawler.work.plugs.DLDCLSProjectListWorker', 'dldc_ls_project_url', 'admin', '大连旅顺区项目列表信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_ls_room_info', '1', '', '1', '1', '0', '', '100', 'six.com.crawler.work.CommonCrawlWorker', 'dldc_ls_room_info', 'admin', '大连旅顺区房间信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_ls_room_state_info', '1', '', '1', '1', '0', '', '100', 'six.com.crawler.work.plugs.DLDCLSRoomStateInfoWorker', 'dldc_ls_room_state_info', 'admin', '大连旅顺区房间状态信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_ls_unit_info', '1', '', '1', '1', '0', '', '100', 'six.com.crawler.work.plugs.DLDCLSUnitInfoWorker', 'dldc_ls_unit_info', 'admin', '大连旅顺区项目楼栋信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_sq_project_info', '1', '', '1', '1', '0', '', '100', 'six.com.crawler.work.plugs.DLDCSQProjectInfoWorker', 'dldc_sq_project_info', 'admin', '大连市内四区项目信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_sq_project_url', '1', '', '1', '1', '0', '', '100', 'six.com.crawler.work.plugs.DLDCSQProjectListWorker', 'dldc_sq_project_url', 'admin', '大连市内四区项目列表URL采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_sq_room_state_info', '1', '', '3', '6', '0', '', '100', 'six.com.crawler.work.plugs.DLDCSQRoomStateInfoWorker', 'dldc_sq_room_state_info', 'admin', '大连市内四区房间状态信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('dldc_sq_unit_info', '1', '', '3', '3', '0', '', '100', 'six.com.crawler.work.plugs.DLDCSQUnitInfoWorker', 'dldc_sq_unit_info', 'admin', '大连市内四区单元信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('nb_cnnbfdc_presell_info', '0', '', '3', '3', '0', '', '3000', 'six.com.crawler.work.plugs.NbCnnbfdcPresellInfoWorker', 'nb_cnnbfdc_presell_info', 'admin', '宁波住宅与房地产网许可证公示信息抓取', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('nb_cnnbfdc_presell_list', '0', '', '1', '1', '1', '0 11 6 * * ? *', '3000', 'six.com.crawler.work.plugs.NbCnnbfdcPresellListWorker', 'nb_cnnbfdc_presell_list', 'admin', '宁波住宅与房地产网许可证公示信息抓取url', '60');
INSERT INTO `ex_crawler_platform_job` VALUES ('nb_cnnbfdc_presell_list_monitor', '0', '', '1', '1', '0', null, '300000', 'six.com.crawler.work.CommonMonitorWorker', 'nb_cnnbfdc_presell_list_monitor', 'admin', '宁波住宅与房地产网许可证公示信息抓取url任务监控', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('nb_cnnbfdc_project_info', '0', '', '3', '5', '0', '', '3000', 'six.com.crawler.work.plugs.NbCnnbfdcProjectInfoWorker', 'nb_cnnbfdc_project_info', 'admin', '宁波住宅与房地产网项目信息抓取', '1');
INSERT INTO `ex_crawler_platform_job` VALUES ('nb_cnnbfdc_project_list', '0', '', '1', '1', '1', '0 10 6 * * ? *', '3000', 'six.com.crawler.work.plugs.NbCnnbfdcProjectListWorker', 'nb_cnnbfdc_project_list', 'admin', '抓去宁波房产数据项目url', '57');
INSERT INTO `ex_crawler_platform_job` VALUES ('nb_cnnbfdc_project_list_monitor', '0', '', '1', '1', '0', null, '300000', 'six.com.crawler.work.CommonMonitorWorker', 'nb_cnnbfdc_project_list_monitor', 'admin', '抓去宁波房产数据项目url任务监控', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('nb_cnnbfdc_room_info', '0', '', '3', '6', '0', '', '3000', 'six.com.crawler.work.plugs.NbCnnbfdcRoomInfoWorker', 'nb_cnnbfdc_room_info', 'admin', '宁波住宅与房地产网楼层单元信息抓取', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('nb_cnnbfdc_room_state_info', '0', '', '3', '6', '0', '0 25 16 * * ? *', '3000', 'six.com.crawler.work.plugs.NbCnnbfdcRoomStateInfoWorker', 'nb_cnnbfdc_room_state_info', 'admin', '宁波住宅与房地产网楼层单元状态信息抓取', '5');
INSERT INTO `ex_crawler_platform_job` VALUES ('nb_cnnbfdc_unit_info', '0', '', '3', '3', '0', '', '3000', 'six.com.crawler.work.plugs.NbCnnbfdcUnitInfoWorker', 'nb_cnnbfdc_unit_info', 'admin', '宁波住宅与房地产网项目信息抓取', '1');
INSERT INTO `ex_crawler_platform_job` VALUES ('qichacha', '1', 'node_80', '3', '0', '0', null, '8000', 'six.com.crawler.work.plugs.QichachaWorker', 'qichacha', 'admin', '采集 企查查 企业信息', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('qichachaSearch', '1', 'node_81', '1', '0', '0', null, '8000', 'six.com.crawler.work.plugs.QichachaSearchWorker', 'qichachaSearch', 'admin', '采集 企查查 企业信息内容页url', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('qichachaSynchronousData', '1', 'node_80', '1', '0', '0', null, '0', 'six.com.crawler.work.plugs.QichachaSynchronousDataWorker', 'qichachaSynchronousData', 'admin', '企查查crm数据同步', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('sh_fangdi_building_info', '1', 'node_80', '1', '0', '0', '0 0 4 * * ? *', '1000', 'six.com.crawler.work.ShFangDiBuildingInfoWorker', 'sh_fangdi_building_info', 'admin', '上海网上房地产项目楼栋信息采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('sh_fangdi_house_info', '1', 'node_80', '1', '0', '0', '0 0 6 * * ? *', '1000', 'six.com.crawler.work.ShFangDiHouseInfoWorker', 'sh_fangdi_house_info', 'admin', '上海网上房地产房屋信息采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('sh_fangdi_presale_info', '1', 'node_80', '1', '0', '0', '0 0 1 * * ? *', '1000', 'six.com.crawler.work.ShFangDiPresaleInfoWorker', 'sh_fangdi_presale_info', 'admin', '上海网上房地产项目预售信息信息采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('sh_fangdi_project_info', '1', 'node_80', '1', '0', '0', 'null', '1000', 'six.com.crawler.work.ShFangDiProjectInfoWorker', 'sh_fangdi_project_info', 'admin', '上海网上房地产项目基本信息采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('sh_fangdi_project_list', '1', 'node_80', '1', '0', '0', 'null', '4000', 'six.com.crawler.work.ShFangDiProjectListWorker', 'sh_fangdi_project_list', 'admin', '上海网上房地产项目列表采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('sh_fangdi_sale_info', '1', 'node_80', '1', '0', '0', '0 0 2 * * ? *', '1000', 'six.com.crawler.work.ShFangDiSaleInfoWorker', 'sh_fangdi_sale_info', 'admin', '上海网上房地产项目销售信息采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('szpl_gov_pre_sale', '1', 'node_80', '1', '0', '0', 'null', '4000', 'six.com.crawler.work.SzplGovPreSaleWorker', 'szpl_gov_pre_sale', 'admin', '深圳市规划和国土资源委员会项目许可证信息集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('szpl_gov_project', '1', 'node_80', '1', '0', '0', 'null', '4000', 'six.com.crawler.work.SzplGovProjectWorker', 'szpl_gov_project', 'admin', '深圳市规划和国土资源委员会项目列表采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('szpl_gov_project_detail', '1', 'node_80', '1', '0', '0', 'null', '2000', 'six.com.crawler.work.SzplGovProjectDetailWorker', 'szpl_gov_project_detail', 'admin', '深圳市规划和国土资源委员会项目详细信息采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('szpl_gov_suite_detail', '1', 'node_80', '1', '0', '0', 'null', '1000', 'six.com.crawler.work.SzplGovSuiteDetailWorker', 'szpl_gov_suite_info_detail', 'admin', '深圳市规划和国土资源委员会房屋信息', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('szpl_gov_suite_state', '1', 'node_80', '1', '0', '0', 'null', '1000', 'six.com.crawler.work.SzplGovSuiteStateWorker', 'szpl_gov_suite_state', 'admin', '深圳市规划和国土资源委员会房屋状态', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tjfdc_building_info', '1', 'node_80', '1', '0', '0', 'null', '8000', 'six.com.crawler.work.TjfdcBuildingInfoWorker', 'tjfdc_building_info', 'admin', '天津市综合房地产信息网 项目楼栋信息采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tjfdc_house_info', '1', 'node_80', '1', '0', '0', 'null', '8000', 'six.com.crawler.work.TjfdcHouseInfoWorker', 'tjfdc_house_info', 'admin', '天津市综合房地产信息网 项目房屋信息采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tjfdc_house_state', '1', 'node_80', '1', '0', '0', 'null', '8000', 'six.com.crawler.work.TjfdcHouseStateWorker', 'tjfdc_house_state', 'admin', '天津市综合房地产信息网 项目房屋状态信息采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tjfdc_presale_info', '1', 'node_80', '1', '0', '0', 'null', '8000', 'six.com.crawler.work.TjfdcPresaleInfoWorker', 'tjfdc_presale_info', 'admin', '天津市综合房地产信息网 项目预售信息采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tjfdc_project_info', '1', 'node_80', '1', '0', '0', 'null', '8000', 'six.com.crawler.work.TjfdcProjectInfoWorker', 'tjfdc_projec_info', 'admin', '天津市综合房地产信息网 项目信息采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tjfdc_project_url', '1', 'node_80', '1', '0', '0', 'null', '8000', 'six.com.crawler.work.TjfdcProjectUrlWorker', 'tjfdc_project_url', 'admin', '天津市综合房地产信息网 项目信息url采集', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tmsf_house_info', '1', '', '3', '5', '0', '0 0 6 * * ? *', '500', 'six.com.crawler.work.plugs.TmsfHouseInfoWorker', 'tmsf_house_info', 'admin', '杭州透明售房网 项目房间信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tmsf_house_status', '1', '', '3', '5', '0', '', '500', 'six.com.crawler.work.plugs.TmsfHouseStatusWorker', 'tmsf_house_status', 'admin', '杭州透明售房网 项目房间信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tmsf_house_status_1', '1', '', '3', '5', '0', '', '500', 'six.com.crawler.work.plugs.TmsfHouseStatus1Worker', 'tmsf_house_status_1', 'admin', '杭州透明售房网 项目房间信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tmsf_house_url', '1', '', '3', '5', '0', '0 0 2 * * ? *', '500', 'six.com.crawler.work.plugs.TmsfHouseUrlWorker', 'tmsf_house_url', 'admin', '杭州透明售房网 项目房间信息Url采集任务', '1');
INSERT INTO `ex_crawler_platform_job` VALUES ('tmsf_presell_info', '1', '', '3', '5', '0', '', '500', 'six.com.crawler.work.plugs.TmsfPresellInfoWorker', 'tmsf_presell_info', 'admin', '杭州透明售房网 项目预售信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tmsf_presell_info_1', '1', '', '3', '5', '0', '', '500', 'six.com.crawler.work.plugs.TmsfPresellInfo1Worker', 'tmsf_presell_info_1', 'admin', '杭州透明售房网 项目预售信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tmsf_presell_url', '1', '', '3', '5', '0', '', '500', 'six.com.crawler.work.plugs.TmsfPresellUrlWorker', 'tmsf_presell_url', 'admin', '杭州透明售房网 项目预售信息url采集任务', '2');
INSERT INTO `ex_crawler_platform_job` VALUES ('tmsf_presell_url_1', '1', '', '3', '5', '0', null, '500', 'six.com.crawler.work.plugs.TmsfPresellUrl1Worker', 'tmsf_presell_url_1', 'admin', '杭州透明售房网 项目预售信息url采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tmsf_project_info', '1', '', '3', '3', '0', '0 0 1 * * ? *', '500', 'six.com.crawler.work.plugs.TmsfProjectInfoWorker', 'tmsf_project_info', 'admin', '杭州透明售房网 项目信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tmsf_project_info_1', '1', '', '3', '3', '0', '', '500', 'six.com.crawler.work.plugs.TmsfProjectInfo1Worker', 'tmsf_project_info_1', 'admin', '杭州透明售房网1 项目信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('tmsf_project_list', '1', '', '1', '1', '0', '0 30 8 * * ? *', '500', 'six.com.crawler.work.plugs.TmsfProjectListWorker', 'tmsf_project_list', 'admin', '杭州透明售房网 项目信息url采集任务', '8');
INSERT INTO `ex_crawler_platform_job` VALUES ('xianfang99_house_info', '1', 'node_80', '1', '0', '0', 'null', '5000', 'six.com.crawler.work.XiAnFang99HouseInfoWorker', 'xianfang99_house_info', 'admin', '西安市房产网房屋信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('xianfang99_presale_info', '1', 'node_80', '1', '0', '0', 'null', '5000', 'six.com.crawler.work.XiAnFang99PresaleWorker', 'xianfang99_presale_info', 'admin', '西安市房产网预售信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('xianfang99_project_info_1', '1', 'node_80', '1', '0', '0', 'null', '5000', 'six.com.crawler.work.XiAnFang99ProjectUrlWorker', 'xianfang99_project_info_1', 'admin', '西安市房产网项目信息url采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('xianfang99_project_info_2', '1', 'node_80', '1', '0', '0', 'null', '5000', 'six.com.crawler.work.XiAnFang99ProjectInfoWorker', 'xianfang99_project_info_2', 'admin', '西安市房产网项目信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('zqfgzx_presell_info', '1', '', '1', '1', '1', '0 10 2 * * ? *', '1000', 'six.com.crawler.work.plugs.ZQFGZXPresellInfoWorker', 'zqfgzx_presell_info', 'admin', '章丘预售信息采集任务', '1');
INSERT INTO `ex_crawler_platform_job` VALUES ('zqfgzx_project_info', '1', '', '1', '1', '1', '0 40 2 * * ? *', '1000', 'six.com.crawler.work.plugs.ZQFGZXProjectInfoWorker', 'zqfgzx_project_info', 'admin', '章丘项目信息采集任务', '1');
INSERT INTO `ex_crawler_platform_job` VALUES ('zqfgzx_room_state_info', '1', '', '3', '3', '0', '', '1000', 'six.com.crawler.work.CommonCrawlWorker', 'zqfgzx_room_state_info', 'admin', '章丘房间状态信息采集任务', '0');
INSERT INTO `ex_crawler_platform_job` VALUES ('zqfgzx_unit_info', '1', '', '3', '3', '0', '', '1000', 'six.com.crawler.work.plugs.ZQFGZXUnitInfoWorker', 'zqfgzx_unit_info', 'admin', '章丘楼栋信息采集任务', '0');
