package com.yunjisuan.presentation.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "education_count")
public class EducationCount {
    @Id
    private String id;
    @Field("type")
    private String type;
    @Field("count")
    private int count;
}
