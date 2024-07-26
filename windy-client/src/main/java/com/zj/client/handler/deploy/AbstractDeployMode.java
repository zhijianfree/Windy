package com.zj.client.handler.deploy;

import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.common.enums.ProcessStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractDeployMode<T extends DeployContext> implements IDeployMode<T>{

    protected final Map<String, QueryResponseModel> statusMap = new HashMap<>();

    public void updateDeployStatus(String recordId, ProcessStatus processStatus) {
        statusMap.put(recordId, convertStatus(processStatus));
    }

    public void updateDeployStatus(String recordId, ProcessStatus processStatus, List<String> messages) {
        statusMap.put(recordId, convertStatus(processStatus, messages));
    }

    private QueryResponseModel convertStatus(ProcessStatus processStatus){
        QueryResponseModel queryResponseModel = new QueryResponseModel();
        queryResponseModel.setStatus(processStatus.getType());
        queryResponseModel.setMessage(Collections.singletonList(processStatus.getDesc()));
        queryResponseModel.setData(new QueryResponseModel.ResponseStatus(processStatus.getType()));
        return queryResponseModel;
    }

    private QueryResponseModel convertStatus(ProcessStatus processStatus, List<String> messages){
        QueryResponseModel queryResponseModel = new QueryResponseModel();
        queryResponseModel.setStatus(processStatus.getType());
        queryResponseModel.setMessage(messages);
        queryResponseModel.setData(new QueryResponseModel.ResponseStatus(processStatus.getType()));
        return queryResponseModel;
    }
}
