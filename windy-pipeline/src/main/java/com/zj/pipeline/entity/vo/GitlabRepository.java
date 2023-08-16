package com.zj.pipeline.entity.vo;

import com.alibaba.fastjson.annotation.JSONField;
import java.util.Objects;
import lombok.Data;

/**
 * 仓库信息
 *
 * @author falcon
 * @since 2023/7/18
 */
@Data
public class GitlabRepository {

  private Integer id;
  private String name;
  private Permissions permissions;

  @Data
  public static class Permissions {

    @JSONField(name = "project_access")
    private Access projectAccess;

    @JSONField(name = "group_access")
    private Access groupAccess;

    public boolean checkPermission() {
      if (Objects.nonNull(projectAccess) && projectAccess.getAccessLevel() >= 30) {
        return true;
      }

      return Objects.nonNull(groupAccess) && groupAccess.getAccessLevel() >= 30;
    }
  }

  @Data
  public static class Access {

    private Integer accessLevel;
    private Integer notificationLevel;
  }
}
