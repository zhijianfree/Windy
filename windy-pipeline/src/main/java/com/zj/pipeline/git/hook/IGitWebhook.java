package com.zj.pipeline.git.hook;

/**
 * @author falcon
 * @since 2023/6/27
 */
public interface IGitWebhook {

  String platform();

  void webhook(Object data);
}
