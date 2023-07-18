package com.zj.pipeline.entity.vo;

import lombok.Data;

/**
 * 仓库信息
 *
 * @author falcon
 * @since 2023/7/18
 */
@Data
public class GiteaRepository {

  private String name;
  private Permissions permissions;

  @Data
  public static class Permissions {

    private boolean admin;
    private boolean push;
    private boolean pull;

    public boolean checkPermission() {
      return push && pull;
    }
  }
}
