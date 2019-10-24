package com.yunjisuan.presentation.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Document(collection = "city_code")
public class CityCode {
    @Id
    private String id;
    @Field("code")
    private String code;
    @Field("name")
    private String pro;
    @Field("children")
    private List<Child> children;
}
