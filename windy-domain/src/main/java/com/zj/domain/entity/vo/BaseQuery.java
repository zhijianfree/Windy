package com.zj.domain.entity.vo;

import com.zj.domain.entity.enums.QueryType;
import lombok.Data;

import java.util.Objects;

@Data
public abstract class BaseQuery {

    public abstract void setProposer(String userId);
    public abstract void setAcceptor(String userId);

    public void handleQueryType(Integer type,String userId){
        if (Objects.equals(type, QueryType.QUERY_ALL.getType())) {
            this.setProposer(null);
            this.setAcceptor(null);
        }
        if (Objects.equals(type, QueryType.QUERY_HANDLE_BY_MYSELF.getType())) {
            this.setProposer(null);
            this.setAcceptor(userId);
        }
        if (Objects.equals(type, QueryType.QUERY_CREATE_BY_MYSELF.getType())) {
            this.setAcceptor(null);
        }
    }
}
