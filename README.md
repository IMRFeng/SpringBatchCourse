# Spring Batch 视频教程地址：http://edu.csdn.net/course/detail/6478
QQ群：685732078

本示例代码仅供付费学员学习使用，代码可以随意使用在任何商用软件但请勿上传到任何其他网站或共享给其他人员，谢谢配合。

##运行示例前提
在运行代码前请确保你已经安装：
* `JDK/JRE` and `Maven`
* Ensure you're running the versions JDK `8` and Maven `3.x.x`+


##编译及运行
在命令行窗口运行 `mvn clean package` 来build项目，然后运行 `mvn spring-boot:run` 来启动项目。
   * 需要注意在运行前需要修改properties中的`spring.batch.job.names`值，默认执行名为mockDataJob的作业来mock数据
   * 如需启动作业执行数据迁移则把mockDataJob替换为dataPartitioningJob


##使用技术
示例代码中使用了Maven，Spring Boot, Spring Data JPA, Spring Batch, Spring Batch Integration以及其他项目依赖库，具体请大家查看pom.xml

数据库使用的是MariaDB，数据库表的脚本在resources/data/tables.sql中。


##配置参数
你可根据自己的需求进行相关参数的配置，配置文件在 `src/main/resources/` 中的 `application.properties`.

##性能参考
根据测试（开发环境）400万条数据处理时间大约12分钟左右，其中包括在处理的过程中（ItemProcessor）有查询数据库校验的过程


##课程各小节代码
每节课内的内容具体请参考其他branches，例如 通过定时任务启动Job 代码在[start-job-scheduling](IVictorFeng/SpringBatchCourse/blob/start-job-scheduling/README.md) branch

如有问题欢迎随时在群里提出并讨论