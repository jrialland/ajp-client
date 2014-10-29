java client library for the Apache JServ Protocol 1.3
==============================

This is a java implementation of an ajp13 client, allowing to send requests to a servlet container using this protocol.

This library relies on netty 4.0 and commons-pool2

Licensed under the Apache License, Version 2.0 (see [LICENSE](https://github.com/jrialland/ajp-client/blob/master/LICENSE))

[![Build Status](https://travis-ci.org/jrialland/ajp-client.svg)](https://travis-ci.org/jrialland/ajp-client)
[![Coverage Status](https://img.shields.io/coveralls/jrialland/ajp-client.svg)](https://coveralls.io/r/jrialland/ajp-client)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.jrialland/ajpclient/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.jrialland/ajpclient)



![Commits](https://www.openhub.net/p/ajp-client/analyses/latest/commits_spark.png)
[![Code statistics](http://www.ohloh.net/p/ajp-client/widgets/project_thin_badge.gif)](https://www.ohloh.net/p/ajp-client)

Simple Usecases :
------------------

* Making a cPing request

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

* Making a forward request

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
	new AjpServletProxy.forHost("localhost", 8009).forward(request, response);
```
