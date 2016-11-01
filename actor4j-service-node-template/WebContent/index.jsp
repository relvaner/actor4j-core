<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
		<title>Actor4j - Service Node</title>
	</head>
	<style>
		body {
			text-align:center;
		}
		
		.actor4j { 	
			color:#fe9a78; 
		}
	</style>
	<body>
		<h1>Actor4j - Service Node</h1>
		<jsp:useBean id="controller" class="actor4j.service.example.controller.ExampleActorServiceController" scope="session"/>
		<p class="actor4j">Service: Actor4j <%=controller.isOnline()%> online!</p>
		<img src="images/actor4j.png" alt="Actor4j" width="91" height="73" />
		<p>Copyright (c) 2015-2016, David A. Bauer</p>
	</body>
</html>