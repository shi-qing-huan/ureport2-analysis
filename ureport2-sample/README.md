# ureport2-sample 示例中心

## 配置说明
* 指定报表模板存储路径：修改configure.properties中的配置项ureport.fileStoreDir，会自动覆盖报表默认路径

## 工程目录结构说明
* --ureport2-sample
* ----src
* ------main
* --------java
* ----------com.bstek.ureport.sapmle
* ------------BuildinReportDS 定义报表内置数据源
* ------------Config springboot的配置类
* ------------DemoApplication springBoot启动类
* ------------TestController restController类
* --------resources 资源文件
* ----------static 静态类库文件
* ----------templates Thymeleaf模板文件
* ----------application.properties springboot默认配置文件
* ----------configure.properties 属性配置文件
* ----------context.xml spring配置文件
* ----ureportfiles 报表示例模板文件
* ----pom.xml mavne依赖配置文件

