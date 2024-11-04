package com.zj.client.handler.pipeline.build.go;

import com.zj.client.handler.pipeline.build.IBuildNotifyListener;
import com.zj.client.handler.pipeline.build.ICodeBuilder;
import org.springframework.stereotype.Component;

@Component
public class GoCodeBuilder implements ICodeBuilder {

    @Override
    public void build(String targetDir, IBuildNotifyListener notifyListener) {

    }
}
