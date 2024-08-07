# 智能BI项目
> 作者：[StreLitziaYc](https://github.com/StreLitziaYc)

本项目是基于 Spring Boot + MQ + AIGC 的智能数据分析平台。区别于传统 BI，用户只需要导入原始数据集、并输入分析诉求，就能自动生成可视化图表及分析结论，实现数据分析的降本增效。

效果图如下：
![](https://s3.bmp.ovh/imgs/2024/07/30/c39b03065ea92a41.png)

## 项目架构
![项目架构图](https://s3.bmp.ovh/imgs/2024/08/07/42adaca830aea70e.jpg)

## 技术选型
- Java Spring Boot
- MySQL 数据库
- MyBatis-Plus 及 MyBatis X 自动生成
- Redis + Redisson 限流
- RabbitMQ 消息队列
- DashScope AI SDK
- JDK 线程池及异步化
- Easy Excel 表格数据处理
- Swagger + Knife4j 接口文档生成
