package com.zj.pipeline.entity.vo;

import com.zj.pipeline.executer.vo.TaskNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author guyuelan
 * @since 2023/5/9
 */
@Data
public class StageQueue {

  private String stageId;

  private List<TaskNode> taskNodes;

}
