package com.example.es.entity;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@ToString
@Accessors(chain = true)
public class User {

    private String name;

    private Integer age;

    private Float salary;

    private String address;

    private String remark;

    private Date createTime;

    private Date birthday;

}
