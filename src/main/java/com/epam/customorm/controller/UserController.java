package com.epam.customorm.controller;

import com.epam.customorm.dto.UserDto;
import com.epam.customorm.exception.DaoException;
import com.epam.customorm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import java.util.Objects;
import java.util.Optional;


@RestController
@RequestMapping(value = "/users")
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable long id) throws DaoException {
        Optional<UserDto> userDto = userService.findEntityById(id);
        return new ResponseEntity(userDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable long id) throws DaoException {
        userService.delete(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void create(@RequestBody UserDto userDto) throws DaoException {
        userService.create(userDto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable long id,
            @RequestBody UserDto userDto) throws DaoException {
        if (!Objects.nonNull(userDto.getId())){
            userDto.setId(id);
        }
        userService.update(userDto);
    }

    @GetMapping(value = "/")
    public @ResponseBody
    String status() {
        return "Status green";
    }
}
