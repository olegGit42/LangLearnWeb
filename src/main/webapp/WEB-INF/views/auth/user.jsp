<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html>
<html>
<head>
<link href="<c:url value="/resources/css/user.css" />" rel="stylesheet">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Wordkeeper | ${username}</title>
</head>
<body>

	<script type='text/javascript'>
		var dateVar = new Date();
		var timezone = dateVar.getTimezoneOffset();
		var xmlHttp = new XMLHttpRequest();
		xmlHttp.open( "GET", "http://localhost:8080/ColibriWeb/userTimeZone?timezoneOffset=" + timezone, false ); // false for synchronous request
		xmlHttp.send( null );
		
		function clearContents(id) {
		    document.getElementById(id).value = '';
		}
	</script>

    

	<a href="http://localhost:8080/ColibriWeb/"><spring:message code="Home page"/></a>
	|
	<a href="http://localhost:8080/ColibriWeb/auth/forgettable"><spring:message code="Forgettable"/></a>
	|
	<a href="http://localhost:8080/ColibriWeb/auth/dictionary"><spring:message code="Dictionary"/></a>
	||
	<a href="http://localhost:8080/ColibriWeb/auth/user?refresh=true"><spring:message code="Refresh"/></a>

	<span style="float: right">
		<a href="logout"><spring:message code="Logout"/></a>
	</span>

	<form:form method="POST" modelAttribute="newWord" action="user" class="box login">

		<fieldset class="boxBody">

			<div>
				<span style="float: right"> 
					<a href="?lang=en">en</a> 
					<a href="?lang=ru">ru</a>
				</span>
			</div>

			<div>
				<label for='wordNew' ondblclick="clearContents('wordNew');"> <spring:message code="Word"/> </label>
				<form:input id="wordNew" type='text' name='wordNew' path='word' />
			</div>

			<div>
				<label for='translateNew' ondblclick="clearContents('translateNew');"> <spring:message code="Translate"/> </label>
				<form:input id="translateNew" type='text' name='translateNew' path='translate' />
			</div>
					
			<c:if test="${not empty error_add_word}">
				<span class="error">${error_add_word}</span>
			</c:if>
		
			<c:if test="${not empty error_empty_field}">
				<span class="error">${error_empty_field}</span>
			</c:if>
		
			<c:if test="${not empty success_add_word}">
				<span class="success">${success_add_word}</span>
			</c:if>

		</fieldset>

		<footer>

			<input type="submit" class="btnLogin" value=<spring:message code="Add"/> formaction="user/add_new_word">
			<p><spring:message code="Number of all words"/> ${wordStat.allWordsCount}</p>

		</footer>

	</form:form>

	<form:form method="POST" modelAttribute="repWord" action="user" class="box login">

		<fieldset class="boxBody">

			<div>
				<spring:message code="Repetition"/>${wordStat.repeatDateTime} | <spring:message code="Box"/> ${wordStat.box} | <spring:message code="Today repetition count"/> ${wordStat.todayRepeatCount}
			</div>

			<div>
				<label for='wordRep'> <spring:message code="Word"/> </label>
				<form:input type='text' name='wordRep' path='word' readonly="true"/>
			</div>

			<div>
				<label for='translateRep'> <spring:message code="Translate"/> </label>
				<form:input type='text' name='translateRep' path='translate' readonly="true"/>
			</div> 

		</fieldset>

		<footer>

			<input type="submit" class="btnShow" value=<spring:message code="Show"/> formaction="user/show_word">
			<input type="submit" class="btnRemembered" value=<spring:message code="Remembered"/> formaction="user/remember_word">
			<input type="submit" class="btnForgot" value=<spring:message code="Forgot"/> formaction="user/forgot_word">

		</footer>

	</form:form>

	<script type='text/javascript'>
		document.getElementById('wordNew').addEventListener('mousedown',
				function(e) {
					if (e.which == 2) {
						this.value = '';
					}
				});

		document.getElementById('translateNew').addEventListener('mousedown',
				function(e) {
					if (e.which == 2) {
						this.value = '';
					}
				});
	</script>

</body>
</html>