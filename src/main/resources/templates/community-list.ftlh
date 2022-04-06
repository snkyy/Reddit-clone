<#include "base.ftlh"/>
<@page title="Community list">

<p><a href="index">Main page</a></p>

<p>Page: ${pageNumber}</p>
<#if pageNumber gt 0>
<p><a href="community-list?page=${(pageNumber - 1)?c}">Previous page</a></p>
</#if>
<p><a href="community-list?page=${(pageNumber + 1)?c}">Next page</a></p>

<#list communities as community>
<div class="item">
    <p><a href="community?name=${community.name}">${community.name}</a></p>
    <p>${community.description}</p>
</div>
</#list>

</@page>
