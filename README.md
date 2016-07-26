java client library for the Apache JServ Protocol 1.3
==============================

This is a java implementation of an ajp13 client, allowing to send requests to a servlet container using this protocol.

Licensed under the Apache License, Version 2.0 (see [LICENSE](https://github.com/jrialland/ajp-client/blob/master/LICENSE))

[![Build Status](https://travis-ci.org/jrialland/ajp-client.svg)](https://travis-ci.org/jrialland/ajp-client)
[![](https://jitpack.io/v/jrialland/ajp-client.svg)](https://jitpack.io/#jrialland/ajp-client)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.jrialland/ajpclient/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.jrialland/ajpclient)
[![Dependencies](https://www.versioneye.com/java/com.github.jrialland:ajpclient/1.9/badge.svg)](https://www.versioneye.com/java/com.github.jrialland:ajpclient/1.9)
[![Coverage Status](https://img.shields.io/coveralls/jrialland/ajp-client.svg)](https://coveralls.io/r/jrialland/ajp-client)
[![Reference Status](https://www.versioneye.com/java/com.github.jrialland:ajpclient/reference_badge.svg?style=flat)](https://www.versioneye.com/java/com.github.jrialland:ajpclient/references)




![Commits](https://www.openhub.net/p/ajp-client/analyses/latest/commits_spark.png)
[![Code statistics](http://www.ohloh.net/p/ajp-client/widgets/project_thin_badge.gif)](https://www.ohloh.net/p/ajp-client)

Simple Usecases :
------------------

* Making a cPing request (checks server availability)

```java
	import com.github.jrialland.apclient.pool.Channels;
	import com.github.jrialland.apclient.CPing;

	...

	//get a tcp connection
	final Channel channel = Channels.connect("localhost", 8009);
	//will try a cping/cpong exchange on the opened tcp connection
	boolean success = new CPing(2, TimeUnit.SECONDS).execute(channel);
  //                                                .execute("localhost", 8009);	
```

* Making a forward request (serves web content)

```java
	import com.github.jrialland.apclient.pool.Channels;
	import com.github.jrialland.apclient.Forward;

	//send a forward request
	new Forward(ajpRequest, ajpResponse).execute("localhost", 8009);
	
```

* Using a client sockets pool :

Socket pools handle the creation and destruction of multiple connections automatically.

```java
	import com.github.jrialland.apclient.pool.Channels;
	import com.github.jrialland.apclient.Forward;
	
	Channels.getPool("localhost", 8009).execute(new Forward(ajpRequest, ajpResponse));
	
```
Will use a socket channel picked from a pool, allowing the reuse of sockets among request.

* The library can be used directly in a servlet container in order to forward requests to another servlet container :

```java
	import javax.servlet.http.HttpServletRequest;
	import javax.servlet.http.HttpServletResponse;
	import com.github.jrialland.apclient.servlet.AjpServletProxy;
	
	HttpServletRequest request = ...
	HttpServetResponse response = ...
	AjpServletProxy.forHost("localhost", 8009).forward(request, response);
```

AJP header size limit
---------------------

  The protocol does not allow request headers to be greater that 8K, which is ok most of the time.
to overcome this limit, at least with tomcat 5.5.21+ and Tomcat 6.0.1+

1) add the ``packetSize`` attribute to the connector's declaration

```xml
    <Connector port="8009" protocol="AJP/1.3"
               packetSize="20000"
               redirectPort="8443" ></Connector>
```

2) Change the limit in Apache Server configuration :
```
ProxyIOBufferSize 19000 
LimitRequestFieldsize 18000
```
