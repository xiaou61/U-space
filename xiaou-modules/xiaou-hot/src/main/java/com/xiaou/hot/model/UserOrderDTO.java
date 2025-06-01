package com.xiaou.hot.model;

import lombok.Data;

import java.util.List;

@Data
public class UserOrderDTO {
    private String createdUser;
    private List<TorderItem> orderItems;
    private Integer totalPrice;
}
