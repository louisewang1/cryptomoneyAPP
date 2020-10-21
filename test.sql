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

(1,'KFC',100,'',''),
(2,'1',100,'','');

/*Table structure for table `cryptotransferdb` */

CREATE TABLE `cryptotransferdb` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` int(11) NOT NULL,
  `amount` double NOT NULL DEFAULT '0',
  `crypto_time` datetime NOT NULL,
  `address` varchar(100) NOT NULL,
  `N` varchar(100) NOT NULL,
  `pk` varchar(10) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cryptotransferdb_id` (`account_id`),
  CONSTRAINT `fk_cryptotransferdb_id` FOREIGN KEY (`account_id`) REFERENCES `logindb` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `cryptotransferdb` */

/*Table structure for table `logindb` */

CREATE TABLE `logindb` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `pwd` varchar(100) NOT NULL,
  `usertype` varchar(10) NOT NULL,
  `sk` varchar(100) DEFAULT NULL,
  `N` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

/*Data for the table `logindb` */

insert  into `logindb`(`id`,`username`,`pwd`,`usertype`,`sk`,`N`) values 

(1,'KFC','kfc','MERCHANT','bR2TEbSv5NKviVl7PhhRZ9F0yVZ6DxD8KQyyuTSLWntYwXxBH2+rUDS5lC0yyEjIK6fejINKdJxqluEljQt+iQ==','AI6YJBi/C8I53lmfgQi6DZRD0FDqbK0qnsQgXNAapjla/oyrzjoQTk02LwibR8YamwyTlCSOQA+RIa05Og6SdTU='),

(2,'1','1','CUSTOMER','','');

/*Table structure for table `merchant_infodb` */

CREATE TABLE `merchant_infodb` (
  `account_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `balance` double NOT NULL DEFAULT '0',
  `email` varchar(50) DEFAULT NULL,
  `cellphone` varchar(20) DEFAULT NULL,
  KEY `fk_merchant_info_id` (`account_id`),
  CONSTRAINT `fk_merchant_info_id` FOREIGN KEY (`account_id`) REFERENCES `merchant_logindb` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `merchant_infodb` */

/*Table structure for table `merchant_logindb` */

CREATE TABLE `merchant_logindb` (
  `account_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(20) NOT NULL,
  `pwd` varchar(50) NOT NULL,
  `sk` varchar(100) NOT NULL,
  `N` varchar(100) NOT NULL,
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `merchant_logindb` */

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

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `account_register`(IN username_ VARCHAR(50), IN pwd_ VARCHAR(100), IN usertype_ varchar(10), in sk_ varchar(100), in N_ varchar(100), IN email_ VARCHAR(100), IN cellphone_ VARCHAR(100), OUT result INT)
label:BEGIN
DECLARE account_id_ INT DEFAULT 0;
SELECT id INTO account_id_ FROM logindb WHERE username = username_;
IF account_id_ = 0 THEN 
    INSERT INTO logindb(username,pwd,usertype,sk,N) VALUES(username_,pwd_,usertype_,sk_,N_);
    SELECT LAST_INSERT_ID() INTO account_id_;
    INSERT INTO accountinfodb(account_id,username) SELECT id,username FROM logindb WHERE id = account_id_;
    UPDATE accountinfodb SET email = email_, cellphone = cellphone_ WHERE account_id = account_id_ ;
    SET result = 1; # 注册成功
    LEAVE label;
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

/* Procedure structure for procedure `clear_everything` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `clear_everything`()
label:begin
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE accountinfodb;
TRUNCATE cryptotransferdb;
TRUNCATE logindb;
TRUNCATE merchant_infodb;
TRUNCATE merchant_logindb;
TRUNCATE transactiondb;
ALTER TABLE logindb AUTO_INCREMENT=1;
ALTER TABLE merchant_logindb AUTO_INCREMENT=1;
ALTER TABLE transactiondb AUTO_INCREMENT=1;
SET FOREIGN_KEY_CHECKS = 1;
end */$$
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

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `crypto_transfer`(IN account_id_ INT, IN value_ DOUBLE, IN N_ varchar(100),in pk_ varchar(10), IN addr_ VARCHAR(100), OUT result INT)
label:BEGIN
DECLARE time_ DATETIME;
DECLARE addr_exist INT DEFAULT 1;
SELECT COUNT(*) INTO addr_exist FROM cryptotransferdb WHERE address = addr_;
IF addr_exist = 0 THEN
    SELECT NOW() INTO time_;
    UPDATE accountinfodb SET balance = balance - value_ WHERE account_id = account_id_;
    INSERT INTO cryptotransferdb(account_id,amount,crypto_time,address,N,pk) VALUES(account_id_,value_,time_,addr_,N_,pk_);
    SET result = 1;
ELSE 
    SET result = 0;
    LEAVE label;
END IF;
END */$$
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

/* Procedure structure for procedure `get_merchant_sk_N` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `get_merchant_sk_N`(IN username_ VARCHAR(20), OUT sk_ VARCHAR(100), OUT N_ VARCHAR(100))
label:BEGIN
SELECT sk INTO sk_ FROM logindb WHERE username = username_;
SELECT N INTO N_ FROM logindb WHERE username = username_;
END */$$
DELIMITER ;

/* Procedure structure for procedure `get_pk_N` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `get_pk_N`(IN addr_ varchar(100), OUT pk_ VARCHAR(10), OUT N_ VARCHAR(1000))
label:BEGIN
SELECT pk INTO pk_ FROM cryptotransferdb WHERE address = addr_;
SELECT N INTO N_ FROM cryptotransferdb WHERE address = addr_;
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

/* Procedure structure for procedure `merchant_list` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `merchant_list`()
label:BEGIN
SELECT username FROM logindb where usertype like "MERCHANT";
END */$$
DELIMITER ;

/* Procedure structure for procedure `merchant_login_check` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `merchant_login_check`(IN username_ VARCHAR(50), IN pwd_ VARCHAR(50), OUT account_id_ INT)
label:BEGIN
SELECT account_id INTO account_id_ FROM merchant_logindb WHERE username = username_ AND pwd = pwd_;
IF ISNULL(account_id_) THEN SET account_id_ = 0;
END IF;
END */$$
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
