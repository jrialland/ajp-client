java client library for the Apache JServ Protocol 1.3
==============================

This is a java implementation of an ajp13 client, allowing to send requests to a servlet container using this protocol.

This library relies on netty 4.0 and commons-pool2

Licensed under the Apache License, Version 2.0 (see [LICENSE](https://github.com/jrialland/ajp-client/blob/master/LICENSE))

Simple Usecases :
------------------

* Making a cPing request

```java
	//get a tcp connection
	final Channel channel = Channels.connect("localhost", 8009);<br>
	//will try a cping/cpong exchange on the opened tcp connection<br>
	boolean success = new CPing(2, TimeUnit.SECONDS).doWithChannel(channel);<br>
```

* Making a forward request

```java
	//get a tcp connection
	final Channel channel = Channels.connect("localhost", 8009);
	//send a forward request
	new Forward(ajpRequest, ajpResponse).doWithChannel(channel);
```

* Using a client sockets pool :

	Channels.getPool("localhost", 8009).execute(new Forward(ajpRequest, ajpResponse));

Will use a socket channel picked from a pool, allowing the reuse of sockets among request.

* The library can be used directly in a servlet container in order to forward requests to another servlet container :

```java
	HttpServletRequest request = ...
	HttpServetResponse response = ...
	new AjpServletProxy.forHost("localhost", 8009).forward(request, response);
```