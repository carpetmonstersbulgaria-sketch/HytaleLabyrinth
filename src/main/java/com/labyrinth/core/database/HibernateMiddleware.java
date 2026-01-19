package com.labyrinth.core.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.cfg.Configuration;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

import static com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil.getLogger;

public class HibernateMiddleware {

    private static HibernateMiddleware INSTANCE;

    private final DatabaseConfig config;
    private EntityManagerFactory entityManagerFactory;

    private HibernateMiddleware() {
        this.config = new DatabaseConfig();
    }

    public static HibernateMiddleware getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HibernateMiddleware();
        }
        return INSTANCE;
    }

    public void connect(Class<?>... entityClasses) {
        try {
            Configuration configuration = new Configuration();

            Properties props = new Properties();
            props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
            props.put("hibernate.connection.url", config.getJdbcUrl());
            props.put("hibernate.connection.username", config.getUsername());
            props.put("hibernate.connection.password", config.getPassword());
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.put("hibernate.hbm2ddl.auto", "create-drop");
            props.put("hibernate.show_sql", "false");
            props.put("hibernate.format_sql", "true");

            // HikariCP settings
            props.put("hibernate.hikari.minimumIdle", String.valueOf(config.getMinIdle()));
            props.put("hibernate.hikari.maximumPoolSize", String.valueOf(config.getMaxPoolSize()));
            props.put("hibernate.hikari.idleTimeout", String.valueOf(config.getIdleTimeout()));
            props.put("hibernate.hikari.connectionTimeout", String.valueOf(config.getConnectionTimeout()));
            props.put("hibernate.hikari.maxLifetime", String.valueOf(config.getMaxLifetime()));
            props.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");

            configuration.setProperties(props);

            for (Class<?> entityClass : entityClasses) {
                configuration.addAnnotatedClass(entityClass);
            }

            this.entityManagerFactory = configuration.buildSessionFactory();
            getLogger().info("[Hibernate] Connected to PostgreSQL successfully");

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "[Hibernate] Failed to connect to PostgreSQL", e);
        }
    }

    public void disconnect() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            getLogger().info("[Hibernate] Disconnected from PostgreSQL");
        }
    }

    public boolean isConnected() {
        return entityManagerFactory != null && entityManagerFactory.isOpen();
    }

    public EntityManager createEntityManager() {
        if (entityManagerFactory == null) {
            throw new IllegalStateException("EntityManagerFactory is not initialized. Call connect() first.");
        }
        return entityManagerFactory.createEntityManager();
    }

    public <T> void save(T entity) {
        executeInTransaction(em -> em.persist(entity));
    }

    public <T> CompletableFuture<Void> saveAsync(T entity) {
        return CompletableFuture.runAsync(() -> save(entity));
    }

    public <T> T merge(T entity) {
        return executeWithResult(em -> {
            em.getTransaction().begin();
            T merged = em.merge(entity);
            em.getTransaction().commit();
            return merged;
        });
    }

    public <T> CompletableFuture<T> mergeAsync(T entity) {
        return CompletableFuture.supplyAsync(() -> merge(entity));
    }

    public <T> void delete(T entity) {
        executeInTransaction(em -> {
            T managed = em.contains(entity) ? entity : em.merge(entity);
            em.remove(managed);
        });
    }

    public <T> CompletableFuture<Void> deleteAsync(T entity) {
        return CompletableFuture.runAsync(() -> delete(entity));
    }

    public <T> Optional<T> findById(Class<T> entityClass, Object id) {
        return executeWithResult(em -> Optional.ofNullable(em.find(entityClass, id)));
    }

    public <T> CompletableFuture<Optional<T>> findByIdAsync(Class<T> entityClass, Object id) {
        return CompletableFuture.supplyAsync(() -> findById(entityClass, id));
    }

    public <T> List<T> findAll(Class<T> entityClass) {
        return executeWithResult(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);
            return em.createQuery(cq).getResultList();
        });
    }

    public <T> CompletableFuture<List<T>> findAllAsync(Class<T> entityClass) {
        return CompletableFuture.supplyAsync(() -> findAll(entityClass));
    }

    public <T> List<T> query(String jpql, Class<T> resultClass, Object... params) {
        return executeWithResult(em -> {
            TypedQuery<T> query = em.createQuery(jpql, resultClass);
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i + 1, params[i]);
            }
            return query.getResultList();
        });
    }

    public <T> CompletableFuture<List<T>> queryAsync(String jpql, Class<T> resultClass, Object... params) {
        return CompletableFuture.supplyAsync(() -> query(jpql, resultClass, params));
    }

    public <T> Optional<T> queryFirst(String jpql, Class<T> resultClass, Object... params) {
        return executeWithResult(em -> {
            TypedQuery<T> query = em.createQuery(jpql, resultClass);
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i + 1, params[i]);
            }
            query.setMaxResults(1);
            List<T> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }

    public int executeUpdate(String jpql, Object... params) {
        return executeWithResult(em -> {
            em.getTransaction().begin();
            var query = em.createQuery(jpql);
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i + 1, params[i]);
            }
            int result = query.executeUpdate();
            em.getTransaction().commit();
            return result;
        });
    }

    public CompletableFuture<Integer> executeUpdateAsync(String jpql, Object... params) {
        return CompletableFuture.supplyAsync(() -> executeUpdate(jpql, params));
    }

    public void executeInTransaction(Consumer<EntityManager> operation) {
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            operation.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            getLogger().log(Level.SEVERE, "[Hibernate] Transaction failed", e);
            throw e;
        } finally {
            em.close();
        }
    }

    public CompletableFuture<Void> executeInTransactionAsync(Consumer<EntityManager> operation) {
        return CompletableFuture.runAsync(() -> executeInTransaction(operation));
    }

    public <T> T executeWithResult(Function<EntityManager, T> operation) {
        EntityManager em = createEntityManager();
        try {
            return operation.apply(em);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "[Hibernate] Operation failed", e);
            throw e;
        } finally {
            em.close();
        }
    }

    public <T> CompletableFuture<T> executeWithResultAsync(Function<EntityManager, T> operation) {
        return CompletableFuture.supplyAsync(() -> executeWithResult(operation));
    }
}