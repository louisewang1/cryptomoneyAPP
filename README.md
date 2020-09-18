## cryptomoney

**已实现的基本功能**
1. 直连MySQL
2. 用户注册
3. 用户登录
4. 信息查询
5. 转账
6. 查询转账记录
7. 二维码字符串提取
8. 字符串生成二维码
9. 字符串生成二维码并连接打印机打印（快速接入指南辣鸡！！）  **蓝牙连接测试必须用真机，否则无法获取设备**
 
**待实现的功能**
1. ~~全局保持MySQL连接~~
2. ~~连接服务器~~
3. 下拉刷新
4. 信息修改
5. ~~记住密码~~
6. ~~确认退出提示框~~
7. 打印
8. NFC

**服务器设置**
工具：
1. Eclipse （JAVA EE Developers）:https://www.eclipse.org/downloads/packages/release/oxygen/1a/eclipse-ide-java-ee-developers
2. Apache Tomcat: Version 8.5x https://tomcat.apache.org/download-80.cgi
步骤：
1. 配置好Tomcat服务器：https://www.jianshu.com/p/1057af640c8b
2. 在服务器上运行JAVA 程序，该程序参考教程:https://blog.csdn.net/Tianweidadada/article/details/79516799
3. 运行app


**数据库设置**
1. 导入test.sql到自己MySQL服务端
2. 新建MySQL用户名user@localhost，user@%, 密码都为user，授权user@localhost和user@% 对test数据库的所有权限
3. DBOpenHelper.java中把IP改为自己内网IPv4
4. 编译运行，Android Studio logcat中出现连接成功
