package com.huike.domain.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessCountDTO {

    private String businessDate;
    private Integer businessCount;

}
