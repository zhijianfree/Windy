package com.zj.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  /*===================流水线=================*/
  SUCCESS(HttpStatus.OK, "Common.000000", "请求成功"),
  ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Common.000001", "请求失败"),
  PARAM_VALIDATE_ERROR(HttpStatus.BAD_REQUEST, "Common.000005", "参数校验失败"),
  NOT_FOUND_PIPELINE(HttpStatus.NOT_FOUND, "Pipeline.000002", "流水线未找到"),
  NOT_FOUND_CODE_CHANGE(HttpStatus.NOT_FOUND, "Pipeline.000003", "服务变更未找到"),
  NOT_FOUND_PIPELINE_GIT_BIND(HttpStatus.NOT_FOUND, "Pipeline.000004", "流水关联分支未找到"),
  UPDATE_PIPELINE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Pipeline.000005", "更新流水线失败"),
  NOT_FOUND_SERVICE(HttpStatus.NOT_FOUND, "Pipeline.000006", "未找到服务"),

  REQUEST_GIT_SERVER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Pipeline.000007", "请求Git服务错误"),
  DELETE_PIPELINE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Pipeline.000008", "删除流水线错误"),

  BRANCH_ALREADY_BIND(HttpStatus.INTERNAL_SERVER_ERROR, "Pipeline.000009", "分支已经绑定"),
  CREATE_PIPELINE(HttpStatus.INTERNAL_SERVER_ERROR, "Pipeline.000010", "创建流水线失败"),
  NOT_FIND_BRANCH(HttpStatus.BAD_REQUEST, "Pipeline.000011", "没有找到绑定分支，无法执行"),

  PIPELINE_NOT_BIND(HttpStatus.INTERNAL_SERVER_ERROR, "Pipeline.000012", "流水线未绑定分支，无法发布"),
  SERVICE_BRANCH_PUBLISH_EXIST(HttpStatus.INTERNAL_SERVER_ERROR, "Pipeline.000013", "当前分支已推送发布，请查看发布列表"),
  PUBLISH_PIPELINE_EXIST(HttpStatus.BAD_REQUEST, "Pipeline.000014", "当前服务已存在发布流水线,无法创建"),

  NOT_FIND_PIPELINE(HttpStatus.BAD_REQUEST, "Pipeline.000015", "当前流水线不存在"),
  CREATE_BRANCH_ERROR(HttpStatus.BAD_REQUEST, "Pipeline.000016", "创建分支失败"),

  /*==================Service服务==================*/
  NOT_FIND_REPO_CONFIG(HttpStatus.BAD_REQUEST, "Service.000001", "git访问失败，请检查系统配置"),
  REPO_NOT_EXIST(HttpStatus.BAD_REQUEST, "Service.000002", "未发现用户可访问的仓库列表"),
  SERVICE_EXIST_PIPELINE(HttpStatus.BAD_REQUEST, "Service.000003", "服务下存在流水线无法删除"),
  SERVICE_EXIST_FEATURE(HttpStatus.BAD_REQUEST, "Service.000004", "服务下存在测试集无法删除"),
  GIT_NO_PERMISSION(HttpStatus.BAD_REQUEST, "Service.000005", "配置的用户token无权限访问"),
  USER_NO_PERMISSION(HttpStatus.BAD_REQUEST, "Service.000006", "用户未被授权访问当前git地址"),
  MAVEN_NOT_CONFIG(HttpStatus.BAD_REQUEST, "Service.000007", "系统未配置maven仓库地址，请先配置再尝试生成"),
  GENERATE_VERSION_EXIST(HttpStatus.BAD_REQUEST, "Service.000008", "构建Maven而方包失败，版本号已存在"),
  /*==================用例服务==================*/
  COMPARE_ERROR(HttpStatus.FORBIDDEN, "Feature.000002", "feature compare error"),
  EXECUTE_POINT_NOT_FIND(HttpStatus.NOT_FOUND, "Feature.000003", "can not find execute point"),
  FEATURE_NOT_FIND(HttpStatus.NOT_FOUND, "Feature.000004", "can not find feature"),
  SYSTEM_EXECUTE_ERROR(HttpStatus.BAD_REQUEST, "Feature.000101", "unknown error occur when execute feature "),
  SUB_FEATURE_EXIST(HttpStatus.BAD_REQUEST, "Feature.000005", "存在子用例不能删除目录"),
  PARSE_PLUGIN_ERROR(HttpStatus.BAD_REQUEST, "Feature.000006", "解析插件错误"),
  /*==================需求缺陷管理==================*/
  DEMAND_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Demand.000001", "创建需求失败"),
  /*==================Client端构建运行==================*/
  NOT_FIND_JAR(HttpStatus.INTERNAL_SERVER_ERROR, "Client.000001", "构建时未发现JAR包"),
  UNKNOWN_EXECUTE_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "Client.000002", "未找到节点执行类型"),
  RUN_DEPLOY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Client.000003", "部署节点失败"),
  MERGE_CODE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Client.000004", "合并代码失败"),

  BRANCH_NOT_DIFF(HttpStatus.INTERNAL_SERVER_ERROR, "Client.000005", "合并分支%s与master不存在差异，不能发布"),
  ;

  ErrorCode(HttpStatus httpStatus, String code, String message) {
    this.code = code;
    this.message = message;
    this.httpStatus = httpStatus;
  }

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;

}
