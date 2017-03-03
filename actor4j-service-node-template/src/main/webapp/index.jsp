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
		<jsp:useBean id="controller" class="actor4j.service.node.template.controller.DefaultActorServiceController" scope="session"/>
		<h1>Actor4j - Service Node (<%=controller.getName()%>)</h1> 
		<table style="margin-left:auto;margin-right:auto;">
			<tr>
				<td>
					<p class="actor4j">Service: Actor4j <%=controller.isOnline()%> online! See also: </p>
				</td>
				<td>	
					<a href="http://docs.actor4j.apiary.io/">REST API</a>
				</td>
			</tr>
		</table>
		<img src="images/actor4j.png" alt="Actor4j" width="91" height="73" />
		<p>Copyright (c) 2015-2016, David A. Bauer</p>
	</body>
</html>