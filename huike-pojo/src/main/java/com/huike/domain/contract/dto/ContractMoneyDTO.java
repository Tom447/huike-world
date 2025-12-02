package com.huike.domain.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractMoneyDTO {

    private String contractDate;
    private Double contractMoney;

}
