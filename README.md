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

The version of one jar is too low, or the version of a jar can't work with another one of some a version.

Before you use Version Conflicts, you should config your version conflicts rule to file versionCheck.pb ,which you can locate at the directory of pom.xml.

The rule of versionCheck.pb is like this (one rule one line):     
if xxxgroupId:xxxArtifactId >=2.2.2 then  yyygroupId:yyyArtifactId>2.1.8   
zzzgroupId:zzzArtifactId>5.1.6  


The sample is :https://github.com/hzdavid/findConflicts/tree/master/src/main/resources/versionCheck.pb






# Conflicts Result Table  Explanation

+ Jar Conflicts                       
	|number| classConflictRatio |  groupId  |artifactId|version|originFrom|

    number: the number of a group of conflicts jar. One same number, One conflicts group.      
    classConflictRatio: the class conflict ratio. refer to definition above.    
    groupId: the maven position attribute group.   
    artifactId: the maven position attribute artifactId   
    version:the maven position attribute version.  
    originFrom: which dependency makes this jar to be imported.    
+ Log Conflicts
	|number| logConflictType |  groupId  |artifactId|version|originFrom|

    number: the number of a group of conflicts jar. One same number, One conflicts group.      
    logConflictType: the type of log conflicts, StackOverflow,  JCL Conflicts ,Multiple StaticLoggerBinder
    groupId: the maven position attribute group.   
    artifactId: the maven position attribute artifactId   
    version:the maven position attribute version.  
    originFrom: which dependency makes this jar to be imported.  


+ Version Conflicts
	|number| logConflictType |  groupId  |artifactId|version|originFrom|

    number: the number of a group of conflicts jar. One same number, One conflicts group.       
    groupId: the maven position attribute group.   
    artifactId: the maven position attribute artifactId   
    version:the maven position attribute version.  
    requiredVersion: the right required version  
    conflictReason: the reason. 
    originFrom: which dependency makes this jar to be imported.  
+ Class Conflicts
	|number| classConflictRatio |  groupId  |artifactId|version|originFrom|

    number: the number of a group of conflicts jar. One same number, One conflicts group.      
    classConflictRatio: the class conflict ratio. refer to definition above.    
    groupId: the maven position attribute group.   
    artifactId: the maven position attribute artifactId   
    version:the maven position attribute version.  
    originFrom: which dependency makes this jar to be imported.
 

