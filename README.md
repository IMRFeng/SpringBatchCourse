# Spring Batch 视频教程地址：http://edu.csdn.net/course/detail/6478
QQ群：685732078

本示例代码仅供付费学员学习使用，代码可以随意使用在任何商用软件但请勿上传到任何其他网站或共享给其他人员，谢谢配合。

示例代码中使用了Maven，Spring Boot, Spring Data JPA, Spring Batch, Spring Batch Integration以及其他项目依赖库，具体请大家查看pom.xml

数据库使用的是MariaDB，数据库表的脚本在resources/data/tables.sql中。

根据测试（开发环境）400万条数据处理时间大约12分钟左右，其中包括在处理的过程中（ItemProcessor）有查询数据库校验的过程

每节课内的内容具体请参考其他branches，如有问题欢迎随时在群里提出并讨论