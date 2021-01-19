# Project Title
springboot-jetty-httpchannel-listener

# Project Desc
springboot-jetty-httpchannel-listener project provides information on following

1. Clients which are slow in sending request and consuming response payload from springboot service
2. Clients whose requests are put in backlog queue when all Jetty threads are in use
3. springboot service which takes more time processing and dispatching response to clients request

# Getting Started
Get copy of **springboot-jetty-httpchannel-listener** module via

`git clone https://github.com/nvattikonda/springboot-jetty-httpchannel-listener.git`

or

`https://github.com/nvattikonda/springboot-jetty-httpchannel-listener/archive/master.zip`
## Prerequisites

### JDK/Maven setup

Following software is required to build and run the project
1. JDK (Version 1.8.x)
2. Maven (Version 3.5.x)

#### Installing JDK and Maven
use `sdkman` for installing JDK and Maven
##### Installing `sdkman`
[Installing sdkman](https://sdkman.io/install)

###### Usage `sdkman`
[Using sdkman](https://sdkman.io/usage)

##### List available Java and Maven versions
1. `sdk list java`
2. `sdk list maven`

##### Available `JDK Distributions` and installing JDK
[JDK distributions supported by sdkman](https://sdkman.io/jdks)

##### Installing `Maven`
[Installing Maven](https://sdkman.io/sdks#maven)

### Building springboot-jetty-httpchannel-listener
**Build the project using command**

nvattikonda@nvattikonda-mbp:`cd ~/projects/personal/springboot-jetty-httpchannel-listener &&  mvn clean install`

## How to use springboot-jetty-httpchannel-listener

Ensure `spring-boot-starter-jetty` is leveraged, refer below how to disable **tomcat** and enable **jetty**

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <scope>provided</scope>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jetty</artifactId>
    <scope>provided</scope>
</dependency>
```

import **springboot-jetty-httpchannel-listener** project

```
 <groupId>com.nv</groupId>
 <artifactId>springboot-jetty-httpchannel-listener</artifactId>
 <version>0.0.1-SNAPSHOT</version>
```
replace `<version>` with appropriate value

Refer to [Configuration](#configuration) section for setting appropriate configuration

## Configuration

In application.properties or application.yaml configure following properties

`nv.jetty.httpchannel.diagnostics` property defines whether httpchannel diagnostics should be enabled/disabled. 
Default value is `false` to enable value should be `true`

`nv.jetty.request.parsingTimeInMillis` property defines which requests `DiagnosticsManager` should making entry to log file.
Default is any requests whose request complete request parsing takes more than 5000ms prior dispatching to request handler.
Value can be adjusted based on service needs.

`nv.jetty.response.DispatchTimeInMillis` property defines which requests `DiagnosticsManager` should making entry to log file.
Default is any requests whose response takes more than 5000ms to be fully written. Value can be adjusted based on service needs.

# Acknowledgments
* Thanks to anyone who's code/framework/references/examples are used

# Useful Information
[Spring @Configuration vs @Component](http://dimafeng.com/2015/08/29/spring-configuration_vs_component/)

[markdown cheatsheet](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet)
