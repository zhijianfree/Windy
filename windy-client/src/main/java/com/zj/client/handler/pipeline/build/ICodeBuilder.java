package com.zj.client.handler.pipeline.build;

public interface ICodeBuilder {

    String codeType();

    Integer build(CodeBuildContext context, IBuildNotifyListener notifyListener);
}
