<#macro page title>
<html>
<head>
    <title>${title}</title>
    <style>
        p.section { font-weight: bold; }
        p.info { font-size: small; }
        p.error { color: red; }
        div.item { border-left: 3px solid gray; padding-left: 5px; }
        div.indent { margin-left: 20px; }
    </style>
</head>
<body>
<h1>${title}</h1>
<#nested/>
</body>
</html>
</#macro>

<#function date timestamp>
    <#return (timestamp * 1000)?number_to_datetime/>
</#function>

<#macro postItem post>
<div class="item">
    <p><a href="post?post=${post.id?c}">${post.title}</a></p>
    <p class="info">${date(post.sentTime)}, ${post.sender.name}, ${post.points} points</p>
</div>
</#macro>

<#macro commentItem comment>
<div class="item">
    <p>${comment.content}</p>
    <p class="info">${date(comment.sentTime)}, ${(comment.editedTime??)?then("edited, ", "")}
    ${comment.sender.name}, ${comment.points} points, <a href="comment?comment=${comment.id?c}">link</a></p>
</div>
</#macro>
