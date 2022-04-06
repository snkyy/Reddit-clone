<#include "base.ftlh"/>
<@page title="Send new post">

<p>Logged in as ${loggedInUser.name}</p>

<p>Community: <a href="community?name=${community.name}">${community.name}</a></p>

<form action="do-send-post" method="post">
    <input type="hidden" name="community" value="${community.id?c}"/>
    <p>Title: <input type="text" name="title"/></p>
    <p>Content:</p>
    <textarea name="content"></textarea>
    <p><input type="submit" value="Send"/></p>
</form>

</@page>
