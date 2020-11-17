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
  `pk` VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

/*Data for the table `logindb` */

INSERT  INTO `logindb`(`id`,`username`,`pwd`,`usertype`,`sk`,`N`,`pk`) VALUES (1,'User','user','CUSTOMER','','','');

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

INSERT  INTO `accountinfodb`(`account_id`,`username`,`balance`,`email`,`cellphone`) VALUES (1,'User',43,'','');

/*Table structure for table `contract` */

CREATE TABLE `contract` (
  `contract_addr` VARCHAR(20) NOT NULL,
  `contract_N` VARCHAR(100) NOT NULL,
  `contract_pk` VARCHAR(100) NOT NULL,
  `rcver_id` INT(11) NOT NULL,
  `contract_value` DOUBLE NOT NULL,
  `current_value` DOUBLE NOT NULL,
  PRIMARY KEY (`contract_addr`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

/*Data for the table `contract` */

/*Table structure for table `contractrecord` */

CREATE TABLE `contractrecord` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `contract_addr` VARCHAR(20) DEFAULT NULL,
  `sender_id` INT(11) NOT NULL,
  `amount` DOUBLE NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_contractrecord` (`contract_addr`),
  CONSTRAINT `fk_contractrecord` FOREIGN KEY (`contract_addr`) REFERENCES `contract` (`contract_addr`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

/*Data for the table `contractrecord` */

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
) ENGINE=INNODB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1;

/*Data for the table `cryptotransferdb` */

INSERT  INTO `cryptotransferdb`(`id`,`account_id`,`amount`,`crypto_time`,`address`,`N`,`pk`) VALUES (15,1,15,'2020-11-17 15:24:01','CdvAzxlZ6zkBfQOzja8k','ANwCPjDJDyK5G8Nrn8BVi6pp2bDBh9uMH6tx2SZeMTCABtVFLCtthcLYx+vkKXJ2hW4ewSjc7QqrZu6+lTW0VpU=','AQAB');


/*Table structure for table `merchant_token` */

CREATE TABLE `merchant_token` (
  `cus_id` INT(11) NOT NULL,
  `amount` DOUBLE NOT NULL DEFAULT '0',
  `crypto_time` DATETIME NOT NULL,
  `address` VARCHAR(20) NOT NULL,
  `N` VARCHAR(100) NOT NULL,
  `pk` VARCHAR(10) NOT NULL,
  `mer_id` INT(11) NOT NULL,
  `mer_name` VARCHAR(20) NOT NULL,
  `ciphertext` VARCHAR(300) NOT NULL,
  KEY `fk_merchant_token_cus_id` (`cus_id`),
  KEY `fk_merchant_token_mer_id` (`mer_id`),
  CONSTRAINT `fk_merchant_token_cus_id` FOREIGN KEY (`cus_id`) REFERENCES `logindb` (`id`),
  CONSTRAINT `fk_merchant_token_mer_id` FOREIGN KEY (`mer_id`) REFERENCES `logindb` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

/*Data for the table `merchant_token` */

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

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `account_register`(IN username_ VARCHAR(50), IN pwd_ VARCHAR(100), IN usertype_ VARCHAR(10), IN sk_ VARCHAR(100), in pk_ varchar(100), IN N_ VARCHAR(100), IN email_ VARCHAR(100), IN cellphone_ VARCHAR(100), OUT result INT)
label:BEGIN
DECLARE account_id_ INT DEFAULT 0;
SELECT id INTO account_id_ FROM logindb WHERE username = username_;
IF account_id_ = 0 THEN 
    INSERT INTO logindb(username,pwd,usertype,sk,N,pk) VALUES(username_,pwd_,usertype_,sk_,N_,pk_);
    SELECT LAST_INSERT_ID() INTO account_id_;
    INSERT INTO accountinfodb(account_id,username) SELECT id,username FROM logindb WHERE id = account_id_;
    UPDATE accountinfodb SET email = email_, cellphone = cellphone_ WHERE account_id = account_id_ ;
    SET result = 1; # 娉ㄥ唽鎴愬姛
    LEAVE label;
ELSE 
    SET result = 0; # 鐢ㄦ埛鍚嶅凡瀛樺湪 
    LEAVE label;
END IF;
END */$$
DELIMITER ;

/* Procedure structure for procedure `addr_to_id` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `addr_to_id`(IN addr_ VARCHAR(50), OUT result INT)
label:BEGIN
DECLARE account_id_ INT DEFAULT 0;
SELECT cus_id INTO account_id_ FROM merchant_token WHERE address = addr_;
SET result = account_id_;
END */$$
DELIMITER ;

/* Procedure structure for procedure `add_new_token` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `add_new_token`(IN addr_ VARCHAR(20), IN id_ INT, IN value_ DOUBLE, OUT result INT)
label:BEGIN
DECLARE addr_exist INT DEFAULT 1;
SELECT COUNT(*) INTO addr_exist FROM contract WHERE contract_addr = addr_;
IF addr_exist = 0 THEN
    SET result = 0;
    LEAVE label;
ELSE
    INSERT INTO contractrecord(contract_addr,sender_id,amount) VALUES(addr_,id_,value_);
    SET result = 1;
end if;
END */$$
DELIMITER ;

/* Procedure structure for procedure `check_balance` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `check_balance`(in account_id_ int, in value_ double, out result int)
label:begin
DECLARE balance_ DOUBLE DEFAULT 0.0;
SELECT balance INTO balance_ FROM accountinfodb WHERE account_id = account_id_;
IF balance_ < value_ THEN 
    SET result = 0; # 浣欓涓嶈冻
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
label:BEGIN
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE accountinfodb;
TRUNCATE cryptotransferdb;
TRUNCATE logindb;
TRUNCATE transactiondb;
TRUNCATE contract;
TRUNCATE contractrecord;
truncate merchant_token;
ALTER TABLE logindb AUTO_INCREMENT=1;
ALTER TABLE transactiondb AUTO_INCREMENT=1;
ALTER TABLE contractrecord AUTO_INCREMENT = 1;
ALTER TABLE cryptotransferdb AUTO_INCREMENT = 1;
SET FOREIGN_KEY_CHECKS = 1;
END */$$
DELIMITER ;

/* Procedure structure for procedure `create_contract` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `create_contract`(in addr_ varchar(20), in N_ varchar(100), in pk_ varchar(100), in id_ int, in value_ double, out result int)
label:begin
DECLARE addr_exist INT DEFAULT 1;
SELECT COUNT(*) INTO addr_exist FROM contract WHERE contract_addr = addr_;
IF addr_exist = 0 THEN
    INSERT INTO contract(contract_addr,contract_N,contract_pk,rcver_id,contract_value,current_value) VALUES (addr_,N_,pk_,id_,value_,0);
    SET result = 1;
ELSE 
    SET result = 0;
    LEAVE label;
END IF;
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

/* Procedure structure for procedure `crypto_transfer_merchant` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `crypto_transfer_merchant`(IN account_id_ INT, IN value_ DOUBLE, IN N_ VARCHAR(100),IN pk_ VARCHAR(10), IN addr_ VARCHAR(100), in merchant_ varchar(20), in ciphertext_ varchar(300), OUT result INT)
label:BEGIN
DECLARE time_ DATETIME;
DECLARE addr_exist1 INT DEFAULT 1;
DECLARE addr_exist2 INT DEFAULT 1;
declare mer_id_ int;
SELECT COUNT(*) INTO addr_exist1 FROM cryptotransferdb WHERE address = addr_;
SELECT COUNT(*) INTO addr_exist2 FROM merchant_token WHERE address = addr_;
IF addr_exist1 = 0 and addr_exist2 = 0 THEN
    SELECT NOW() INTO time_;
    select account_id into mer_id_ from accountinfodb where username = merchant_;
    UPDATE accountinfodb SET balance = balance - value_ WHERE account_id = account_id_;
    INSERT INTO merchant_token(cus_id,amount,crypto_time,address,N,pk,mer_id,mer_name,ciphertext) VALUES(account_id_,value_,time_,addr_,N_,pk_,mer_id_,merchant_,ciphertext_);
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

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `exe_crypto`(IN to_id_ INT, IN addr_ VARCHAR(50), OUT amount_ DOUBLE)
label:BEGIN
declare mer_id_ int;
SELECT amount INTO amount_ FROM merchant_token WHERE address = addr_;
select mer_id into mer_id_ from merchant_token where address = addr_;
if mer_id_ = to_id_ then
    UPDATE accountinfodb SET balance = balance + amount_ WHERE account_id = to_id_;
    DELETE FROM merchant_token WHERE address = addr_;
else 
    set amount_ = -1;
end if;
END */$$
DELIMITER ;

/* Procedure structure for procedure `exe_free_crypto` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `exe_free_crypto`(IN to_id_ INT, IN addr_ VARCHAR(50), OUT amount_ DOUBLE)
label:BEGIN
SELECT amount INTO amount_ FROM cryptotransferdb WHERE address = addr_;
UPDATE accountinfodb SET balance = balance + amount_ WHERE account_id = to_id_;
DELETE FROM cryptotransferdb WHERE address = addr_;
END */$$
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
    set result = -1; # 璐︽埛涓嶅瓨鍦?
    Leave label;
end if;
if from_account_id = to_account_id then 
    set result = -2; # 杞粰鑷繁
    leave label;
end if;
select now() into tr_time_;
select balance into balance_ from accountinfodb where account_id = from_account_id;
if balance_ < tr_value then 
    set result = 0; # 浣欓涓嶈冻
    leave label;
END IF;   
update accountinfodb set balance = balance - tr_value where account_id = from_account_id;
UPDATE accountinfodb SET balance = balance + tr_value WHERE account_id = to_account_id;
insert into transactiondb(account_id,tr_from_account,tr_to_account,tr_time,tr_value) values(from_account_id,from_account_id,to_account_id,tr_time_,tr_value);
INSERT INTO transactiondb(account_id,tr_from_account,tr_to_account,tr_time,tr_value) vALUES(to_account_id,from_account_id,to_account_id,tr_time_,tr_value);
set result = 1; # 杞处鎴愬姛
end */$$
DELIMITER ;

/* Procedure structure for procedure `find_last_sender` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `find_last_sender`(in addr_ varchar(20), out id_ int)
label:begin
select sender_id into id_ from contractrecord where contract_addr = addr_ order by id desc limit 1;
end */$$
DELIMITER ;

/* Procedure structure for procedure `finish_contract` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `finish_contract`(IN contract_addr_ VARCHAR(20), IN request_amount_ DOUBLE, IN sender_ INT, IN rcver_ INT, IN change_ DOUBLE, OUT result INT)
label:BEGIN
DECLARE addr_exist INT DEFAULT 1;
SELECT COUNT(*) INTO addr_exist FROM contract WHERE contract_addr = contract_addr_;
IF addr_exist = 0 THEN
    SET result = 0;
    LEAVE label;
ELSE
    UPDATE accountinfodb SET balance = balance + request_amount_ WHERE account_id = rcver_;
    UPDATE accountinfodb SET balance = balance + change_ WHERE account_id = sender_;
    SET FOREIGN_KEY_CHECKS = 0;
    DELETE FROM contract WHERE contract_addr = contract_addr_;
    DELETE FROM contractrecord WHERE contract_addr = contract_addr_;
    SET result = 1;
end if;
END */$$
DELIMITER ;

/* Procedure structure for procedure `freemoney_addr_to_id` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `freemoney_addr_to_id`(IN addr_ VARCHAR(50), OUT result INT)
label:BEGIN
DECLARE account_id_ INT DEFAULT 0;
SELECT account_id INTO account_id_ FROM cryptotransferdb WHERE address = addr_;
SET result = account_id_;
END */$$
DELIMITER ;

/* Procedure structure for procedure `get_contract_detail` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `get_contract_detail`(IN addr_ VARCHAR(20), OUT pk_ VARCHAR(100), OUT N_ VARCHAR(100),out id_ int, out value_ double, out value2_ double)
label:BEGIN
SELECT contract_pk INTO pk_ FROM contract WHERE contract_addr = addr_;
SELECT contract_N INTO N_ FROM contract WHERE contract_addr = addr_;
SELECT rcver_id INTO id_ FROM contract WHERE contract_addr = addr_;
SELECT contract_value INTO value_ FROM contract WHERE contract_addr = addr_;
SELECT current_value INTO value2_ FROM contract WHERE contract_addr = addr_;
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

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `get_pk_N`(IN addr_ VARCHAR(50), OUT pk_ VARCHAR(10), OUT N_ VARCHAR(100))
label:BEGIN
SELECT pk INTO pk_ FROM merchant_token WHERE address = addr_;
SELECT N INTO N_ FROM merchant_token WHERE address = addr_;
END */$$
DELIMITER ;

/* Procedure structure for procedure `get_token_detail` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `get_token_detail`(IN addr_ VARCHAR(20), OUT pk_ VARCHAR(100), OUT N_ VARCHAR(100), out id_ int, out value_ double)
label:BEGIN
SELECT pk INTO pk_ FROM cryptotransferdb WHERE address = addr_;
SELECT N INTO N_ FROM cryptotransferdb WHERE address = addr_;
select account_id into id_ FROM cryptotransferdb WHERE address = addr_;
SELECT amount INTO value_ FROM cryptotransferdb WHERE address = addr_;
delete from cryptotransferdb where address = addr_;
END */$$
DELIMITER ;

/* Procedure structure for procedure `login_check` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `login_check`(IN username_ VARCHAR(50), IN pwd_ VARCHAR(100), OUT account_id_ INT, OUT type_ varchar(10), out N_ varchar(100), out pk_ varchar(100))
label:BEGIN
SELECT id,usertype,N,pk INTO account_id_,type_,N_,pk_ FROM logindb WHERE username = username_ AND pwd = pwd_;
IF ISNULL(account_id_) THEN SET account_id_ = 0;
END IF;
END */$$
DELIMITER ;

/* Procedure structure for procedure `merchanttran_detail` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `merchanttran_detail`(IN account_id_ INT)
label:BEGIN
SELECT amount,crypto_time,mer_name,ciphertext FROM merchant_token WHERE cus_id = account_id_;
END */$$
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

/* Procedure structure for procedure `return_money` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `return_money`(in contract_addr_ varchar(20), out result int)
label:begin
DECLARE addr_exist INT DEFAULT 1;
DECLARE s INT DEFAULT 0;
DECLARE sender_ INT DEFAULT 0;
DECLARE amount_ DOUBLE DEFAULT 0;
DECLARE cur CURSOR FOR SELECT sender_id,amount FROM contractrecord WHERE contract_addr = contract_addr_;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET s=1;
SELECT COUNT(*) INTO addr_exist FROM contract WHERE contract_addr = contract_addr_;
IF addr_exist = 0 THEN
    set result = 0;
    leave label;
else
    open cur;
        fetch cur into sender_,amount_;
        while s<>1 do
            update accountinfodb set balance = balance + amount_ where account_id = sender_;
            fetch cur into sender_,amount_;
        end while;
    close cur;
    SET FOREIGN_KEY_CHECKS = 0;
    DELETE FROM contract WHERE contract_addr = contract_addr_;
    DELETE FROM contractrecord WHERE contract_addr = contract_addr_;
    SET FOREIGN_KEY_CHECKS = 1;
    SET result = 1;
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

/* Procedure structure for procedure `update_current_amount` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `update_current_amount`(IN addr_ VARCHAR(20), IN value_ DOUBLE, OUT result INT)
label:BEGIN
DECLARE addr_exist INT DEFAULT 1;
SELECT COUNT(*) INTO addr_exist FROM contract WHERE contract_addr = addr_;
IF addr_exist = 0 THEN
    SET result = 0;
    LEAVE label;
ELSE
    UPDATE contract SET current_value = value_ WHERE contract_addr = addr_;
    SET result = 1;
end if;
END */$$
DELIMITER ;

/* Procedure structure for procedure `update_request_amount` */

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `update_request_amount`(IN addr_ VARCHAR(20), IN value_ DOUBLE, OUT result INT)
label:BEGIN
DECLARE addr_exist INT DEFAULT 1;
SELECT COUNT(*) INTO addr_exist FROM contract WHERE contract_addr = addr_;
IF addr_exist = 0 THEN
    SET result = 0;
    LEAVE label;
ELSE
    UPDATE contract SET contract_value = value_ WHERE contract_addr = addr_;
    SET result = 1;
END IF;
END */$$
DELIMITER ;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
