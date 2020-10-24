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

/*Table structure for table `logindb` */

CREATE TABLE `logindb` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `pwd` VARCHAR(100) NOT NULL,
  `usertype` VARCHAR(10) NOT NULL,
  `sk` VARCHAR(100) DEFAULT NULL,
  `N` VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

/*Data for the table `logindb` */

INSERT  INTO `logindb`(`id`,`username`,`pwd`,`usertype`,`sk`,`N`) VALUES 

(1,'KFC','kfc','MERCHANT','bR2TEbSv5NKviVl7PhhRZ9F0yVZ6DxD8KQyyuTSLWntYwXxBH2+rUDS5lC0yyEjIK6fejINKdJxqluEljQt+iQ==','AI6YJBi/C8I53lmfgQi6DZRD0FDqbK0qnsQgXNAapjla/oyrzjoQTk02LwibR8YamwyTlCSOQA+RIa05Og6SdTU='),

(2,'1','1','CUSTOMER','','');

/*Table structure for table `accountinfodb` */

CREATE TABLE `accountinfodb` (
  `account_id` INT(11) NOT NULL,
  `username` VARCHAR(50) NOT NULL,
  `balance` DOUBLE NOT NULL DEFAULT '100',
  `email` VARCHAR(100) DEFAULT NULL,
  `cellphone` VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`account_id`),
  CONSTRAINT `fk_accountinfo_id` FOREIGN KEY (`account_id`) REFERENCES `logindb` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

/*Data for the table `accountinfodb` */

INSERT  INTO `accountinfodb`(`account_id`,`username`,`balance`,`email`,`cellphone`) VALUES 

(1,'KFC',100,'',''),
(2,'1',100,'','');

/*Table structure for table `cryptotransferdb` */

CREATE TABLE `cryptotransferdb` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `account_id` INT(11) NOT NULL,
  `amount` DOUBLE NOT NULL DEFAULT '0',
  `crypto_time` DATETIME NOT NULL,
  `address` VARCHAR(100) NOT NULL,
  `N` VARCHAR(100) NOT NULL,
  `pk` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cryptotransferdb_id` (`account_id`),
  CONSTRAINT `fk_cryptotransferdb_id` FOREIGN KEY (`account_id`) REFERENCES `logindb` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

/*Data for the table `cryptotransferdb` */

/*Table structure for table `merchant_logindb` */

CREATE TABLE `merchant_logindb` (
  `account_id` INT(11) NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(20) NOT NULL,
  `pwd` VARCHAR(50) NOT NULL,
  `sk` VARCHAR(100) NOT NULL,
  `N` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

/*Data for the table `merchant_logindb` */

/*Table structure for table `merchant_infodb` */

CREATE TABLE `merchant_infodb` (
  `account_id` INT(11) NOT NULL,
  `username` VARCHAR(50) NOT NULL,
  `balance` DOUBLE NOT NULL DEFAULT '0',
  `email` VARCHAR(50) DEFAULT NULL,
  `cellphone` VARCHAR(20) DEFAULT NULL,
  KEY `fk_merchant_info_id` (`account_id`),
  CONSTRAINT `fk_merchant_info_id` FOREIGN KEY (`account_id`) REFERENCES `merchant_logindb` (`account_id`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

/*Data for the table `merchant_infodb` */

/*Table structure for table `transactiondb` */

CREATE TABLE `transactiondb` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `account_id` INT(11) NOT NULL,
  `tr_from_account` INT(11) NOT NULL,
  `tr_to_account` INT(11) NOT NULL,
  `tr_time` DATETIME NOT NULL,
  `tr_value` DOUBLE NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_transactiondb_id` (`account_id`),
  CONSTRAINT `fk_transactiondb_id` FOREIGN KEY (`account_id`) REFERENCES `logindb` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

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

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS contract;
SET FOREIGN_KEY_CHECKS = 1;
CREATE TABLE IF NOT EXISTS contract (
    contract_addr VARCHAR(20) PRIMARY KEY,
    contract_N VARCHAR(100) NOT NULL,
    contract_pk VARCHAR(100) NOT NULL,
    rcver_id INT NOT NULL,
    contract_value DOUBLE NOT NULL
)

DROP TABLE IF EXISTS contractrecord;
CREATE TABLE IF NOT EXISTS contractrecord (
    contract_addr VARCHAR(20),
    sender_id INT NOT NULL,
    amount DOUBLE NOT NULL,
    CONSTRAINT fk_contractrecord FOREIGN KEY (contract_addr) REFERENCES contract(contract_addr)
)

DROP TABLE IF EXISTS merchant_infodb;
DROP TABLE IF EXISTS merchant_logindb;

DROP PROCEDURE IF EXISTS create_contract;
DELIMITER $
CREATE PROCEDURE create_contract(IN addr_ VARCHAR(20), IN N_ VARCHAR(100), IN pk_ VARCHAR(100), IN id_ INT, IN value_ DOUBLE, OUT result INT)
label:BEGIN
DECLARE addr_exist INT DEFAULT 1;
SELECT COUNT(*) INTO addr_exist FROM contract WHERE contract_addr = addr_;
IF addr_exist = 0 THEN
    INSERT INTO contract(contract_addr,contract_N,contract_pk,rcver_id,contract_value,current_value) VALUES (addr_,N_,pk_,id_,value_,0);
    SET result = 1;
ELSE 
    SET result = 0;
    LEAVE label;
END IF;
END $

DROP PROCEDURE IF EXISTS clear_everything;
DELIMITER $
CREATE PROCEDURE clear_everything()
label:BEGIN
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE accountinfodb;
TRUNCATE cryptotransferdb;
TRUNCATE logindb;
TRUNCATE transactiondb;
TRUNCATE contract;
TRUNCATE contractrecord;
ALTER TABLE logindb AUTO_INCREMENT=1;
ALTER TABLE transactiondb AUTO_INCREMENT=1;
SET FOREIGN_KEY_CHECKS = 1;
END $
DELIMITER ;

DROP PROCEDURE IF EXISTS get_contract_detail;
DELIMITER $
CREATE PROCEDURE get_contract_detail(IN addr_ VARCHAR(20), OUT pk_ VARCHAR(100), OUT N_ VARCHAR(100),OUT id_ INT, OUT value_ DOUBLE, OUT value2_ DOUBLE)
label:BEGIN
SELECT contract_pk INTO pk_ FROM contract WHERE contract_addr = addr_;
SELECT contract_N INTO N_ FROM contract WHERE contract_addr = addr_;
SELECT rcver_id INTO id_ FROM contract WHERE contract_addr = addr_;
SELECT contract_value INTO value_ FROM contract WHERE contract_addr = addr_;
SELECT current_value INTO value2_ FROM contract WHERE contract_addr = addr_;
END $
DELIMITER ;



DROP PROCEDURE IF EXISTS get_token_detail;
DELIMITER $
CREATE PROCEDURE get_token_detail(IN addr_ VARCHAR(20), OUT pk_ VARCHAR(100), OUT N_ VARCHAR(100), OUT id_ INT, OUT value_ DOUBLE)
label:BEGIN
SELECT pk INTO pk_ FROM cryptotransferdb WHERE address = addr_;
SELECT N INTO N_ FROM cryptotransferdb WHERE address = addr_;
SELECT account_id INTO id_ FROM cryptotransferdb WHERE address = addr_;
SELECT amount INTO value_ FROM cryptotransferdb WHERE address = addr_;
DELETE FROM cryptotransferdb WHERE address = addr_;
END $
DELIMITER ;

ALTER TABLE contract ADD COLUMN current_value DOUBLE NOT NULL;


DROP PROCEDURE IF EXISTS add_new_token;
DELIMITER $
CREATE PROCEDURE add_new_token (IN addr_ VARCHAR(20), IN id_ INT, IN value_ DOUBLE, OUT result INT)
label:BEGIN
INSERT INTO contractrecord(contract_addr,sender_id,amount) VALUES(addr_,id_,value_);
SET result = 1;
END $
DELIMITER ;

DROP PROCEDURE IF EXISTS update_current_amount;
DELIMITER $
CREATE PROCEDURE update_current_amount(IN addr_ VARCHAR(20), IN value_ DOUBLE, OUT result INT)
label:BEGIN
UPDATE contract SET current_value = value_ WHERE contract_addr = addr_;
SET result = 1;
END $
DELIMITER ;

CALL clear_everything();

DROP PROCEDURE IF EXISTS finish_contract;
DELIMITER $
CREATE PROCEDURE finish_contract(IN contract_addr_ VARCHAR(20), IN request_amount_ DOUBLE, IN sender_ INT, IN rcver_ INT, IN change_ DOUBLE, OUT result INT)
label:BEGIN
UPDATE accountinfodb SET balance = balance + request_amount_ WHERE account_id = rcver_;
UPDATE accountinfodb SET balance = balance + change_ WHERE account_id = sender_;
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM contract WHERE contract_addr = contract_addr_;
DELETE FROM contractrecord WHERE contract_addr = contract_addr_;
SET result = 1;
END $
DELIMITER ;

-- call clear_everything();

