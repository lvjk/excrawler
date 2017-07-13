/*
Navicat MySQL Data Transfer

Source Server         : 172.30.103.83(excrawler)
Source Server Version : 50718
Source Host           : 172.30.103.83:3306
Source Database       : excra_meta

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-07-13 15:22:47
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ex_crawler_platform_job_worker_err
-- ----------------------------
DROP TABLE IF EXISTS `ex_crawler_platform_job_worker_err`;
CREATE TABLE `ex_crawler_platform_job_worker_err` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `jobSnapshotId` varchar(200) NOT NULL,
  `jobName` varchar(200) NOT NULL,
  `workerName` varchar(200) NOT NULL,
  `startTime` datetime NOT NULL,
  `type` varchar(100) NOT NULL,
  `msg` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8126468 DEFAULT CHARSET=utf8 COMMENT='任务worker 执行快照信息表 异常信息表';
