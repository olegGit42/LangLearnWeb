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

	<a href="${appurl}"><spring:message code="Home page"/></a>
	|
	<a href="${appurl}auth/user"><spring:message code="AddRepeat"/></a>
	|
	<a href="${appurl}auth/forgettable"><spring:message code="Forgettable"/></a>

	<span style="float: right">
		<a href="logout"><spring:message code="Logout"/></a>
	</span>

<h2><a href="${appurl}auth/dictionary${sort}"><spring:message code="Dictionary"/></a></h2>
${wordList}

</body>
</html>