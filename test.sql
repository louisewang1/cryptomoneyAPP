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

/*Table structure for table `accountinfodb` */

CREATE TABLE `accountinfodb` (
  `account_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `balance` double NOT NULL DEFAULT '100',
  `email` varchar(100) DEFAULT NULL,
  `cellphone` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`account_id`),
  CONSTRAINT `fk_accountinfo_id` FOREIGN KEY (`account_id`) REFERENCES `logindb` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `accountinfodb` */

insert  into `accountinfodb`(`account_id`,`username`,`balance`,`email`,`cellphone`) values 
(1,'1',140,'',''),
(2,'2',60,'','');

/*Table structure for table `cryptotransferdb` */

CREATE TABLE `cryptotransferdb` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` int(11) NOT NULL,
  `amount` double NOT NULL DEFAULT '0',
  `crypto_time` datetime NOT NULL,
  `address` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cryptotransferdb_id` (`account_id`),
  CONSTRAINT `fk_cryptotransferdb_id` FOREIGN KEY (`account_id`) REFERENCES `logindb` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

/*Data for the table `cryptotransferdb` */

/*Table structure for table `logindb` */

CREATE TABLE `logindb` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `pwd` varchar(100) NOT NULL,
  `pk` varchar(500) NOT NULL,
  `N` varchar(1000) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

/*Data for the table `logindb` */

insert  into `logindb`(`id`,`username`,`pwd`,`pk`,`N`) values 
(1,'1','1','AQAB','AO/Qm9XLCAfCjDpNxVaiB0XLEbdj5g4MNgb/1mS6n0mOWmk1iuQfJXs/AVEPdOepKRmKUUs+ikVSWIRbeOExadk='),
(2,'2','2','AQAB','ANviCVQrr88u4GKoPUWuranG66Het30Y/P9WpghMskHM/IEanrESUQyx5K64PCoDm++1ucgDESpgfH/fTfC+ClU=');

/*Table structure for table `transactiondb` */

CREATE TABLE `transactiondb` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` int(11) NOT NULL,
  `tr_from_account` int(11) NOT NULL,
  `tr_to_account` int(11) NOT NULL,
  `tr_time` datetime NOT NULL,
  `tr_value` double NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_transactiondb_id` (`account_id`),
  CONSTRAINT `fk_transactiondb_id` FOREIGN KEY (`account_id`) REFERENCES `logindb` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `transactiondb` */

/* Procedure structure for procedure `account_register` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `account_register`(IN username_ VARCHAR(50), IN pwd_ VARCHAR(100), IN email_ VARCHAR(100), IN cellphone_ VARCHAR(100), IN pk_ VARCHAR(500), in n_ varchar(1000), OUT result INT)
label:BEGIN
DECLARE account_id_ INT DEFAULT 0;
SELECT id INTO account_id_ FROM logindb WHERE username = username_;
IF account_id_ = 0 THEN 
    INSERT INTO logindb(username,pwd,pk,N) VALUES(username_,pwd_,pk_,n_);
    SELECT LAST_INSERT_ID() INTO account_id_;
    INSERT INTO accountinfodb(account_id,username) SELECT id,username FROM logindb WHERE id = account_id_;
    UPDATE accountinfodb SET email = email_, cellphone = cellphone_ WHERE account_id = account_id_ ;
    set result = 1; # 注册成功
    leave label;
ELSE 
    SET result = 0; # 用户名已存在 
    LEAVE label;
END IF;
END */$$
DELIMITER ;

/* Procedure structure for procedure `addr_to_id` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `addr_to_id`(IN addr_ varchar(50), OUT result int)
label:BEGIN
Declare account_id_ int default 0;
select account_id into account_id_ from cryptotransferdb where address = addr_;
set result = account_id_;
end */$$
DELIMITER ;

/* Procedure structure for procedure `check_balance` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `check_balance`(in account_id_ int, in value_ double, out result int)
label:begin
DECLARE balance_ DOUBLE DEFAULT 0.0;
SELECT balance INTO balance_ FROM accountinfodb WHERE account_id = account_id_;
IF balance_ < value_ THEN 
    SET result = 0; # 余额不足
else 
    set result = 1;
END IF;   
end */$$
DELIMITER ;

/* Procedure structure for procedure `check_modulus` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `check_modulus`(IN account_id_ INT, IN N_ VARCHAR(500), OUT result INT)
label:BEGIN
DECLARE real_N VARCHAR(1000);
SELECT N INTO real_N FROM logindb WHERE id = account_id_;
IF real_N <=> N_ THEN
    SET result = 1;
ELSE 
    SET result = 0;
END IF;
END */$$
DELIMITER ;

/* Procedure structure for procedure `cryptotran_detail` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `cryptotran_detail`(IN account_id_ int)
label:BEGIN
SELECT amount,crypto_time,address FROM cryptotransferdb WHERE account_id = account_id_;
END */$$
DELIMITER ;

/* Procedure structure for procedure `crypto_transfer` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `crypto_transfer`(IN account_id_ int, in value_ double, in addr_ varchar(100), out result int)
label:begin
DECLARE time_ DATETIME;
declare addr_exist int default 1;
SELECT COUNT(*) INTO addr_exist FROM cryptotransferdb WHERE address = addr_;
if addr_exist = 0 then
    SELECT NOW() INTO time_;
    UPDATE accountinfodb SET balance = balance - value_ WHERE account_id = account_id_;
    INSERT INTO cryptotransferdb(account_id,amount,crypto_time,address) VALUES(account_id_,value_,time_,addr_);
    set result = 1;
else 
    set result = 0;
    leave label;
end if;
end */$$
DELIMITER ;

/* Procedure structure for procedure `display_info` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `display_info`(in account_id_ int, out username_ varchar(50),out balance_ double, out email_ varchar(100), out cellphone_ varchar(100))
label:begin
select username,balance,email,cellphone into username_,balance_,email_,cellphone_ from accountinfodb where account_id = account_id_;
end */$$
DELIMITER ;

/* Procedure structure for procedure `exe_crypto` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `exe_crypto`(In to_id_ int, In addr_ VARCHAR(50), OUT amount_ double)
label:begin
select amount into amount_ from cryptotransferdb where address = addr_;
update accountinfodb set balance = balance + amount_ where account_id = to_id_;
delete from cryptotransferdb where address = addr_;
end */$$
DELIMITER ;

/* Procedure structure for procedure `exe_transaction` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `exe_transaction`(in from_account_id int, in to_account_id int, in tr_value double, out result int)
label:begin
declare id_exist int default 0;
DECLARE balance_ DOUBLE DEFAULT 0.0;
DECLARE tr_time_ DATETIME;
select COUNT(*) into id_exist from accountinfodb where account_id = to_account_id;
if id_exist = 0 then 
    set result = -1; # 账户不存在
    Leave label;
end if;
if from_account_id = to_account_id then 
    set result = -2; # 转给自己
    leave label;
end if;
select now() into tr_time_;
select balance into balance_ from accountinfodb where account_id = from_account_id;
if balance_ < tr_value then 
    set result = 0; # 余额不足
    leave label;
END IF;   
update accountinfodb set balance = balance - tr_value where account_id = from_account_id;
UPDATE accountinfodb SET balance = balance + tr_value WHERE account_id = to_account_id;
insert into transactiondb(account_id,tr_from_account,tr_to_account,tr_time,tr_value) values(from_account_id,from_account_id,to_account_id,tr_time_,tr_value);
INSERT INTO transactiondb(account_id,tr_from_account,tr_to_account,tr_time,tr_value) vALUES(to_account_id,from_account_id,to_account_id,tr_time_,tr_value);
set result = 1; # 转账成功
end */$$
DELIMITER ;

/* Procedure structure for procedure `get_pk_N` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `get_pk_N`(IN account_id_ INT, OUT pk_ VARCHAR(500), out N_ varchar(1000))
label:BEGIN
SELECT pk INTO pk_ FROM logindb WHERE id = account_id_;
SELECT N INTO N_ FROM logindb WHERE id = account_id_;
END */$$
DELIMITER ;

/* Procedure structure for procedure `login_check` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `login_check`(IN username_ VARCHAR(50), IN pwd_ varchar(100), OUT account_id_ int)
label:begin
SELECT id into account_id_ from logindb where username = username_ and pwd = pwd_;
if isnull(account_id_) then set account_id_ = 0;
end if;
end */$$
DELIMITER ;

/* Procedure structure for procedure `tran_detail` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `tran_detail`(in account_id_ int)
label:begin
select tr_from_account,tr_to_account,tr_time,tr_value from transactiondb where account_id = account_id_;
end */$$
DELIMITER ;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
