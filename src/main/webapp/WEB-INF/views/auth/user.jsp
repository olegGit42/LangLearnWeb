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
		xmlHttp.open( "GET", "${appurl}userTimeZone?timezoneOffset=" + timezone, true ); // false for synchronous request
		xmlHttp.send( null );

		function clearContents(id) {
		    document.getElementById(id).value = '';
		}
	</script>

	<span style="float: right">
		<a href="logout" onclick="return confirm('<spring:message code="Logout"/>')"><spring:message code="Logout"/></a>
	</span>

	<div id="main">
		<button id="openbtn" class="openbtn" onclick="openNav()">☰ <spring:message code="Commands"/></button>
		<a href="${appurl}"><spring:message code="Home page"/></a>
		|
		<a href="${appurl}auth/forgettable"><spring:message code="Forgettable"/></a>
		|
		<a href="${appurl}auth/dictionary"><spring:message code="Dictionary"/></a>
		||
		<a href="${appurl}auth/user?refresh=true"><spring:message code="Refresh"/></a>
	</div>

	<div id="mySidebar" class="sidebar">
		<button id="closebtn" class="closebtn" onclick="closeNav()">☰ <spring:message code="Commands"/></button>
		${commands}
	</div>

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
					
			<c:if test="${not empty error}">
				<span class="error">${error}</span>
			</c:if>

			<c:if test="${not empty success}">
				<span class="success">${success}</span>
			</c:if>

		</fieldset>

		<footer>

			<input type="submit" onclick="return confirm('<spring:message code="Add"/>')" class="btnLogin" value=<spring:message code="Add"/> formaction="user/add_new_word">
			<form:checkbox name="planned" path="isPlanned" class="checkPlanned" />
			<a href="${appurl}auth/user?refresh=planned" onclick="return confirm('<spring:message code="Planned"/>')"><label for="planned"><spring:message code="Planned"/></label></a>
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

		function openNav() {
			document.getElementById("mySidebar").style.width = "250px";
			document.getElementById("main").style.marginLeft = "250px";
			document.getElementById("openbtn").hidden = true;
		}

		function closeNav() {
			document.getElementById("mySidebar").style.width = "0";
			document.getElementById("main").style.marginLeft = "0";
			document.getElementById("openbtn").hidden = false;
		}
	</script>

</body>
</html>