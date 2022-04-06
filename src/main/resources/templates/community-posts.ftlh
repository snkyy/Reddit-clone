<#include "base.ftlh"/>
<@page title="Community ${community.name} - all posts">

<p><a href="community?name=${community.name}">Community main page</a></p>

<#if day == 0>
<p>Viewing today's posts</p>
<#elseif day == 1>
<p>Viewing yesterday's posts</p>
<#else>
<p>Viewing posts from ${day} days ago</p>
</#if>

<p><a href="community-posts?name=${community.name}&day=${(day + 1)?c}">Previous day</a></p>
<#if day gt 0>
<p><a href="community-posts?name=${community.name}&day=${(day - 1)?c}">Next day</a></p>
</#if>

<#list posts as post>
<@postItem post/>
</#list>

</@page>
