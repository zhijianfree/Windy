package com.zj.service.entity;

import com.zj.domain.entity.dto.service.ServiceApiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ImportApiResult {

    private List<ServiceApiDto> apiList;
}
