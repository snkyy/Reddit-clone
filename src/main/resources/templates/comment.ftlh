<#include "base.ftlh"/>
<#assign comment = commentTree.comment/>
<@page title="Comment">

<p>${comment.content}</p>
<p class="info">Sent on ${date(comment.sentTime)} by <a href="user?name=${comment.sender.name}">${comment.sender.name}</a></p>
<#if comment.editedTime??>
<p class="info">Edited on ${date(comment.editedTime)}</p>
</#if>
<p class="info">${comment.points} points, ${votes} votes</p>
<p>Post: <a href="post?post=${comment.post.id?c}">${comment.post.title}</a></p>

<#if loggedIn>
    <form action="upvote-comment" method="post">
        <input type="hidden" name="comment" value="${comment.id?c}"/>
        <p><input type="submit" value="Upvote"/></p>
    </form>
    <form action="downvote-comment" method="post">
        <input type="hidden" name="comment" value="${comment.id?c}"/>
        <p><input type="submit" value="Downvote"/></p>
    </form>
    <#if vote??>
        <#if vote.upvote>
            <p>Upvoted on ${date(vote.voteTime)}</p>
        <#else>
            <p>Downvoted on ${date(vote.voteTime)}</p>
        </#if>
        <form action="unvote-comment" method="post">
            <input type="hidden" name="comment" value="${comment.id?c}"/>
            <p><input type="submit" value="Remove vote"/></p>
        </form>
    </#if>
    <p><a href="send-reply?comment=${comment.id?c}">Send reply</a></p>
<#else>
    <p><a href="login">Log in</a> to reply or vote</p>
</#if>

<#if sender>
    <p class="section">Edit comment:</p>
    <form action="edit-comment" method="post">
        <input type="hidden" name="comment" value="${comment.id?c}"/>
        <textarea name="content"></textarea>
        <p><input type="submit" value="Edit"/></p>
    </form>
</#if>

<#if sender || moderator>
    <p class="section">Delete comment:</p>
    <form action="delete-comment" method="post">
        <input type="hidden" name="comment" value="${comment.id?c}"/>
        <p><input type="checkbox" name="confirm" value="true"/> confirm
        <input type="submit" value="Delete"/></p>
    </form>
</#if>

<#if parent??>
<p class="section">Parent comment:</p>
<@commentItem parent/>
</#if>

<p class="section">Comment tree:</p>

<#macro tree node>
<@commentItem node.comment/>
<div class="indent">
    <#list node.children as child>
    <@tree child/>
    </#list>
</div>
</#macro>

<@tree commentTree/>

</@page>
