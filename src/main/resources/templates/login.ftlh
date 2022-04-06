<#include "base.ftlh"/>
<@page title="Login">

<#if failed>
<p class="error">Login failed</p>
</#if>

<form action="do-login" method="post">
    <p>Name: <input type="text" name="name"/></p>
    <p>Password: <input type="password" name="password"/></p>
    <p><input type="submit" value="Login"/></p>
</form>

</@page>
