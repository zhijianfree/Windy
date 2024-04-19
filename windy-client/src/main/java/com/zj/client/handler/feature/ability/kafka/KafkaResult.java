package com.zj.client.handler.feature.ability.kafka;

import lombok.Data;

import java.util.Map;

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
