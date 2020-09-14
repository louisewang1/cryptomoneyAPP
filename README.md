## cryptomoney

**已实现的基本功能**
0. 直连MySQL
1. 用户注册
2. 用户登录
3. 信息查询
4. 转账
5. 查询转账记录
6. 二维码字符串提取

**待实现的功能**
0. 全局保持MySQL连接
1. 连接服务器
2. 下拉刷新
3. 信息修改
4. 记住密码
5. 确认退出提示框
6. 打印
7. NFC

**服务器设置**
工具：
1. Eclipse （JAVA EE Developers）:https://www.eclipse.org/downloads/packages/release/oxygen/1a/eclipse-ide-java-ee-developers
2. Apache Tomcat: Version 8.5x https://tomcat.apache.org/download-80.cgi
步骤：
1. 配置好Tomcat服务器：https://www.jianshu.com/p/1057af640c8b
2. 在服务器上运行JAVA 程序，该程序参考教程:https://blog.csdn.net/Tianweidadada/article/details/79516799
3. 运行app


**运行步骤**
1. 导入test.sql到自己MySQL服务端
2. 新建MySQL用户名user@localhost，user@%, 密码都为user，授权user@localhost和user@% 对test数据库的所有权限
3. DBOpenHelper.java中把IP改为自己内网IPv4
4. 编译运行，Android Studio logcat中出现连接成功