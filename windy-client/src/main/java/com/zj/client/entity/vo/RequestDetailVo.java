package com.zj.client.entity.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class RequestDetailVo {
    private List<String> request = new ArrayList<>();
    private Object requestBody;
}
