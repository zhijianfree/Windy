package com.zj.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  SUCCESS(HttpStatus.OK, "Common.000000", "请求成功"),
  ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Common.000001", "请求失败"),
  PARAM_VALIDATE_ERROR(HttpStatus.BAD_REQUEST, "Common.000002", "参数校验失败"),
  STATUS_UNCHANGEABLE_ERROR(HttpStatus.BAD_REQUEST, "Common.00003", "状态不可变更"),
  /*===================流水线=================*/
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
  PIPELINE_RUNNING_NOT_DELETE_PUBLISH(HttpStatus.INTERNAL_SERVER_ERROR, "Pipeline.000017", "流水线正在运行，无法删除发布分支"),
  PIPELINE_NOT_BIND_SERVICE(HttpStatus.BAD_REQUEST, "Pipeline.000018", "流水线不属于当前服务"),
  RUN_PIPELINE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Pipeline.000019", "流水线任务调度失败,请稍后重试"),
  SERVICE_CREATE_PUBLISH_PIPELINE(HttpStatus.INTERNAL_SERVER_ERROR, "Pipeline.000020", "当前服务未创建发布流水线，无法推送发布"),
  /*==================Service服务==================*/
  NOT_FIND_REPO_CONFIG(HttpStatus.BAD_REQUEST, "Service.000001", "git访问失败，请检查系统配置"),
  REPO_NOT_EXIST(HttpStatus.BAD_REQUEST, "Service.000002", "未发现用户可访问的仓库列表"),
  SERVICE_EXIST_PIPELINE(HttpStatus.BAD_REQUEST, "Service.000003", "服务下存在流水线无法删除"),
  SERVICE_EXIST_FEATURE(HttpStatus.BAD_REQUEST, "Service.000004", "服务下存在测试集无法删除"),
  GIT_NO_PERMISSION(HttpStatus.BAD_REQUEST, "Service.000005", "配置的用户token无权限访问"),
  USER_NO_PERMISSION(HttpStatus.BAD_REQUEST, "Service.000006", "用户未被授权访问当前git地址"),
  MAVEN_NOT_CONFIG(HttpStatus.BAD_REQUEST, "Service.000007", "系统未配置maven仓库地址，请先配置再尝试生成"),
  GENERATE_VERSION_EXIST(HttpStatus.BAD_REQUEST, "Service.000008", "构建Maven二方包失败，版本号已存在"),
  SERVICE_GENERATE_NAME_EMPTY(HttpStatus.BAD_REQUEST, "Service.000009", "[%s]接口转化的类名、方法名为空"),
  SERVICE_GENERATE_BODY_NAME_EMPTY(HttpStatus.BAD_REQUEST, "Service.000010", "[%s]接口转化的请求体类名为空"),
  SERVICE_GENERATE_BODY_PARAM_NAME_EMPTY(HttpStatus.BAD_REQUEST, "Service.000011", "[%s]接口请求参数类名为空"),
  SERVICE_GENERATE_RESPONSE_PARAM_NAME_EMPTY(HttpStatus.BAD_REQUEST, "Service.000012", "[%s]接口响应参数类名为空"),
  SERVICE_GENERATE_RESPONSE_NAME_EMPTY(HttpStatus.BAD_REQUEST, "Service.000013", "[%s]接口转化的响应类名为空"),
  SERVICE_API_NOT_FIND(HttpStatus.BAD_REQUEST, "Service.000014", "未发现服务API列表"),
  LOAD_CLIENT_BUILD_TOOL_ERROR(HttpStatus.OK, "Service.000015", "加载client节点构建工具失败,节点列表: %s"),
  SERVICE_EXIST_API(HttpStatus.BAD_REQUEST, "Service.000016", "服务下存在API无法删除"),
  SERVICE_EXIST_TEMPLATE(HttpStatus.BAD_REQUEST, "Service.000017", "服务下存在用例模版无法删除"),
  /*==================用例服务==================*/
  COMPARE_ERROR(HttpStatus.FORBIDDEN, "Feature.000002", "feature compare error"),
  EXECUTE_POINT_NOT_FIND(HttpStatus.NOT_FOUND, "Feature.000003", "can not find execute point"),
  FEATURE_NOT_FIND(HttpStatus.NOT_FOUND, "Feature.000004", "can not find feature"),
  SYSTEM_EXECUTE_ERROR(HttpStatus.BAD_REQUEST, "Feature.000101", "unknown error occur when execute feature "),
  SUB_FEATURE_EXIST(HttpStatus.BAD_REQUEST, "Feature.000005", "存在子用例不能删除目录"),
  PARSE_PLUGIN_ERROR(HttpStatus.BAD_REQUEST, "Feature.000006", "解析插件错误"),
  TEMPLATE_NOT_FIND(HttpStatus.NOT_FOUND, "Feature.000007", "未找到模版"),
  BATCH_UPDATE_FEATURE_POINTS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Feature.000008", "批量更新用例执行点失败"),
  NO_TEMPLATE_IN_PLUGIN(HttpStatus.BAD_REQUEST, "Feature.000009", "在插件中未找到模版信息，导入失败"),
  TASK_NOT_FIND(HttpStatus.BAD_REQUEST, "Feature.000010", "任务不存在"),
  TASK_RUN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Feature.000011", "用例任务调度失败，请稍后重试"),
  FEATURE_RUN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Feature.000012", "用例执行调度失败，请稍后重试"),
  COPY_CASE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Feature.000013", "复制用例失败"),
  CASE_EXIST_FEATURE(HttpStatus.INTERNAL_SERVER_ERROR, "Feature.000014", "测试集下存在用例不允许删除"),
  /*==================需求缺陷管理======================*/
  BUG_NOT_EXIST(HttpStatus.BAD_REQUEST, "Bug.000001", "缺陷不存在"),
  BUG_HAS_BIND_BRANCH(HttpStatus.BAD_REQUEST, "Bug.000002", "缺陷已经关联了分支无法删除"),
  DEMAND_HAS_BIND_BRANCH(HttpStatus.BAD_REQUEST, "Demand.000001", "需求已经关联了分支无法删除"),
  DEMAND_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Demand.000002", "创建需求失败"),
  DEMAND_NOT_EXIST(HttpStatus.BAD_REQUEST, "Demand.000004", "需求不存在"),
  SPACE_NOT_EXIST(HttpStatus.BAD_REQUEST, "Space.000001", "空间不存在"),
  SPACE_HAS_NOT_COMPLETE_ITERATION(HttpStatus.BAD_REQUEST, "Space.000002", "空间下有未完成的迭代无法删除"),
  SPACE_HAS_NOT_COMPLETE_DEMAND(HttpStatus.BAD_REQUEST, "Space.000003", "空间下有未完成的需求"),
  SPACE_HAS_NOT_COMPLETE_BUG(HttpStatus.BAD_REQUEST, "Space.000004", "空间下有未完成的bug无法删除"),
  ITERATION_NOT_EXIST(HttpStatus.BAD_REQUEST, "Iteration.000001", "迭代不存在"),
  ITERATION_HAS_NOT_COMPLETE_DEMAND(HttpStatus.BAD_REQUEST, "Iteration.000002", "迭代下有未完成的需求"),
  ITERATION_HAS_NOT_COMPLETE_BUG(HttpStatus.BAD_REQUEST, "Iteration.00003", "迭代下有未完成的bug无法删除"),
  /*==================Client端构建运行==================*/
  NOT_FIND_JAR(HttpStatus.INTERNAL_SERVER_ERROR, "Client.000001", "构建时未发现JAR包"),
  UNKNOWN_EXECUTE_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "Client.000002", "未找到节点执行类型"),
  RUN_DEPLOY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Client.000003", "部署节点失败"),
  MERGE_CODE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Client.000004", "合并代码失败"),
  BRANCH_NOT_DIFF(HttpStatus.INTERNAL_SERVER_ERROR, "Client.000005", "合并分支%s与master不存在差异，不能发布"),
  /*==================权限校验==================*/
  USER_TOKEN_INVALID(HttpStatus.FORBIDDEN,"Auth.000001","token无效"),
  USER_NOT_FIND(HttpStatus.BAD_REQUEST,"Auth.000002","用户不存在"),
  USER_PASSWORD_ERROR(HttpStatus.BAD_REQUEST,"Auth.000003","用户名或者密码错误"),
  USER_WAS_BLOCKED(HttpStatus.BAD_REQUEST,"Auth.000004","用户被冻结"),
  USER_NOT_HAVE_PERMISSION(HttpStatus.BAD_REQUEST,"Auth.000005","用户没有权限"),
  RESOURCE_IS_BIND(HttpStatus.BAD_REQUEST,"Auth.000006","资源被绑定无法删除"),
  ROLE_IS_BIND(HttpStatus.BAD_REQUEST,"Auth.000007","角色被绑定无法删除"),
  GROUP_EXIST_USER(HttpStatus.INTERNAL_SERVER_ERROR,"Auth.000008","组织下存在用户无法删除"),
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
