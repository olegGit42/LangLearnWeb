<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html>
<html>
<head>
<link href="<c:url value="/resources/css/user.css" />" rel="stylesheet">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Wordkeeper | <spring:message code="Dictionary"/> | ${username}</title>
</head>
<body>

	<a href="http://localhost:8080/ColibriWeb/"><spring:message code="Home page"/></a>
	|
	<a href="http://localhost:8080/ColibriWeb/auth/user"><spring:message code="AddRepeat"/></a>
	|
	<a href="http://localhost:8080/ColibriWeb/auth/forgettable"><spring:message code="Forgettable"/></a>
	|
	<a href="logout"><spring:message code="Logout"/></a>

<h2><spring:message code="Dictionary"/></h2>
${wordList}

</body>
</html>