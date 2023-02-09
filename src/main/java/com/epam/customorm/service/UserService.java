package com.epam.customorm.service;

import com.epam.customorm.dao.UserEntity;
import com.epam.customorm.dao.UserRepository;
import com.epam.customorm.dto.UserDto;
import com.epam.customorm.exception.DaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public int create (UserDto userDto) throws DaoException {
        return userRepository.create(UserConverter.toUserEntity(userDto));
    }

    public int update(UserDto userDto) throws DaoException {
        return userRepository.update(UserConverter.toUserEntity(userDto));
    }

    public Optional<UserDto> findEntityById(long id) throws DaoException {
        Optional<UserEntity> userDtoOptional = userRepository.findById(UserEntity.class, id);
        return userDtoOptional.isPresent()
                ? Optional.ofNullable(UserConverter.toUserDto(userDtoOptional.get()))
                : null;
    }

    public boolean delete(long id) throws DaoException {
        return userRepository.delete(UserEntity.class,id);
    }
}
