package com.zj.pipeline.git.hook;

import com.zj.pipeline.entity.vo.GitPushResult;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
public interface IGitWebhook {

  String platform();

  GitPushResult webhook(Object data);
}
