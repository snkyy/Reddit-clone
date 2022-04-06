<#include "base.ftlh"/>
<@page title="Manage community ${community.name}">

<p><a href="community?name=${community.name}">Community main page</a></p>

<p class="section">Update description:</p>
<form action="update-community-description" method="post">
    <input type="hidden" name="community" value="${community.id?c}"/>
    <textarea name="description">${community.description}</textarea>
    <p><input type="submit" value="Update"/></p>
</form>

<p class="section">Moderators:</p>
<ul>
    <#list moderators as moderator>
    <#assign user = moderator.user/>
    <li><a href="user?name=${user.name}">${user.name}</a></li>
    </#list>
</ul>

<#if subscriberNotFound>
<p class="error">Subscriber not found</p>
</#if>

<p class="section">Add moderator:</p>
<form action="add-moderator" method="post">
    <input type="hidden" name="community" value="${community.id?c}"/>
    <p>User name: <input type="text" name="name"/></p>
    <p><input type="submit" value="Add"/></p>
</form>

<p class="section">Remove moderator:</p>
<form action="remove-moderator" method="post">
    <input type="hidden" name="community" value="${community.id?c}"/>
    <p>User name: <input type="text" name="name"/></p>
    <p><input type="submit" value="Remove"/></p>
</form>

<p class="section">Delete community:</p>
<form action="delete-community" method="post">
    <input type="hidden" name="community" value="${community.id?c}"/>
    <p>Confirm community name: <input type="text" name="name-confirm"/></p>
    <p><input type="submit" value="Delete"/></p>
</form>

</@page>
