<#include "base.ftlh"/>
<@page title="Send reply">

<p>Logged in as ${loggedInUser.name}</p>

<p>Post: <a href="post?post=${comment.post.id?c}">${comment.post.title}</a></p>
<p>Comment:</p>
<@commentItem comment/>

<form action="do-send-reply" method="post">
    <input type="hidden" name="comment" value="${comment.id?c}"/>
    <p>Content:</p>
    <textarea name="content"></textarea>
    <p><input type="submit" value="Send"/></p>
</form>

</@page>
