package com.labyrinth.core.database;

import java.util.List;
import java.util.Optional;

public abstract class BaseService<T, ID> {

    protected final HibernateMiddleware db = HibernateMiddleware.getInstance();
    private final Class<T> entityClass;

    protected BaseService(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void save(T entity) {
        db.save(entity);
    }

    public T merge(T entity) {
        return db.merge(entity);
    }

    public void delete(T entity) {
        db.delete(entity);
    }

    public Optional<T> findById(ID id) {
        return db.findById(entityClass, id);
    }

    public List<T> findAll() {
        return db.findAll(entityClass);
    }

    public List<T> query(String jpql, Object... params) {
        return db.query(jpql, entityClass, params);
    }

    public Optional<T> queryFirst(String jpql, Object... params) {
        return db.queryFirst(jpql, entityClass, params);
    }
}