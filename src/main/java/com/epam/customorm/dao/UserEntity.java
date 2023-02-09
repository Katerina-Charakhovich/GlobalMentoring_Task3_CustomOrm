package com.epam.customorm.dao;

import com.epam.customorm.orm.Entity;
import com.epam.customorm.orm.FieldType;
import com.epam.customorm.orm.annotation.Column;
import com.epam.customorm.orm.annotation.DBTable;
import com.epam.customorm.orm.annotation.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;


@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@DBTable(name = "USER")
public class UserEntity extends Entity {
    @Id
    @Column(name = "id", type = FieldType.BIGINT)
    Long id;
    @Column(name = "firstName", type = FieldType.VARCHAR)
    private String firstName;
    @Column(name = "lastName", type = FieldType.VARCHAR)
    private String lastName;
    @Column(name = "age", type = FieldType.INTEGER)
    Integer age;
}
