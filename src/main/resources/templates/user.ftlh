<#include "base.ftlh"/>
<@page title="User ${user.name}">

<#if self>
<p>(Yourself)</p>
<p><a href="account">Account settings</a></p>
<p><a href="logout">Logout</a></p>
</#if>

<p><a href="index">Main page</a></p>

<p>Joined: ${date(user.joinedTime)}</p>
<p>Points: ${user.points}</p>

<p class="section">Posts:</p>
<ul>
    <#list posts as post>
    <li>${date(post.sentTime)} <a href="post?post=${post.id?c}">${post.title}</a></li>
    </#list>
</ul>

<p class="section">Comments:</p>
<ul>
    <#list comments as comment>
    <li>${date(comment.sentTime)} <a href="comment?comment=${comment.id?c}">Comment: ${comment.post.title}</a></li>
    </#list>
</ul>

<#if self>
<p class="section">Post votes:</p>
<ul>
    <#list postVotes as vote>
    <#assign post = vote.post/>
    <li>${date(vote.voteTime)} <a href="post?post=${post.id?c}">Vote: ${post.title}</a></li>
    </#list>
</ul>

<p class="section">Post votes:</p>
<ul>
    <#list commentVotes as vote>
    <#assign comment = vote.comment/>
    <li>${date(vote.voteTime)} <a href="comment?comment=${comment.id?c}">Comment vote: ${comment.post.title}</a></li>
    </#list>
</ul>
</#if>

</@page>
