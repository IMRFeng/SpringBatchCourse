SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for data_source
-- ----------------------------
DROP TABLE IF EXISTS `data_source`;
CREATE TABLE `data_source` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `column_number` int(11) DEFAULT NULL,
  `data_text` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `parent_class` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `sub_class` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=166765 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


-- ----------------------------
-- Table structure for data_destination
-- ----------------------------
DROP TABLE IF EXISTS `data_destination`;
CREATE TABLE `data_destination` (
  `id` int(11) NOT NULL,
  `data_text1` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `data_text2` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `data_text3` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `data_text4` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `data_text5` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `data_text6` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `data_text7` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `data_text8` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `data_text9` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `data_text10` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `parent_class` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `sub_class` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

SET FOREIGN_KEY_CHECKS = 1;


