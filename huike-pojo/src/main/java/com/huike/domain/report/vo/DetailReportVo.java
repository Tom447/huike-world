package com.huike.domain.report.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailReportVo {
    private String date;
    private Integer newClueCount;
    private Integer newBusinessCount;
    private Integer newContractCount;
    private Double saleMoney;
}
