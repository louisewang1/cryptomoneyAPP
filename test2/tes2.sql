/* Procedure structure for procedure `exe_free_crypto` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `exe_free_crypto`(IN to_id_ INT, IN addr_ VARCHAR(50), OUT amount_ DOUBLE)
label:BEGIN
SELECT amount INTO amount_ FROM cryptotransferdb WHERE address = addr_;
UPDATE accountinfodb SET balance = balance + amount_ WHERE account_id = to_id_;
DELETE FROM cryptotransferdb WHERE address = addr_;
END */$$
DELIMITER ;


/* Procedure structure for procedure `freemoney_addr_to_id` */

DELIMITER $$
/*
SQLyog Ultimate v12.14 (64 bit)
MySQL - 5.1.30-community : Database - test
*********************************************************************
*/
/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`test` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `test`;

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `freemoney_addr_to_id`(IN addr_ VARCHAR(50), OUT result INT)
label:BEGIN
DECLARE account_id_ INT DEFAULT 0;
SELECT account_id INTO account_id_ FROM cryptotransferdb WHERE address = addr_;
SET result = account_id_;
END */$$
DELIMITER ;


/* Procedure structure for procedure `get_free_token_detail` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `get_free_token_detail`(IN addr_ VARCHAR(20), OUT pk_ VARCHAR(100), OUT N_ VARCHAR(100), OUT id_ INT, OUT value_ DOUBLE)
label:BEGIN
SELECT pk INTO pk_ FROM cryptotransferdb WHERE address = addr_;
SELECT N INTO N_ FROM cryptotransferdb WHERE address = addr_;
SELECT account_id INTO id_ FROM cryptotransferdb WHERE address = addr_;
SELECT amount INTO value_ FROM cryptotransferdb WHERE address = addr_;
END */$$
DELIMITER ;
