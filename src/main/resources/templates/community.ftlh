<#include "base.ftlh"/>
<@page title="Community ${community.name}">

<p>${community.description}</p>
<p class="info">Created on ${date(community.createdTime)}</p>
<p class="info">${subscribers} subscribers</p>

<p><a href="index">Main page</a></p>

<#if loggedInSubscriber??>
    <#if loggedInSubscriber.user == community.owner>
        <p>You are the owner of this community</p>
        <p><a href="manage-community?community=${community.id?c}">Manage community</a></p>
    <#else>
        <p>Subscribed since ${date(loggedInSubscriber.subscribedTime)}</p>
        <#if loggedInSubscriber.moderator>
            <p>You are a moderator of this community</p>
        </#if>
        <form action="unsubscribe" method="post">
            <input type="hidden" name="community" value="${community.id?c}"/>
            <p><input type="submit" value="Unsubscribe"/></p>
        </form>
    </#if>
<#else>
    <form action="subscribe" method="post">
        <input type="hidden" name="community" value="${community.id?c}"/>
        <p><input type="submit" value="Subscribe"/></p>
    </form>
</#if>

<p><a href="community-posts?name=${community.name}&day=0">Browse all posts</a></p>

<p><a href="send-post?community=${community.id?c}">Send new post</a></p>

<p class="section">Popular posts:</p>

<#list popularPosts as post>
<@postItem post/>
</#list>

</@page>
