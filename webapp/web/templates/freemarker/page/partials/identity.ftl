<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#import "lib-list.ftl" as l>

<div id="identity">

    <h1><a title="Home" href="${urls.home}">${siteName}</a></h1>
    
    <#if siteTagline?has_content>
        <em>${siteTagline}</em>
    </#if>
    
    <ul id="otherMenu">  
        <@l.firstLastList>  
            <#if user.loggedIn>
                <li>
                    Logged in as <strong>${user.loginName}</strong> (<a href="${urls.logout}">Log out</a>)     
                </li>
                <#if user.hasSiteAdminAccess>
                    <li><a href="${urls.siteAdmin}">Site Admin</a></li>
                </#if>
            <#else>
                 <li><a title="log in to manage this site" href="${urls.login}">Log in</a></li>
            </#if> 
            
            <#include "subMenuLinks.ftl">
        </@l.firstLastList>       
    </ul>   
</div>