*** 
# 什么是findConflicts?

FindConflicts 是一款maven插件。它可以找出你maven工程中各种冲突（jar冲突，类冲突，日志冲突，版本冲突）。    

我敢打赌，

你一定遇到过日志冲突。 我们都知道，java的日志包各种各样，象log4j,logback,slf4j,common-logs,log4j-over-slf4j,
slf4j-over-log4j12等等。 这些包非常容易混在一起。 最终的后果是，突然有一天你的应用系统就打印不出来日志了，更糟糕的是，你的应用可能因为日志系统初始化不成功而导致应用无法正常启动。
另外，你也一定遇到过NoSuchMethodError，很明显你的系统存在类冲突。

如何在maven打包前就能发现这些冲突呢？

FindConflicts就是来干这事的！ FindConflicts在你打包你的应用前，就能扫描你的系统中有哪些冲突。让你在系统上线前，辅助你解决各种冲突，减少因为各种冲突（jar冲突，类冲突，日志冲突，版本冲突）带来的风险，
而且它使用起来非常简单，一个maven命令即可。
  
 

# 如何使用 ?

非常简单，象你平时配置maven插件一样，配置这款插件FindConflicts。
 

- 先决条件:

1. JDK(1.5+) 
2. Maven(2.x+)
3. Git 


- 使用步骤:
1. git clone  https://github.com/hzdavid/findConflicts.git 
2. cd findConflicts   
   mvn install   
   或者 mvn install deploy (如果你想发布到自己的远程仓库)
3. 把FindConflicts配置在你的maven工程的pom.xml中   
例如:
 
	 	<build>
	   	<plugins> 
	 		<plugin>
	            <groupId>findconflicts</groupId>
	            <artifactId>fc-maven-plugin</artifactId>
	            <version>1.0.0-SNAPSHOT</version>
	            <executions>
	                    <execution>
	                            <phase>package</phase>
	                            <goals><goal>go</goal></goals>
	                    </execution>
	            </executions>
	            <configuration>
	                    <versionCheckConfig>${basedir}/versionCheck.pb</versionCheckConfig>
	            </configuration>
	          </plugin>
	    </plugins>
	 	</build> 
 
4. mvn package -Dmaven.test.skip=true

  或者 mvn fc:go 
  
 
5. 于是，你的maven工程中的冲突，将会以表格形式呈现出来，

冲突的显示是以下面的字符串开头的:

	[WARNING]*********************************************Jar Conflicts****************************************************
	[WARNING]*********************************************Log Conflicts****************************************************
	[WARNING]*******************************************Version Conflicts****************************************************
	[WARNING]*******************************************Class Conflicts****************************************************

关于这些冲突结果的解读，请继续看下文。 




# 冲突定义 

在开始解读冲突结果前，我们需要对冲突的定义达成一致。

有如下4种冲突   

+ 类冲突

两个类有相同的包名，类名，但是类的大小不一样。  
类冲突率(Class Conflicts Ratio):  冲突类个数/总类数。.  
例如,在你的类路径上有1000个类，但是有5个类是存在冲突的，于是类冲突率(Class Conflicts Ratio)=5/1000(0.5%)

默认情况下，类冲突是不显示的，除非你加上maven参数 -Dshow.class.conflicts=true 

+ Jar包冲突   
有类冲突的2个jar。比如相同的类（相同包名，相同类名，但类大小不一样），在1.jar有，2.jar也有，则1.jar与2.jar就有冲突。    
Jar包冲突率(Jar Conflicts Ratio): 有冲突的jar包数 /总jar包数  
例如：在你的应用的classpath上有100个jar包，有5个jar包有冲突，则Jar包冲突率(Jar Conflicts Ratio)=5/100(5%)  

+ 日志冲突    

   有些日志包不能共存，不然它们可能会导致你的应用日志打印不出来，或者日志系统无法初始化。
   主要有3种类型：    
    1. 栈溢出（StackOverflow）    
    log4j-over-slf4j and slf4j-log4j12 不能共存。   
    jcl-over-slf4j and slf4j-jcl 不能共存.   
    因为它们会导致栈溢出异常。  
    2. Apache公共日志包冲突(JCL Conflicts)     
    jcl-over-slf4j, commons-logging 不能共存。   
    因为它们可能导致方法找不到的jvm错误，日志打印不出来。    
    3. 多个日志类静态绑定（Multiple StaticLoggerBinder)   
    在你类路径上有多个org/slf4j/impl/StaticLoggerBinder.class.      
    它可能会导致日志打印不出来。 
   
如果你想了解关于日志冲突的更多细节，你可以参考这个网站 https://www.slf4j.org/codes.html#version_mismatch.  

+ 版本冲突    

某个jar的版本过低，或者某个jar的版本与另外一个版本的jar不兼容。 

当你使用版本冲突前，你需要配置版本冲突规则到文件versionCheck.pb中。versionCheck.pb这个文件，你可以放置和pom.xml相同目录下。
 
版本冲突规则比如这样的（一行一个规则）   
if xxxgroupId:xxxArtifactId >=2.2.2 then  yyygroupId:yyyArtifactId>2.1.8   
zzzgroupId:zzzArtifactId>5.1.6  
上面的意思是，在你的应用类路径中，
如果 xxxgroupId:xxxArtifactId的版本大于等于2.2.2了，那么yyygroupId:yyyArtifactId的版本必须大于2.1.8，否则发生了冲突。

zzzgroupId:zzzArtifactId的版本必须大于5.1.6，否则发生了冲突。


versionCheck.pb样本文件见 :https://github.com/hzdavid/findConflicts/tree/master/src/main/resources/versionCheck.pb




# 冲突结果表格解释

+ Jar包冲突(Jar Conflicts)                         
	|number| classConflictRatio |  groupId  |artifactId|version|originFrom|

    number: 冲突号。相同冲突号的，为一组冲突的jar包。     
    classConflictRatio: 类冲突率。见上文的解释。   
    groupId: jar的maven坐标 group.   
    artifactId: jar的maven坐标 artifactId   
    version: jar的maven坐标version.  
    originFrom: 是因为你配置了哪个依赖，导致这个jar被引入了。如果你要想排除这个jar包，你可以在pom.xml的dependency标签中增加exclusion。
+ 日志冲突(Log Conflicts)    
	|number| logConflictType |  groupId  |artifactId|version|originFrom|

    number: 冲突号。相同冲突号的，为一组冲突的jar包。       
    logConflictType: 日志冲突类型。即栈溢出(StackOverflow),  Apache公共日志包冲突(JCL Conflicts) ,多个日志类静态绑定(Multiple StaticLoggerBinder)
     groupId: jar的maven坐标 group.   
    artifactId: jar的maven坐标 artifactId   
    version:the jar的maven坐标version.  
    originFrom: 是因为你配置了哪个依赖，导致这个jar被引入了。如果你要想排除这个jar包，你可以在pom.xml的dependency标签中增加exclusion。


+ 版本冲突(Version Conflicts)    
	|number| logConflictType |  groupId  |artifactId|version|originFrom|

    number: 冲突号。相同冲突号的，为一组冲突的jar包。         
    groupId: jar的maven坐标 group.   
    artifactId: jar的maven坐标 artifactId   
    version:the jar的maven坐标version.  
    requiredVersion: 正确的jar版本应该是  
    conflictReason: 冲突原因 
    originFrom: 是因为你配置了哪个依赖，导致这个jar被引入了。如果你要想排除这个jar包，你可以在pom.xml的dependency标签中增加exclusion。
+ Class Conflicts   
	|number| classConflictRatio |  groupId  |artifactId|version|originFrom|

    number: 冲突号。相同冲突号的，为一组冲突的jar包。     
    classConflictRatio: 类冲突率。见上文的解释。   
    groupId: jar的maven坐标 group.   
    artifactId: jar的maven坐标 artifactId   
    version: jar的maven坐标version.  
    originFrom: 是因为你配置了哪个依赖，导致这个jar被引入了。如果你要想排除这个jar包，你可以在pom.xml的dependency标签中增加exclusion。
    
    
# 帮助

mvn fc:help



***
In Engish:


# What is findConflicts?

FindConflicts is a maven plugin, which is used to find conflicts such as jar/class/log conflicts among your maven projects.
I bet you must have met log conflicts。
As you know, java has many log libraries such  as log4j,logback,slf4j,common-logs,log4j-over-slf4j,
slf4j-over-log4j12...and so on. They are confusing,right ?
The consequent is log information of your application is not written someday. Or even stop your application to boot because the log system initializes exceptionally.

But now, FindConflicts can help you to find the conflicts before you package your application. 

And it is very easy to use,Just a maven command!
 

# How to use ?

Very easy, just config a maven plugin as you used to do.

- Prerequisites:

1. JDK(1.5+) 
2. Maven(2.x+)
3. Git 


- Instructions:
1. git clone  https://github.com/hzdavid/findConflicts.git 
2. cd findConflicts   
   mvn install   
   or mvn install deploy (if you have your own maven remote repository)
3. add FindConflicts maven plugin to your pom.xml   
For example:
 
	 	<build>
	   	<plugins> 
	 		<plugin>
	            <groupId>findconflicts</groupId>
	            <artifactId>fc-maven-plugin</artifactId>
	            <version>1.0.0-SNAPSHOT</version>
	            <executions>
	                    <execution>
	                            <phase>package</phase>
	                            <goals><goal>go</goal></goals>
	                    </execution>
	            </executions>
	            <configuration>
	                    <versionCheckConfig>${basedir}/versionCheck.pb</versionCheckConfig>
	            </configuration>
	          </plugin>
	    </plugins>
	 	</build> 
 
4. mvn package -Dmaven.test.skip=true

  Or mvn fc:go 
  
 
5. then, the conflicts   among your projects will be shown in form of table.

They start with:

	[WARNING]*********************************************Jar Conflicts****************************************************
	[WARNING]*********************************************Log Conflicts****************************************************
	[WARNING]*******************************************Version Conflicts****************************************************
	[WARNING]*******************************************Class Conflicts****************************************************

About the explanation of the result,please read next.




# Conflicts Definition 

Before we explain the conflicts result, we need to have same recognition as for conflicts.

They are four types of conflicts.

+ Class Conflicts   
Two classes have same package,same className. but different size.  
Class Conflicts Ratio:  Number of Class Conflicts / Number of all classes.  
For example,there are 1000 classes at your application classpath,but 5 classes conflict with others.So  Class Conflicts Ratio=5/1000(0.5%)
+ Jar Conflicts   
Two jars have Class Conflicts.    
For example, a same class(same package,same className,but different class size), exists in 1.jar and 2.jar,then 1.jar conflicts with 2.jar.    
Jar Conflicts Ratio: Number of jar Conflicts / Number of all jar.    
For example,there are 100 jars at your application classpath, but 5 jars conflict with others.So  Jar Conflicts Ratio=5/100(5%)  

+ Log Conflicts   

   Some log libraries can't exist together. Otherwise it may cause log information will not be written or log system of your application initialized exceptionally.
   There are three types. 
    1. StackOverflow    
    log4j-over-slf4j and slf4j-log4j12 can't exist together   
    jcl-over-slf4j and slf4j-jcl can't exist together.   
    It may cause StackOverflow. 
    2. JCL Conflicts     
    jcl-over-slf4j, commons-logging can't exist together.   
    It may cause NoSuthMethodError or log information  will not be written.
    3. Multiple StaticLoggerBinder    
    There are 2 or more org/slf4j/impl/StaticLoggerBinder.class.      
    It may cause log information  will not be written.
   
	 
	
If you want to know more about log conflicts, you can refer https://www.slf4j.org/codes.html#version_mismatch.  

+ Version Conflicts   

The version of one jar is too low, or some a version of a jar can't work with another one of some a version.

Before you use Version Conflicts, you should config your version conflict rules to file versionCheck.pb ,which you can locate at the directory of pom.xml.

The rule of versionCheck.pb is like this (one rule one line):     
if xxxgroupId:xxxArtifactId >=2.2.2 then  yyygroupId:yyyArtifactId>2.1.8   
zzzgroupId:zzzArtifactId>5.1.6  

It means that at your application's classpath,      
if the version of xxxgroupId:xxxArtifactId is larger than 2.2.2 or equal to 2.2.2, the  yyygroupId:yyyArtifactId should be larger than 2.1.8. Otherwise it is a conflict.
The version of zzzgroupId:zzzArtifactId should be larger than 5.1.6. Otherwise it is a conflict.

 
The sample is :https://github.com/hzdavid/findConflicts/tree/master/src/main/resources/versionCheck.pb






# Conflicts Result Table  Explanation

+ Jar Conflicts                       
	|number| classConflictRatio |  groupId  |artifactId|version|originFrom|

    number: the number of a group of conflicts jar. One same number, One conflicts group.      
    classConflictRatio: the class conflict ratio. Refer to definition above.    
    groupId: the maven position attribute group.   
    artifactId: the maven position attribute artifactId.   
    version:the maven position attribute version.  
    originFrom: which dependency makes this jar to be imported.    It is the hint for you to add a exclusion at your pom.xml at element dependency if you want to exclude this jar.
+ Log Conflicts    
	|number| logConflictType |  groupId  |artifactId|version|originFrom|

    number: the number of a group of conflicts jar. One same number, One conflicts group.      
    logConflictType: the type of log conflicts:StackOverflow,  JCL Conflicts ,Multiple StaticLoggerBinder
    groupId: the maven position attribute group.   
    artifactId: the maven position attribute artifactId.   
    version:the maven position attribute version.  
    originFrom: which dependency makes this jar to be imported. It is the hint for you to add a exclusion at your pom.xml at element dependency if you want to exclude this jar.


+ Version Conflicts   
	|number| logConflictType |  groupId  |artifactId|version|originFrom|

    number: the number of a group of conflicts jar. One same number, One conflicts group.       
    groupId: the maven position attribute group.   
    artifactId: the maven position attribute artifactId.   
    version:the maven position attribute version.  
    requiredVersion: the right required version  
    conflictReason: the reason. 
    originFrom: which dependency makes this jar to be imported.  It is the hint for you to add a exclusion at your pom.xml at element dependency if you want to exclude this jar.
+ Class Conflicts    
	|number| classConflictRatio |  groupId  |artifactId|version|originFrom|

    number: the number of a group of conflicts jar. One same number, One conflicts group.      
    classConflictRatio: the class conflict ratio. Refer to definition above.    
    groupId: the maven position attribute group.   
    artifactId: the maven position attribute artifactId   
    version:the maven position attribute version.  
    originFrom: which dependency makes this jar to be imported.It is the hint for you to add a exclusion at your pom.xml at element dependency if you want to exclude this jar.
 
# Help

 mvn fc:help   
 
    
