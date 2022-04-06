<#include "base.ftlh"/>
<@page title="Account settings">

<p><a href="index">Main page</a></p>

<p>User: <a href="user?name=${user.name}">${user.name}</a></p>

<#if emailInvalid>
<p class="error">Invalid email</p>
</#if>
<#if passwordsMismatch>
<p class="error">Passwords do not match</p>
</#if>

<p class="section">Update email:</p>
<form action="update-email" method="post">
    <p>Email (optional): <input type="text" name="email" value="${user.email!""}"/></p>
    <p><input type="submit" value="Update"/></p>
</form>

<p class="section">Update password:</p>
<form action="update-password" method="post">
    <p>Password: <input type="password" name="password"/></p>
    <p>Confirm password: <input type="password" name="password-confirm"/></p>
    <p><input type="submit" value="Update"/></p>
</form>

<p class="section">Delete account:</p>
<form action="delete-account" method="post">
    <p>Password: <input type="password" name="password"/></p>
    <p><input type="submit" value="Delete"/></p>
</form>

</@page>
