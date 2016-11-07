<%@page isELIgnored="false" language="java" pageEncoding="UTF-8"%>
<%@include file="/common/taglib.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>
<script>
	window.onload = function() {
		var message = "${message}";
		if (message) {
			alert(message);
		}
	}
</script>

</head>
<body>
	<form id=loginForm action="${ctx}/login.do" method="post">
		<table cellpadding=3 align="center">
			<tr>
				<td>用户名：</td>
				<td><input id="username" name="username" /></td>
			</tr>
			<tr>
				<td colspan="2"><input type="submit" value="提交" /></td>
			</tr>
		</table>
	</form>
</body>
</html>
