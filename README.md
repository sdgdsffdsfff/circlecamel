# 驼圈后台服务
## 概述
提供朋友圈功能，可实名或匿名进行发帖、评论以及点赞；消息提醒等。服务是基于Startalk IM 的社交服务
## 项目结构
````
- cricle-camel-web (后台服务)
 - src/main
     - resources.beat（资源文件） 
         - beta
     - java（代码）
         - com.qunar.qtalk.cricle.camel
     - resources（全局资源文件）
     - webapp（web目录）
         - WEB-INF
         - resultDisplay.jsp 数据展示jsp
 - sql 需要初始化的sql语句
````
## 部署流程
 - 初始化数据库
 ````
 psql -U {用户} -d {数据库} -f sql/init.sql
 ````
 - 拉取源码 
 ````
   git clone git@github.com:qunarcorp/circlecamel.git
 ````
 - 修改配置文件 
 ```` 
 resources.beat下的application.properties文件
    - spring.datasource.* 数据库相关配置,仅支持postgresql数据库
    - jedis.* redis相关配置 哨兵模式
    - schedule.execute_ip 执行定时任务的机器ip，多台机器部署时指定一台进行定时任务
    - schedule.tasks 定时任务列表以及执行时间
    - url_send_notify 发送通知接口，参考wiki 第二条 https://github.com/qunarcorp/ejabberd-open/blob/master/doc/interface.md
    - camel_circle_switch 驼圈开关 true打开 false关闭 
    - qtalk_send_message 发消息接口 /corp/message/send_http_message.qunar 参考wiki 第一条 https://github.com/qunarcorp/ejabberd-open/blob/master/doc/interface.md
    - max_topOrHot_count 置顶帖置热帖的总数上限
    - default_topOrHot_ExistsTime 置顶置热帖的默认时间，单位小时
    - startalk_host 部署startalk时域名，可查询数据库host_info 表格 host字段
  ````
  - 打war包
  ````                         
    mvn clean package -P beta -Dmaven.test.skip=true
  ````
  - 运行tomcat
  ````
    将war包拷贝值tomcat webapp/ROOT 下解压 并运行tomcat
   ````
  - 配置ng转发路径
   ````
    1. 编辑/startalk/openresty/nginx/conf/conf.d/upstreams/qt.qunar.com.upstream.conf 
    upstream startalk_cricle{  
              server {机器ip}:{服务端口};  
    }
    
    2. 编辑/startalk/openresty/nginx/conf/conf.d/subconf/or.server.location.package.qtapi.conf
    location /newapi/cricle_camel/ {
         rewrite_by_lua_file /startalk/openresty/nginx/lua_app/checks/qim/checkchains.lua;
         proxy_pass http://startalk_cricle/newapi/cricle_camel/;
         proxy_set_header   Host             $host;
         proxy_set_header   X-Real-Scheme    $scheme;
         proxy_set_header   X-Real-IP        $remote_addr;
         proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
     }
     location /newapi/cricle_camel/nck/ {
         proxy_pass http://startalk_cricle/newapi/cricle_camel/;
         proxy_set_header   Host             $host;
         proxy_set_header   X-Real-Scheme    $scheme;
         proxy_set_header   X-Real-IP        $remote_addr;
         proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
     }
     
     3.reload ng 配置
     /startalk/openresty/nginx/sbin/nginx -s reload
   ```` 
    
   
    