package com.epam.customorm.service;

import com.epam.customorm.dao.UserEntity;
import com.epam.customorm.dto.UserDto;

import java.util.Objects;

public class UserConverter {
    public static UserEntity toUserEntity(UserDto userDto) {
        UserEntity userEntity = new UserEntity();
        if (Objects.nonNull(userDto.getId())) {
            userEntity.setId(userDto.getId());
        }
        userEntity.setFirstName(userDto.getFirstName());
        userEntity.setLastName(userDto.getLastName());
        userEntity.setAge(userDto.getAge());
        return userEntity;
    }

    public static UserDto toUserDto(UserEntity userEntity) {
        UserDto userDto = new UserDto();
        if (Objects.nonNull(userEntity.getId())) {
            userDto.setId(userEntity.getId());
        }
        userDto.setFirstName(userEntity.getFirstName());
        userDto.setLastName(userEntity.getLastName());
        userDto.setAge(userEntity.getAge());
        return userDto;
    }

}
