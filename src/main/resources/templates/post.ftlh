<#include "base.ftlh"/>
<@page title="${post.title}">

<p>${post.content}</p>
<p class="info">Sent on ${date(post.sentTime)} by <a href="user?name=${post.sender.name}">${post.sender.name}</a></p>
<#if post.editedTime??>
<p class="info">Edited on ${date(post.editedTime)}</p>
</#if>
<p class="info">${post.points} points, ${votes} votes</p>
<p>Community: <a href="community?name=${post.community.name}">${post.community.name}</a></p>

<#if loggedIn>
    <form action="upvote-post" method="post">
        <input type="hidden" name="post" value="${post.id?c}"/>
        <p><input type="submit" value="Upvote"/></p>
    </form>
    <form action="downvote-post" method="post">
        <input type="hidden" name="post" value="${post.id?c}"/>
        <p><input type="submit" value="Downvote"/></p>
    </form>
    <#if vote??>
        <#if vote.upvote>
            <p>Upvoted on ${date(vote.voteTime)}</p>
        <#else>
            <p>Downvoted on ${date(vote.voteTime)}</p>
        </#if>
        <form action="unvote-post" method="post">
            <input type="hidden" name="post" value="${post.id?c}"/>
            <p><input type="submit" value="Remove vote"/></p>
        </form>
    </#if>
    <p><a href="send-comment?post=${post.id?c}">Send new comment</a></p>
<#else>
    <p><a href="login">Log in</a> to comment or vote</p>
</#if>

<#if sender>
    <p class="section">Edit post:</p>
    <form action="edit-post" method="post">
        <input type="hidden" name="post" value="${post.id?c}"/>
        <textarea name="content"></textarea>
        <p><input type="submit" value="Edit"/></p>
    </form>
</#if>

<#if sender || moderator>
    <p class="section">Delete post:</p>
    <form action="delete-post" method="post">
        <input type="hidden" name="post" value="${post.id?c}"/>
        <p><input type="checkbox" name="confirm" value="true"/> confirm
        <input type="submit" value="Delete"/></p>
    </form>
</#if>

<p class="section">Comments:</p>

<#macro tree node>
<@commentItem node.comment/>
<div class="indent">
    <#list node.children as child>
    <@tree child/>
    </#list>
</div>
</#macro>

<#list comments as node>
<@tree node/>
</#list>

</@page>
