<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<!DOCTYPE html>
<html>
<head>
	<link href="<c:url value="/resources/css/home.css" />" rel="stylesheet">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Wordkeeper</title>
</head>

<body>

	<a href="auth/user"><spring:message code="login"/></a>

	<span style="float: right">
		<spring:message code="app_version"/>
	</span>

	<form:form method="GET" action="download" class="box login">

		<fieldset class="boxBody">

		<div>
			<span style="float: right"> 
				<a href="?lang=en">en</a> 
				<a href="?lang=ru">ru</a>
			</span>
		</div>
		
		<div class="intro" >
		   <spring:message code="introduction"/>
		</div>

		</fieldset>

		<footer> 
			<input type="submit" class="btnLogin" value="<spring:message code="Download"/>">
			<p><spring:message code="downloads_count"/> ${downloads.count}</p>
	    </footer>


	</form:form>


</body>
</html>