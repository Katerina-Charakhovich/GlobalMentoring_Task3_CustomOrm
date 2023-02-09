package com.epam.customorm.orm.metadata;

import com.epam.customorm.orm.annotation.Column;
import com.epam.customorm.orm.annotation.DBTable;
import com.epam.customorm.orm.annotation.Id;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class TableMetaData<T> {
    private static final String COMMA_AND_SPACE = ", ";
    private static final String EQUAL_QUESTION_COMMA_SPACE = "=?, ";
    private static final String EQUAL_QUESTION_SPACE = "=?";

    public String getTableName(Class<?> entityClass) {
        return Optional.ofNullable(entityClass.getAnnotation(DBTable.class))
                .map(DBTable::name)
                .orElse(entityClass.getSimpleName().toLowerCase());
    }

    public String getPrimaryKeyColumnName(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(this::getColumnName)
                .collect(joining());
    }

    public String getColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .filter(annotation -> annotation.name().length() > 0)
                .map(Column::name)
                .orElse(field.getName());

    }



    public String getColumnNamesForCreate(Class<?> entityClass) {
        Field[] declaredFields = entityClass.getDeclaredFields();

        return Arrays.stream(declaredFields)
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .map(Field::getName)
                .collect(joining( COMMA_AND_SPACE)).trim();
    }

    public String getColumnNamesForUpdate(List<Field> declaredFields) {
        String columns = declaredFields.stream()
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .filter(field -> field.isAnnotationPresent(Column.class))
                .map(Field::getName)
                .collect(joining(EQUAL_QUESTION_COMMA_SPACE)).trim();
        return columns+EQUAL_QUESTION_SPACE;
    }

    public List<Field> getFieldsForUpdate(T entity) {
        Field[] declaredFields = entity.getClass().getDeclaredFields();
        for (Field field : declaredFields
        ) {
            field.setAccessible(true);
        }

        return Arrays.stream(declaredFields)
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .filter(field -> {
                    try {
                        return Objects.nonNull(field.get(entity));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    public List<Field> getFieldsWithoutIdForCreate(Class<?> entityClass) {
        Field[] declaredFields = entityClass.getDeclaredFields();

        return Arrays.stream(declaredFields)
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .collect(Collectors.toList());
    }

    public Optional<Field> getIdField(Class<?> entityClass) {

        Field[] declaredFields = entityClass.getDeclaredFields();

        return Arrays.stream(declaredFields)
                .filter(field -> field.isAnnotationPresent(Id.class)).findFirst();
    }

    public List<T> ResultSetMapper(ResultSet resultSet, Class<T> t) throws Exception {
        List<T> entities = new ArrayList<>();
        while (resultSet.next()) {
            T entity = (T) t.newInstance();
            for (Field field : t.getDeclaredFields()) {
                field.setAccessible(true);
                setFieldFromResultSet(field, resultSet, entity, getColumnName(field));
            }
            entities.add(entity);
        }
        return entities.isEmpty() ? null : entities;
    }

    private void setFieldFromResultSet(Field field, ResultSet resultSet, T entity, String columnName) throws Exception {
        String fieldType = field.getType().getSimpleName();
        switch (fieldType) {
            case ("Long"):
                field.set(entity, resultSet.getLong(columnName));
                break;
            case ("Integer"):
                field.set(entity, resultSet.getInt(columnName));
                break;
            case ("String"):
                field.set(entity, resultSet.getString(columnName));
                break;
            case ("Boolean"):
                field.set(entity, resultSet.getBoolean(columnName));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + field.getType());
        }
    }

    public void setFieldToPrepareStatement(Field field, PreparedStatement preparedStatement, int index, T entity) throws Exception {
        String fieldType = field.getType().getSimpleName();
        field.setAccessible(true);
        switch (fieldType) {
            case ("Long"):
                preparedStatement.setLong(index, (Long) field.get(entity));
                break;
            case ("Integer"):
                preparedStatement.setInt(index, (Integer) field.get(entity));
                break;
            case ("String"):
                preparedStatement.setString(index, (String) field.get(entity));
                break;
            case ("Boolean"):
                preparedStatement.setBoolean(index, (Boolean) field.get(entity));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + field.getType());
        }
    }
}
