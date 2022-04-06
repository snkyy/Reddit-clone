<#include "base.ftlh"/>
<@page title="Register">

<#if nameInvalid>
<p class="error">Invalid user name</p>
</#if>
<#if emailInvalid>
<p class="error">Invalid email</p>
</#if>
<#if nameTaken>
<p class="error">User name taken</p>
</#if>
<#if passwordsMismatch>
<p class="error">Passwords do not match</p>
</#if>

<form action="do-register" method="post">
    <p>User name: <input type="text" name="name"/></p>
    <p>Email (optional): <input type="text" name="email"/></p>
    <p>Password: <input type="password" name="password"/></p>
    <p>Confirm password: <input type="password" name="password-confirm"/></p>
    <p><input type="submit" value="Register"/></p>
</form>

</@page>
