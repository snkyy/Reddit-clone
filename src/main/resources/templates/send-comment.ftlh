<#include "base.ftlh"/>
<@page title="Send new comment">

<p>Logged in as ${loggedInUser.name}</p>

<p>Post:</p>
<@postItem post/>

<form action="do-send-comment" method="post">
    <input type="hidden" name="post" value="${post.id?c}"/>
    <p>Content:</p>
    <textarea name="content"></textarea>
    <p><input type="submit" value="Send"/></p>
</form>

</@page>
