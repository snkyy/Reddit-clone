<#switch error>
    <#case "not-found">
        <#assign title = "Not found"/>
        <#break/>
    <#case "bad-request">
        <#assign title = "Bad request"/>
        <#break/>
    <#case "forbidden">
        <#assign title = "Forbidden"/>
        <#break/>
    <#default>
        <#assign title = "Error"/>
</#switch>

<#include "base.ftlh"/>
<@page title=title>
<code>${error}:${details}</code>
</@page>
