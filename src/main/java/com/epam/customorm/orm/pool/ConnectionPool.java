package com.epam.customorm.orm.pool;

import com.epam.customorm.exception.DaoException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public final class ConnectionPool {
    private static ConnectionPool instance = null;
    private BlockingQueue<ProxyConnection> freeConnections;
    private BlockingQueue<ProxyConnection> busyConnections;
    private ConnectorCreator connectorCreator = ConnectorCreator.getInstance();
    private int poolSize;
    private static ReentrantLock lock = new ReentrantLock();
    private static AtomicBoolean isCreate = new AtomicBoolean(false);
    static final Logger logger = LogManager.getLogger(ConnectionPool.class);


    private ConnectionPool() {
        poolSize = connectorCreator.getPoolSizeMin();
        freeConnections = new LinkedBlockingQueue<>();
        busyConnections = new LinkedBlockingQueue<>();
        try {
            for (int i = 0; i < poolSize; i++) {
                ProxyConnection proxyConnection = connectorCreator.createConnection();
                freeConnections.offer(proxyConnection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ConnectionPool getInstance() {
        if (!isCreate.get()) {
            try {
                lock.lock();
                if (instance == null) {
                    instance = new ConnectionPool();
                    isCreate.set(true);
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    /**
     * Take connection proxy connection.
     *
     * @return the proxy connection
     */
    public Connection getConnection() {
        ProxyConnection connection = null;
        try {
            connection = freeConnections.take();
            busyConnections.put(connection);
        } catch (InterruptedException e) {
            logger.log(Level.ERROR, "Error during receiving connection");
        }
        return connection;
    }

    /**
     * Put connection.
     *
     * @param connection the connection
     * @throws DaoException the connection pool exception
     */
    public boolean returnConnection(Connection connection) {
        boolean flag = false;
        try {
            if (connection instanceof ProxyConnection) {
                ProxyConnection proxyConnection = (ProxyConnection) connection;
                if (busyConnections.remove(proxyConnection)) {
                    freeConnections.put(proxyConnection);
                    flag = true;
                } else {
                    throw new DaoException("!!!!!!!");
                }
            } else {
                throw new DaoException("!!!!! ");
            }
        } catch (DaoException | InterruptedException e) {
            e.getStackTrace();
        }
        return flag;
    }

    public void destroyPool() {
        try {
            for (int i = 0; i < poolSize; i++) {
                ProxyConnection connection = freeConnections.take();
                connection.closeProxyConnection();
            }
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
        deregisterDriver();
        logger.info("Connection Pool was destroyed, drivers were deregistered");
    }

    private void deregisterDriver() {
        DriverManager.getDrivers().asIterator().forEachRemaining(driver -> {
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                logger.error(e);
            }
        });
    }

}
