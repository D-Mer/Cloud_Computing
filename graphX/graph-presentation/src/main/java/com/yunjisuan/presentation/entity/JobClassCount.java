package com.yunjisuan.presentation.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "job_class_count")
public class JobClassCount {
    @Id
    private String id;
    @Field("type")
    private String type;
    @Field("count")
    private int count;
    @Field("salary1_count")
    private int salary1_count;
    @Field("salary2_count")
    private int salary2_count;
    @Field("salary3_count")
    private int salary3_count;
    @Field("salary4_count")
    private int salary4_count;
    @Field("salary5_count")
    private int salary5_count;
    @Field("salary6_count")
    private int salary6_count;
    @Field("salary7_count")
    private int salary7_count;
    @Field("salary8_count")
    private int salary8_count;
}
