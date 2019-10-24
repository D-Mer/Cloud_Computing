package com.yunjisuan.presentation.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class Child {
    @Field("code")
    private String code;
    @Field("name")
    private String name;
}
