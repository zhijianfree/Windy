package ${packageName}.model;

import java.io.Serializable;

public class ${className} implements Serializable {

<#list properties as item>
  private ${item.type} ${item.name};

</#list>
<#list properties as item>
  public void set${item.nameUpper}(${item.type} ${item.name}){
    this.${item.name}=${item.name};
  }

  public ${item.type} get${item.nameUpper}(){
    return this.${item.name};
  }
</#list>
}