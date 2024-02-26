package com.zj.client.handler.feature.ability.kafka;

import com.zj.client.entity.enuns.ParamTypeEnum;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.plugin.loader.Feature;
import com.zj.plugin.loader.FeatureDefine;
import com.zj.plugin.loader.ParameterDefine;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;

/**
 * @author guyuelan
 * @since 2023/1/6
 */
@Slf4j
public class KafkaFeature implements Feature {

  public ExecuteDetailVo startConsume(String address, String topic, String group) {
    ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
    saveRequestParam(executeDetailVo, address, topic, group);

    KafkaConsumer<String, String> consumer = buildConsumer(address, topic, group);
    ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(100));

    List<KafkaResult> kafkaResults = new ArrayList<>();
    Iterator<ConsumerRecord<String, String>> iterator = consumerRecords.iterator();
    while (iterator.hasNext()) {
      ConsumerRecord<String, String> next = iterator.next();
      KafkaResult result = new KafkaResult();
      result.setKey(next.key());
      result.setTopic(next.topic());
      result.setValue(next.value());
      Map<String, String> headerMap = Arrays.stream(next.headers().toArray()).collect(
          Collectors.toMap(Header::key,
              header -> new String(header.value(), StandardCharsets.UTF_8)));
      result.setHeader(headerMap);
      kafkaResults.add(result);

      log.info("get consume result ={} headers={} value={} topic={}", next.key(), next.headers(),
          next.value(), next.topic());
    }
    executeDetailVo.setStatus(true);
    executeDetailVo.setResBody(kafkaResults);
    consumer.commitSync();
    consumer.close();
    return executeDetailVo;
  }

  public ExecuteDetailVo produceMessage(String topic, String key, String value, Integer timeout,
      String address) {
    ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
    executeDetailVo.addRequestInfo("address: " + address);
    executeDetailVo.addRequestInfo("topic: " + topic);
    executeDetailVo.addRequestInfo("key: " + key);
    executeDetailVo.addRequestInfo("value: " + value);
    executeDetailVo.addRequestInfo("timeout: " + timeout);
    try {
      Properties props = new Properties();
      props.put("bootstrap.servers", address);
      props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
      props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

      KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
      ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
      RecordMetadata recordMetadata = producer.send(record).get(timeout, TimeUnit.SECONDS);
      executeDetailVo.setStatus(true);
      executeDetailVo.setResBody(recordMetadata);
      producer.close();
    } catch (Exception e) {
      executeDetailVo.setStatus(false);
      executeDetailVo.setErrorMessage(e.getMessage());
      log.error("produceMessage invoke error", e);
    }
    return executeDetailVo;
  }

  private void saveRequestParam(ExecuteDetailVo executeDetailVo, String address, String topic,
      String group) {
    executeDetailVo.addRequestInfo("address: " + address);
    executeDetailVo.addRequestInfo("group: " + group);
    executeDetailVo.addRequestInfo("topic: " + topic);
  }

  private KafkaConsumer<String, String> buildConsumer(String address, String topic, String group) {
    Properties props = new Properties();
    props.put("bootstrap.servers", address);
    props.put("group.id", group);
    // Key 和 Value 的反序列化类
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Arrays.asList(topic));
    return consumer;
  }

  @Override
  public List<FeatureDefine> scanFeatureDefines() {
    FeatureDefine sendDefine = getSendKafakDefine();
    FeatureDefine consumeDefine = getConsumeKafakDefine();
    return Arrays.asList(sendDefine, consumeDefine);
  }

  private FeatureDefine getConsumeKafakDefine() {
    FeatureDefine featureDefine = new FeatureDefine();
    featureDefine.setName("ConsumeKafka");
    featureDefine.setSource("com.zj.client.handler.feature.ability.kafka.KafkaFeature");
    featureDefine.setMethod("startConsume");
    featureDefine.setDescription("消费kafka消息");
    List<ParameterDefine> params = new ArrayList<>();
    ParameterDefine topic = new ParameterDefine();
    topic.setParamKey("topic");
    topic.setType(ParamTypeEnum.String.name());
    topic.setDescription("topic");
    params.add(topic);

    ParameterDefine group = new ParameterDefine();
    group.setParamKey("group");
    group.setType(ParamTypeEnum.String.name());
    group.setDescription("消费组");
    params.add(group);

    ParameterDefine address = new ParameterDefine();
    address.setParamKey("address");
    address.setType(ParamTypeEnum.String.name());
    address.setDescription("kafka地址");
    params.add(address);
    featureDefine.setParams(params);
    return featureDefine;
  }

  private FeatureDefine getSendKafakDefine() {
    FeatureDefine featureDefine = new FeatureDefine();
    featureDefine.setName("SendKafka");
    featureDefine.setSource("com.zj.client.handler.feature.ability.kafka.KafkaFeature");
    featureDefine.setMethod("produceMessage");
    featureDefine.setDescription("发送kafka消息");
    List<ParameterDefine> params = new ArrayList<>();
    ParameterDefine topic = new ParameterDefine();
    topic.setParamKey("topic");
    topic.setType(ParamTypeEnum.String.name());
    topic.setDescription("topic");
    params.add(topic);

    ParameterDefine key = new ParameterDefine();
    key.setParamKey("key");
    key.setType(ParamTypeEnum.String.name());
    key.setDescription("key");
    params.add(key);

    ParameterDefine value = new ParameterDefine();
    value.setParamKey("value");
    value.setType(ParamTypeEnum.String.name());
    value.setDescription("消息内容");
    params.add(value);

    ParameterDefine timeout = new ParameterDefine();
    timeout.setParamKey("timeout");
    timeout.setType(ParamTypeEnum.Integer.name());
    timeout.setDescription("发送超时时间");
    params.add(timeout);

    ParameterDefine address = new ParameterDefine();
    address.setParamKey("address");
    address.setType(ParamTypeEnum.String.name());
    address.setDescription("kafka地址");
    params.add(address);
    featureDefine.setParams(params);
    return featureDefine;
  }

  public static void main(String[] args) throws InterruptedException {
    KafkaFeature kafkaFeature = new KafkaFeature();
    kafkaFeature.produceMessage("sun", "111", "{\"name\":\"huhuhuhuhu\"}",
        5, "10.202.162.127:9092");
    Thread.sleep(4000);
    kafkaFeature.startConsume("10.202.162.127:9092", "sun", "group_gong");
  }

}
