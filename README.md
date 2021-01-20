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

### How to use springboot-jetty-httpchannel-listener

Any requests which take more than configured time for request parsing or response to be written, DiagnosticsManager will
make log entry at info level in the following format

**Requests whose request parsing took more than configured time**

All diagnostics tag times are captured in *EpochMilli*, example=RequestBegin:<EpochMilli>

<span style="color:orange">Currently RequestContent time represents when first chunk of request content is received</span>

```
2021-01-19 14:54:14.517  WARN 89876 --- [tp2015261478-24] c.n.j.s.h.diagnostic.DiagnosticsManager  : requestURI:/log~Transfer-Encoding:chunked~Connection:keep-alive~User-Agent:Apache-HttpClient/4.4 (Java 1.5 minimum; Java/1.8.0_275)~Host:localhost:8080~Accept-Encoding:gzip,deflate~Content-Type:application/json|
RequestBegin:1611096854018~BeforeDispatch:1611096854019~RequestContent:1611096854050~RequestContentEnd:1611096854516~RequestEnd:1611096854516
```

**Requests whose initial response line took more than configured time indicating service issue**

All diagnostics tag times are captured in *EpochMilli*, example=RequestEnd:<EpochMilli>

```
2021-01-19 18:54:23.501  WARN 52627 --- [qtp843959601-24] c.n.j.s.h.diagnostic.DiagnosticsManager  : requestURI:/log~Transfer-Encoding:chunked~Connection:keep-alive~User-Agent:Apache-HttpClient/4.4 (Java 1.5 minimum; Java/1.8.0_275)~Host:localhost:8080~Accept-Encoding:gzip,deflate~Content-Type:application/json|
RequestEnd:1611111262897~ResponseBegin:1611111263501
```

**Requests whose response (Fully written) dispatch took more than configured time**

All diagnostics tag times are captured in *EpochMilli*, example=ResponseBegin:<EpochMilli>

<span style="color:orange">Currently ResponseContent time represents when first chunk of response content is sent</span>

```
2021-01-18 21:55:06.822  WARN 64517 --- [qtp577245010-24] c.n.j.s.h.diagnostic.DiagnosticsManager  : requestURI:/log~Transfer-Encoding:chunked~Connection:keep-alive~User-Agent:Apache-HttpClient/4.4 (Java 1.5 minimum; Java/1.8.0_275)~Host:localhost:8080~Accept-Encoding:gzip,deflate~Content-Type:application/json|
ResponseBegin:1611035706327~ResponseCommit:1611035706329~ResponseContent:1611035706372~ResponseEnd:1611035706821~RequestEnd:1611035706821
```

**Message format when requests get queued, when all Jetty worker threads are used**
```
2021-01-18 21:55:06.822  WARN 64517 --- [qtp577245010-24] c.n.j.s.h.diagnostic.DiagnosticsManager
Request getting queued, requestURI:/log,currentRequestProcessingCount:205,configuredMaxThreads:200,threadPoolQueueSize:-1,
requestHeaders:Transfer-Encoding:chunked~Connection:keep-alive~User-Agent:Apache-HttpClient/4.4 (Java 1.5 minimum; Java/1.8.0_275)~Host:localhost:8080~Accept-Encoding:gzip,deflate~Content-Type:application/json
```

## Configuration

In application.properties or application.yaml configure following properties

`nv.jetty.httpchannel.diagnostics` property defines whether httpchannel diagnostics should be enabled/disabled. 
Default value is `false` to enable value should be `true`

`nv.jetty.request.parsingTimeThresholdInMillis` property defines which requests `DiagnosticsManager` should making entry to log file.
Default is any requests whose request complete request parsing takes more than 5000ms prior dispatching to request handler.
Value can be adjusted based on service needs.

`nv.jetty.response.dispatchTimeThresholdInMillis` property defines which requests `DiagnosticsManager` should making entry to log file.
Default is any requests whose response takes more than 5000ms to be fully written. Value can be adjusted based on service needs.

`nv.jetty.response.responseBeginThresholdInMillis` property defines which requests `DiagnosticsManager` should making entry to log file.
Default is any requests whose responseBegin (initial response line is written to network) takes more than 5000ms. Value can be adjusted based on service needs.

# Acknowledgments
* Thanks to anyone who's code/framework/references/examples are used

# Useful Information
[HttpChannel.Listener](https://www.eclipse.org/jetty/javadoc/jetty-9/org/eclipse/jetty/server/HttpChannel.Listener.html)

[Spring @Configuration vs @Component](http://dimafeng.com/2015/08/29/spring-configuration_vs_component/)

[markdown cheatsheet](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet)
