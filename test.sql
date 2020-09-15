-- create table if not exists accountdb (
--     id int primary key auto_increment,
--     username varchar(20),
--     pwd varchar(50),
--     balance double
-- );
-- 
-- insert into accountdb(username,pwd,balance) values("Alice","00000",100);
-- INSERT INTO accountdb(username,pwd,balance) VALUES("Bob","11111",200);
-- INSERT INTO accountdb(username,pwd,balance) VALUES("Charlie","22222",300);
-- 
-- UPDATE accountdb SET balance = 100 WHERE username = "Alice" AND pwd = "00000";
-- update accountdb set balance = 50 where username = "Alice" and pwd = "00000";
-- 
-- select *from accountdb where username="Alice" and pwd="00000";
-- 
-- ALTER TABLE accountdb ADD COLUMN email varchar(100);
-- alter table accountdb add column cellphone VARCHAR(20);
-- 
-- UPDATE accountdb SET email = "00000@000.com", cellphone = "00000000" WHERE username = "Alice" AND pwd = "00000";
-- UPDATE accountdb SET email = "11111@111.com", cellphone = "11111111" WHERE username = "Bob" AND pwd = "11111";
-- UPDATE accountdb SET email = "22222@222.com", cellphone = "22222222" WHERE username = "Charlie" AND pwd = "22222";
-- drop table if exists accountdb;

CREATE TABLE IF NOT EXISTS logindb (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    pwd VARCHAR(100) NOT NULL
);

TRUNCATE logindb;
INSERT INTO logindb(username,pwd) VALUES("Alice","00000");
INSERT INTO logindb(username,pwd) VALUES("Bob","11111");
INSERT INTO logindb(username,pwd) VALUES("Charlie","22222");

CREATE TABLE IF NOT EXISTS accountinfodb (
    account_id INT PRIMARY KEY NOT NULL,
    username VARCHAR(50) NOT NULL,
    balance DOUBLE NOT NULL DEFAULT 0.0,
    email VARCHAR(100),
    cellphone VARCHAR(100),
    CONSTRAINT fk_accountinfo_id FOREIGN KEY (account_id) REFERENCES logindb(id)
);

INSERT INTO accountinfodb(account_id,username,balance,email,cellphone) VALUES(1,"Alice",100,"00000@000.com","00000000");
INSERT INTO accountinfodb(account_id,username,balance,email,cellphone) VALUES(2,"Bob",200,"11111@111.com","11111111");
INSERT INTO accountinfodb(account_id,username,balance,email,cellphone) VALUES(3,"Charlie",300,"22222@222.com","22222222");

CREATE TABLE IF NOT EXISTS transactiondb (
    id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    tr_from_account INT NOT NULL,
    tr_to_account INT NOT NULL,
    tr_time DATETIME NOT NULL,
    tr_value DOUBLE NOT NULL DEFAULT 0.0,
    CONSTRAINT fk_transactiondb_id FOREIGN KEY (account_id) REFERENCES logindb(id)
);


DROP PROCEDURE IF EXISTS exe_transaction;
SELECT * FROM information_schema.routines WHERE routine_name= 'exe_transaction';
DELIMITER ;

# direct transaction between users
DELIMITER $
CREATE PROCEDURE exe_transaction(IN from_account_id INT, IN to_account_id INT, IN tr_value DOUBLE, OUT result INT)
label:BEGIN
DECLARE id_exist INT DEFAULT 0;
DECLARE balance_ DOUBLE DEFAULT 0.0;
DECLARE tr_time_ DATETIME;
SELECT COUNT(*) INTO id_exist FROM accountinfodb WHERE account_id = to_account_id;
IF id_exist = 0 THEN 
    SET result = -1; # 账户不存在
    LEAVE label;
END IF;
IF from_account_id = to_account_id THEN 
    SET result = -2; # 转给自己
    LEAVE label;
END IF;
SELECT NOW() INTO tr_time_;
SELECT balance INTO balance_ FROM accountinfodb WHERE account_id = from_account_id;
IF balance_ < tr_value THEN 
    SET result = 0; # 余额不足
    LEAVE label;
END IF;   
UPDATE accountinfodb SET balance = balance - tr_value WHERE account_id = from_account_id;
UPDATE accountinfodb SET balance = balance + tr_value WHERE account_id = to_account_id;
INSERT INTO transactiondb(account_id,tr_from_account,tr_to_account,tr_time,tr_value) VALUES(from_account_id,from_account_id,to_account_id,tr_time_,tr_value);
INSERT INTO transactiondb(account_id,tr_from_account,tr_to_account,tr_time,tr_value) VALUES(to_account_id,from_account_id,to_account_id,tr_time_,tr_value);
SET result = 1; # 转账成功
END $
DELIMITER ;

# test ext_transaction
-- Call exe_transaction(2,1,10.0,@tr_result);
-- select @tr_result;

# create procedure print_transaction 连接

-- DROP PROCEDURE IF EXISTS login_check;
-- SELECT * FROM information_schema.routines WHERE routine_name= 'login_check';

# check username and pwd
DELIMITER $
CREATE PROCEDURE login_check(IN username_ VARCHAR(50), IN pwd_ VARCHAR(100), OUT account_id_ INT)
label:BEGIN
SELECT id INTO account_id_ FROM logindb WHERE username = username_ AND pwd = pwd_;
IF ISNULL(account_id_) THEN SET account_id_ = 0;
END IF;
END $
DELIMITER ;

# test login_check
-- call login_check("Alice","00000",@account_id_);
-- select @account_id_;
-- 
-- DROP PROCEDURE IF EXISTS display_info;
-- SELECT * FROM information_schema.routines WHERE routine_name= 'display_info';
-- Delimiter ;

# reurn account info
DELIMITER $
CREATE PROCEDURE display_info(IN account_id_ INT, OUT username_ VARCHAR(50),OUT balance_ DOUBLE, OUT email_ VARCHAR(100), OUT cellphone_ VARCHAR(100))
label:BEGIN
SELECT username,balance,email,cellphone INTO username_,balance_,email_,cellphone_ FROM accountinfodb WHERE account_id = account_id_;
END $
DELIMITER ;

# test display_info
-- call display_info(1,@username_,@balance_,@email_,@cellphone_);
-- select @username_,@balance_,@email_,@cellphone_;

-- DROP PROCEDURE IF EXISTS tran_detail;
-- SELECT * FROM information_schema.routines WHERE routine_name= 'tran_detail';
-- Delimiter ;

DELIMITER $
CREATE PROCEDURE tran_detail(IN account_id_ INT) 
label:BEGIN
SELECT tr_from_account,tr_to_account,tr_time,tr_value FROM transactiondb WHERE account_id = account_id_;
END $

# test tran_detail
-- call tran_detail(1);

# initialize db
TRUNCATE TABLE transactiondb;
UPDATE accountinfodb SET balance = 100 WHERE account_id = 1;
UPDATE accountinfodb SET balance = 200 WHERE account_id = 2;
UPDATE accountinfodb SET balance = 300 WHERE account_id = 3;


DROP PROCEDURE IF EXISTS account_register;
SELECT * FROM information_schema.routines WHERE routine_name= 'account_register';
DELIMITER ;

DELIMITER $
CREATE PROCEDURE account_register(IN username_ VARCHAR(50), IN pwd_ VARCHAR(100), IN email_ VARCHAR(100), IN cellphone_ VARCHAR(100),OUT result_reg INT)
label:BEGIN
DECLARE account_id_ INT DEFAULT 0;
SELECT id INTO account_id_ FROM logindb WHERE username = username_;
IF account_id_ = 0 THEN 
    INSERT INTO logindb(username,pwd) VALUES(username_,pwd_);
    SELECT LAST_INSERT_ID() INTO account_id_;
    INSERT INTO accountinfodb(account_id,username) SELECT id,username FROM logindb WHERE id = account_id_;
    UPDATE accountinfodb SET email = email_, cellphone = cellphone_ WHERE account_id = account_id_ ;
    SET result_reg = 1; # 注册成功
ELSE 
    SET result_reg = 0; # 用户名已存在 
    LEAVE label;
END IF;
END $
DELIMITER ;

# test account_register
CALL account_register("Alice","33333","33333@333.com","33333333",@result_reg);
SELECT @result_reg;

SET FOREIGN_KEY_CHECKS = 0;
DELETE t1, t2 FROM logindb t1 INNER JOIN accountinfodb t2 ON t1.id = t2.account_id WHERE t1.id > 3;
SET FOREIGN_KEY_CHECKS = 1;
ALTER TABLE logindb AUTO_INCREMENT  = 4;  # 删除后主键从删除地方开始增加
ALTER TABLE accountinfodb AUTO_INCREMENT  = 4;
