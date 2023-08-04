## 1.Ureport报表

### 01.简单报表

#### 1.过滤条件

1,数据源配置

```sql
${

if(param("sex")==null || param("sex")==''){
return "select * from test_employee";


}else{
return "select * from test_employee where SEX=:sex";
}

}
```

![](markdown-img/Ureport报表.assets/Snipaste_2022-07-30_16-33-30.png)

#### 2.排序着色展示

```
我们需要在配置条件添加相对应属性
```

![](markdown-img/Ureport报表.assets/Snipaste_2022-07-30_16-40-39.png)

- 效果展示

![](markdown-img/Ureport报表.assets/Snipaste_2022-07-30_16-41-48.png)

#### 3.交叉报表

```
1.如下图,可以看出D1,D2单元格,展开方向向右
2.D4和E4单元格(左父格为B4,上父格为D2)
3,D5,E5,F5单元格(上父格为D2)
```

![](markdown-img/Ureport报表.assets/Snipaste_2022-07-30_16-53-28.png)

- 效果展示

![](markdown-img/Ureport报表.assets/Snipaste_2022-07-30_16-56-21.png)

### 02.分组报表

#### 1.简单分组报表

```
注意一D3单元格的上父格
```

![1659173080194](markdown-img/Ureport报表.assets/1659173080194.png)

- 效果展示

![1659173257994](markdown-img/Ureport报表.assets/1659173257994.png)

#### 2.自定义分组报表

```
B2和C2聚合方式为统计数量
B2过滤条件为<money>3000
```

![](markdown-img/Ureport报表.assets/1659173767329.png)

- 效果展示

#### 3.交叉分组折叠报表

```
注意一下展开方向和D3单元格上父格
```

![1659173971301](markdown-img/Ureport报表.assets/1659173971301.png)

- 效果展示

![1659174018467](markdown-img/Ureport报表.assets/1659174018467.png)

### 03.多源报表

#### 1.简单多源报表

```
两个数据源,我们需要添加过滤条件,使2个数据源之间有关系
```

![1659174378784](markdown-img/Ureport报表.assets/1659174378784.png)

- 效果展示

![1659174455269](markdown-img/Ureport报表.assets/1659174455269.png)

#### 2.多源多片交叉报表

```
D3,D4,D5,D6,D7,D8上父格为D2
D7,D8的左父格为B7
D1,D2的展开方向D2
总额是汇总,数量是统计数量
```

![1659174788251](markdown-img/Ureport报表.assets/1659174788251.png)

- 效果展示

![1659175001209](markdown-img/Ureport报表.assets/1659175001209.png)

### 04.表达式应用

#### 1.数据比较统计

![1659175504214](markdown-img/Ureport报表.assets/1659175504214.png)

- 效果展示

![1659175575997](markdown-img/Ureport报表.assets/1659175575997.png)

#### 2.中文大写样式的数字

- 效果展示

![1659175684478](markdown-img/Ureport报表.assets/1659175684478.png)

#### 3.日期展示

- date()  输出日期，date函数可以有一个参数，就是日期格式，如果没有则采用yyyy-MM-dd HH:mm:ss格式输出日期。
- day()  输出当前在月份中的天，该函数没有参数。  
- month()  输出当前月份，该函数没有参数。  
- week() 输出当前是星期几，该函数没有参数。 
- year() 输出当前年份，该函数没有参数 

![1659175770841](markdown-img/Ureport报表.assets/1659175770841.png)

### 05.主子报表

#### 1.简单主子报表

![1659177927272](markdown-img/Ureport报表.assets/1659177927272.png)

- 效果展示

![1659177964993](markdown-img/Ureport报表.assets/1659177964993.png)

### 06.单元格坐标

#### 1.环比

```
C2 - C2[A2:-1]  A2单元格和C2单元格为同一行
C2[A2:-1]表示A2单元格上一个的C2值
```

![1659178091716](markdown-img/Ureport报表.assets/1659178091716.png)

#### 2.同比

```
C2 - C2[A2:-1]{B2==$B2}
以年份分组表示,2022年一月份与2019年1月底值的比较
```

![1659178394600](markdown-img/Ureport报表.assets/1659178394600.png)

#### 3.逐层累加

```java
表达式:
if(&A2==1){
	return C2;
}else{
	C2 + D2[A2:-1]
}

```

![1659178575443](markdown-img/Ureport报表.assets/1659178575443.png)

#### 4.条件汇总

```
统计进口额度大于1800共有几个月
count(B2{IMPORTS>1800}) 
```

![1659178807312](markdown-img/Ureport报表.assets/1659178807312.png)

#### 5.占比

```
进口占一年总额度的比例
C2 / sum(C2[A2])
```

![1659179056281](markdown-img/Ureport报表.assets/1659179056281.png)

#### 6.累计平均值

```java
D2单元格表达式
if(&B2==1){
	return C2;
}else{
	C2 + D2[B2:-1]
}

```

![1659179287275](markdown-img/Ureport报表.assets/1659179287275.png)

#### 7.组内排名

```
count(C2[A2]{$C2>C2})+1
```

![1659179419442](markdown-img/Ureport报表.assets/1659179419442.png)

#### 8.求和,求最大,最小值

![1659179523298](markdown-img/Ureport报表.assets/1659179523298.png)

### 07.序号

#### 1.组内序号

![1659179660479](markdown-img/Ureport报表.assets/1659179660479.png)

#### 2.行序号

```
row() 行号
```

![1659179725895](markdown-img/Ureport报表.assets/1659179725895.png)

#### 3.列序号

```
column() 列号
```

![1659179805447](markdown-img/Ureport报表.assets/1659179805447.png)

## 2.ureport对应文件

| 01.简单报表    |        |
| -------------- | ------ |
| 1.过滤条件     | Test01 |
| 2.排序着色展示 | Test02 |
| 3.交叉报表     | Test03 |

| 02.分组报表        |        |
| ------------------ | ------ |
| 1.简单分组报表     | Test18 |
| 2.自定义分组报表   | Test04 |
| 3.交叉分组折叠报表 | Test05 |

| 03.多源报表        |        |
| ------------------ | ------ |
| 1.简单多源报表     | Test06 |
| 2.多源多片交叉报表 | Test17 |

| 04.表达式应用        |        |
| -------------------- | ------ |
| 1.数据比较统计       | Test14 |
| 2.中文大写样式的数字 | Test16 |
| 3.日期展示           | Test15 |

| 05.主子报表    |        |
| -------------- | ------ |
| 1.简单主子报表 | Test12 |

| 06.单元格坐标        |       |
| -------------------- | ----- |
| 1.环比               | Test3 |
| 2.同比               | Test4 |
| 3.逐层累加           | Test5 |
| 4.条件汇总           | Test6 |
| 5.占比               | Test7 |
| 6.累计平均值         | Test8 |
| 7.组内排名           | Test9 |
| 8.求和,求最大,最小值 | Test2 |

| 07.序号    |        |
| ---------- | ------ |
| 1.组内序号 | Test11 |
| 2.行序号   | Test08 |
| 3.列序号   | Test09 |

