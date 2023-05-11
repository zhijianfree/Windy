package com.zj.feature.ability.kafka;

import java.util.Map;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/1/11
 */
@Data
public class KafkaResult {

  private String key;

  private String value;

  private String topic;

  private Map<String, String> header;

  private String partitionId;
}
