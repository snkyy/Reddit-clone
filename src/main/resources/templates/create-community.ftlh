<#include "base.ftlh"/>
<@page title="Create new community">

<p>Logged in as <a href="user?name=${loggedInUser.name}">${loggedInUser.name}</a></p>

<#if nameInvalid>
<p class="error">Invalid community name</p>
</#if>
<#if nameTaken>
<p class="error">Community name taken</p>
</#if>

<form action="do-create-community" method="post">
    <p>Community name: <input type="text" name="name"/></p>
    <p>Description:</p>
    <textarea name="description"></textarea>
    <p><input type="submit" value="Create"/></p>
</form>

</@page>
