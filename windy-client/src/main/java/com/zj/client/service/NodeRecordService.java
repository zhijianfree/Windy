package com.zj.client.service;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.po.NodeRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author guyuelan
 * @since 2023/5/12
 */
@Service
public class NodeRecordService {

  public static final String QUERY_MASTER_NODE_RECORD = "http://WindyMaster/v1/devops/master/record/";
  @Autowired
  private RestTemplate restTemplate;

  public NodeRecord getRecord(String recordId) {
    String url = QUERY_MASTER_NODE_RECORD + recordId;
    String result = restTemplate.getForObject(url, String.class);
    return JSON.parseObject(result, NodeRecord.class);
  }
}
