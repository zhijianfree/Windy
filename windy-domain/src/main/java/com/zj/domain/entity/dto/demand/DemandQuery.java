package com.zj.domain.entity.dto.demand;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DemandQuery {

  Integer status;
  Integer page;
  Integer pageSize;
  String name;
  Boolean searchUser;
}
