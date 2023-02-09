package com.epam.customorm.orm;

import com.epam.customorm.exception.DaoException;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface CustomRepository<T> {

    public abstract Optional<T> findById(Class<T> object, long id) throws DaoException, SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException ;

    public abstract boolean delete(Class<T> object, long id) throws DaoException;

    public abstract int create(T entity) throws DaoException;

    public abstract int update(T entity) throws DaoException;
}
