package com.zj.client.entity.dto;

import com.zj.client.entity.vo.ApiModel;
import java.util.List;
import lombok.Data;

@Data
public class GenerateDto {

  /**
   * 服务名称
   * */
  private String service;

  /**
   * 服务Id
   * */
  private String serviceId;

  /**
   * 打包的包名路径
   * */
  private String packageName;

  /**
   * maven打包的版本
   * */
  private String version;

  /**
   * jar包groupId
   * */
  private String groupId;

  /**
   * jar包artifactId
   * */
  private String artifactId;

  /**
   * 访问maven的用户
   * */
  private String mavenUser;

  /**
   * 访问maven的密码
   * */
  private String mavenPwd;

  /**
   * 访问的maven仓库地址(settings中配置的地址)
   * */
  private String mavenRepository;

  private List<ApiModel> apiList;
}
