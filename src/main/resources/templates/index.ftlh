<#include "base.ftlh"/>
<@page title="Forum">

<#if loggedInUser??>
<p>Logged in as <a href="user?name=${loggedInUser.name}">${loggedInUser.name}</a></p>
<p><a href="account">Account settings</a></p>
<p><a href="logout">Logout</a></p>
<p class="section">Subscriptions:</p>
<ul>
    <#list subscriptions as subscription>
    <#assign name = subscription.community.name>
    <li><a href="community?name=${name}">${name}</a></li>
    </#list>
</ul>
<#else>
<p><a href="login">Log in</a></p>
<p><a href="register">Register</a></p>
</#if>

<p><a href="community-list?page=0">Browse communities</a></p>

<#if loggedInUser??>
<p><a href="create-community">Create new community</a></p>

<p class="section">Feed:</p>
<#list feed as post>
<div class="item">
    <p>${post.community.name}: <a href="post?post=${post.id?c}">${post.title}</a></p>
    <p class="info">${date(post.sentTime)}, ${post.sender.name}, ${post.points} points</p>
</div>
</#list>
</#if>

</@page>
