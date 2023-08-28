package ${packageName}.rest;

import ${packageName}.model.*;
import ${packageName}.service.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
public class ${className}Rest {

  private final I${className}Service ${className}Service;

  public ${className}Rest(I${className}Service ${className}Service){
    this.${className}Service = ${className}Service;
  }

<#list paramList as item>
  @RequestMapping(value = "${item.uri}", method = RequestMethod.${item.httpMethod})
  public ${item.resultClass} ${item.methodName}(<#list item.params as param><#if param_index !=0 && param.position != "Body"> </#if><#if param.position == "Path">@PathVariable(value ="${param.name}" <#if param.required == "false">, required = ${param.required}</#if>) ${param.type} ${param.name}<#if param_has_next>,</#if></#if><#if param.position == "Query">@RequestParam(value ="${param.name}"<#if param.required == "false">, required = ${param.required}</#if>) ${param.type} ${param.name}<#if param_has_next>,</#if></#if><#if param.position == "header">@RequestHeader(value ="${param.name}"<#if param.required == "false">, required = ${param.required}</#if>) ${param.type} ${param.name}<#if param_has_next>,</#if></#if></#list><#if item.bodyClass?? && item.bodyClass != "">@RequestBody ${item.bodyClass} ${item.lowerBodyClass}</#if>) {
    return ${className}Service.${item.methodName}(<#list item.params as param><#if param_index !=0 && param.position != "Body"> </#if><#if param.position != "Body">${param.name}<#if param_has_next>,</#if></#if></#list><#if item.bodyClass?? && item.bodyClass != "">${item.lowerBodyClass}</#if>);
  }

</#list>
}
