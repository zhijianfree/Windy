package ${packageName}.service;

import ${packageName}.model.*;
import org.springframework.stereotype.Service;

public interface I${className}Service {

<#list paramList as item>
    ${item.resultClass} ${item.methodName}(<#list item.params as param><#if param_index !=0 && param.position != "Body"> </#if><#if param.position != "Body">${param.type} ${param.name}<#if param_has_next>,</#if></#if></#list><#if item.bodyClass?? && item.bodyClass != "">${item.bodyClass} ${item.lowerBodyClass}</#if>);

</#list>
}
