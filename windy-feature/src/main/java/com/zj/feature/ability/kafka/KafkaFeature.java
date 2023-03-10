package com.zj.feature.ability.kafka;

import com.zj.feature.ability.Feature;
import com.zj.feature.ability.FeatureDefine;
import com.zj.feature.entity.vo.ExecuteDetail;
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
 * @author falcon
 * @since 2023/1/6
 */
@Slf4j
public class KafkaFeature implements Feature {

  public ExecuteDetail startConsume(String address, String topic, String group) {
    ExecuteDetail executeDetail = new ExecuteDetail();
    saveRequestParam(executeDetail, address, topic, group);

    KafkaConsumer<String, String> consumer = buildConsumer(address, topic, group);
    ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(100));
    System.out.println("get record =" + consumerRecords.count());

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
    executeDetail.setStatus(true);
    executeDetail.setResBody(kafkaResults);
    consumer.commitSync();
    consumer.close();
    return executeDetail;
  }

  public ExecuteDetail produceMessage(String topic, String key, String value, Integer timeout,
      String address) {
    ExecuteDetail executeDetail = new ExecuteDetail();
    executeDetail.addRequestInfo("address: " + address);
    executeDetail.addRequestInfo("topic: " + topic);
    executeDetail.addRequestInfo("key: " + key);
    executeDetail.addRequestInfo("value: " + value);
    executeDetail.addRequestInfo("timeout: " + timeout);
    try {
      Properties props = new Properties();
      props.put("bootstrap.servers", address);
      props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
      props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

      KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
      ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, key, value);
      RecordMetadata recordMetadata = producer.send(record).get(timeout, TimeUnit.SECONDS);
      executeDetail.setStatus(true);
      executeDetail.setResBody(recordMetadata);
      producer.close();
    } catch (Exception e) {
      executeDetail.setStatus(false);
      executeDetail.setErrorMessage(e.getMessage());
      log.error("produceMessage invoke error", e);
    }
    return executeDetail;
  }

  private void saveRequestParam(ExecuteDetail executeDetail, String address, String topic,
      String group) {
    executeDetail.addRequestInfo("address: " + address);
    executeDetail.addRequestInfo("group: " + group);
    executeDetail.addRequestInfo("topic: " + topic);
  }

  private KafkaConsumer<String, String> buildConsumer(String address, String topic, String group) {
    Properties props = new Properties();
    props.put("bootstrap.servers", address);
    props.put("group.id", group);
    // Key ??? Value ??????????????????
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Arrays.asList(topic));
    return consumer;
  }

  @Override
  public List<FeatureDefine> scanFeatureDefines() {
    return null;
  }

  public static void main(String[] args) throws InterruptedException {
    KafkaFeature kafkaFeature = new KafkaFeature();
    kafkaFeature.produceMessage("sun", "111", "{\"name\":\"huhuhuhuhu\"}",
        5, "10.202.162.127:9092");
    Thread.sleep(4000);
    kafkaFeature.startConsume("10.202.162.127:9092", "sun", "group_gong");
  }

}
