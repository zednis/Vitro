-- MySQL dump 10.9
--
-- Host: localhost    Database: vivo3
-- ------------------------------------------------------
-- Server version	4.1.16-standard

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `dataprops`
--

DROP TABLE IF EXISTS `dataprops`;
CREATE TABLE `dataprops` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) default NULL,
  `publicName` varchar(255) default NULL,
  `domainClassId` int(11) default NULL,
  `rangeDatatypeId` int(11) default NULL,
  `modTime` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `example` varchar(255) default NULL,
  `description` mediumtext,
  `minCardinality` int(11) default NULL,
  `maxCardinality` int(11) default NULL,
  `displayTier` int(11) default '100',
  `displayLimit` int(11) default '5',
  `flag1Set` set('0','1','2','3','4','5','6','7','8','9','10','11') default '0,1,2,3,4,5,6,7,8,9,10,11',
  `statusId` int(11) default NULL,
  `hidden` varchar(8) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `dataprops`
--


/*!40000 ALTER TABLE `dataprops` DISABLE KEYS */;
LOCK TABLES `dataprops` WRITE;
INSERT INTO `dataprops` VALUES (1,'abstract','abstract',295,NULL,'2006-10-17 12:02:09',NULL,NULL,NULL,NULL,100,5,'0,1,2,3,4,5,6,7,8,9,10,11',0,'FALSE'),(2,'issue','issue',295,NULL,'2006-10-17 12:02:27',NULL,NULL,NULL,NULL,100,5,'0,1,2,3,4,5,6,7,8,9,10,11',0,'FALSE'),(3,'response','response',295,NULL,'2006-10-17 12:03:46',NULL,NULL,NULL,NULL,100,5,'0,1,2,3,4,5,6,7,8,9,10,11',0,'FALSE'),(4,'impact','impact',295,NULL,'2006-10-17 15:41:57',NULL,NULL,NULL,NULL,100,5,'0,1,2,3,4,5,6,7,8,9,10,11',0,'FALSE'),(5,'funding sources','funding sources',295,NULL,'2006-10-17 12:03:36',NULL,NULL,NULL,NULL,100,5,'0,1,2,3,4,5,6,7,8,9,10,11',0,'FALSE');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dataprops` ENABLE KEYS */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

