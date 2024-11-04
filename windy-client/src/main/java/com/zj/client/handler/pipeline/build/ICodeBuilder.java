package com.zj.client.handler.pipeline.build;

public interface ICodeBuilder {

    void build(String targetDir, IBuildNotifyListener notifyListener);
}
