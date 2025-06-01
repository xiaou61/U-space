package com.xiaou.pay.domain;

import lombok.Data;

@Data
public class PayRequestDTO {
    private String out_trade_no;
    private String name;
    private String money;
    private String clientip;

}
