<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>


<!DOCTYPE html>
<html>
<head>
	<!--  для правильной загрузки css  -->
	<link href="<%=request.getContextPath()%>/resources/css/login.css" rel="stylesheet"/>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Wordkeeper | Login</title>
</head>

<body>

	<a href="http://localhost:8080/ColibriWeb/"><spring:message code="Home page"/></a>

	<form method="POST" action="login" class="box login">

		<fieldset class="boxBody">

			<div>
				<span style="float: right"> 
					<a href="?lang=en">en</a> 
					<a href="?lang=ru">ru</a>
				</span>
			</div>

			<label> <spring:message code="username"/> </label> <input type='text' name='user_login' value=''>
			<label> <spring:message code="password"/> </label> <input type='password' name='password_login' />
		
			<c:if test="${not empty error}">
				<span class="error">${error}</span>
			</c:if>

		</fieldset>

		<footer>

			<input name="remember-me" type="checkbox" class="checkAdmin" />
			<label for="remember_me"><spring:message code="Remember"/></label>

			<input type="submit" class="btnLogin" value=<spring:message code="login"/>>

		</footer>

	</form>

</body>
</html>