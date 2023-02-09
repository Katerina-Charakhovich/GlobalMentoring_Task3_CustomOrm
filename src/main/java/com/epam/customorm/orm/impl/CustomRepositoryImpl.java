package com.epam.customorm.orm.impl;

import com.epam.customorm.exception.DaoException;
import com.epam.customorm.orm.CustomRepository;
import com.epam.customorm.orm.Entity;
import com.epam.customorm.orm.metadata.TableMetaData;
import com.epam.customorm.orm.pool.ConnectionPool;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class CustomRepositoryImpl<T extends Entity> implements CustomRepository<T> {

    private TableMetaData<T> tableMetaData = new TableMetaData();
    private static final String FIND_BY_ID_SQL_QUERY = "SELECT * from %s WHERE %s = ?;";
    private static final String CREATE_SQL_QUERY = "INSERT INTO %s (%s) VALUES (%s)";
    private static final String UPDATE_SQL_QUERY = "UPDATE %s SET %s WHERE %s = ?";
    private static final String DELETE_SQL_QUERY = "DELETE FROM %s WHERE %s = ?";
    private static final String COMMA = ",";

    @Override
    public Optional<T> findById(Class<T> object, long id) throws DaoException {
        Connection connection = ConnectionPool.getInstance().getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(String.format(
                    FIND_BY_ID_SQL_QUERY,
                    tableMetaData.getTableName(object),
                    tableMetaData.getPrimaryKeyColumnName(object)));
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> entities = tableMetaData.ResultSetMapper(resultSet, object);
            return Optional.of(entities.get(0));
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            ConnectionPool.getInstance().returnConnection(connection);
        }
    }

    @Override
    public boolean delete(Class<T> object, long id) throws DaoException {
        Connection connection = ConnectionPool.getInstance().getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(String.format(
                    DELETE_SQL_QUERY,
                    tableMetaData.getTableName(object),
                    tableMetaData.getPrimaryKeyColumnName(object)));
            preparedStatement.setLong(1, id);
            int result = preparedStatement.executeUpdate();
            return result == 1;
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            ConnectionPool.getInstance().returnConnection(connection);
        }
    }

    @Override
    public int create(T entity) throws DaoException {
        Connection connection = ConnectionPool.getInstance().getConnection();
        try {
            String columns = tableMetaData.getColumnNamesForCreate(entity.getClass());
            long countColumns = Arrays.stream(columns.split(COMMA)).count();
            StringJoiner preparedStatementValueJoiner = new StringJoiner(COMMA);
            for (int i = 0; i < countColumns; i++) {
                preparedStatementValueJoiner.add(COMMA);
            }
            PreparedStatement preparedStatement = connection.prepareStatement(String.format(
                    CREATE_SQL_QUERY,
                    tableMetaData.getTableName(entity.getClass()),
                    columns,
                    preparedStatementValueJoiner));
            int preparedStatementIndex = 1;
            for (Field field : tableMetaData.getFieldsWithoutIdForCreate(entity.getClass())) {
                tableMetaData.setFieldToPrepareStatement(field,
                        preparedStatement,
                        preparedStatementIndex,
                        entity);
                preparedStatementIndex++;
            }
            return preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            ConnectionPool.getInstance().returnConnection(connection);
        }
    }

    @Override
    public int update(T entity) throws DaoException {
        Connection connection = ConnectionPool.getInstance().getConnection();
        try {
            List<Field> fields = tableMetaData.getFieldsForUpdate(entity);
            String columns = tableMetaData.getColumnNamesForUpdate(fields);
            PreparedStatement preparedStatement = connection.prepareStatement(String.format(
                    UPDATE_SQL_QUERY,
                    tableMetaData.getTableName(entity.getClass()),
                    columns,
                    tableMetaData.getPrimaryKeyColumnName(entity.getClass())
            ), Statement.RETURN_GENERATED_KEYS);
            int preparedStatementIndex = 1;
            for (Field field : fields) {
                tableMetaData.setFieldToPrepareStatement(field, preparedStatement, preparedStatementIndex, entity);
                preparedStatementIndex++;
            }
            Field idField = tableMetaData.getIdField(entity.getClass()).get();
            tableMetaData.setFieldToPrepareStatement(idField, preparedStatement,
                    preparedStatementIndex, entity);
            return preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            ConnectionPool.getInstance().returnConnection(connection);
        }
    }
}
