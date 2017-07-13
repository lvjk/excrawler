/*
Navicat MySQL Data Transfer

Source Server         : 172.30.103.83(excrawler)
Source Server Version : 50718
Source Host           : 172.30.103.83:3306
Source Database       : excra_meta

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-07-13 15:22:56
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ex_crawler_platform_job_worker_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `ex_crawler_platform_job_worker_snapshot`;
CREATE TABLE `ex_crawler_platform_job_worker_snapshot` (
  `jobSnapshotId` varchar(200) NOT NULL,
  `name` varchar(200) NOT NULL,
  `localNode` varchar(45) NOT NULL,
  `jobName` varchar(200) DEFAULT NULL,
  `startTime` datetime DEFAULT NULL,
  `endTime` datetime DEFAULT NULL,
  `totalProcessCount` int(11) DEFAULT NULL,
  `totalResultCount` int(11) DEFAULT NULL,
  `totalProcessTime` int(11) DEFAULT NULL,
  `maxProcessTime` int(11) DEFAULT NULL,
  `minProcessTime` int(11) DEFAULT NULL,
  `avgProcessTime` int(11) DEFAULT NULL,
  `errCount` int(11) DEFAULT NULL,
  PRIMARY KEY (`jobSnapshotId`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='任务worker 执行快照信息表';
