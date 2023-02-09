package com.epam.customorm.orm.pool;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectorCreator {
    private static final Logger logger = LogManager.getLogger(ConnectorCreator.class);
    static final Properties properties = new Properties();
    private static final String DATABASE_URL;
    private static final int POOL_SIZE_MIN;
    private static final int POOL_SIZE_MAX;
    private static final String FILE_PROPERTIES = "db.properties";
    private static ConnectorCreator instance = null;

    /**
     * Instantiates Connection creator.
     */
    static {
        try {
            InputStream inputStream;
            inputStream = ConnectorCreator.class.getClassLoader().getResourceAsStream(FILE_PROPERTIES);
            if (inputStream == null) throw new AssertionError();
            properties.load(inputStream);
            String driver = (String) properties.get("db.driver");
            Class.forName(driver);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        DATABASE_URL = properties.getProperty("db.url");
        POOL_SIZE_MIN = Integer.parseInt(properties.getProperty("pool.size.min"));
        POOL_SIZE_MAX = Integer.parseInt(properties.getProperty("pool.size.max"));
    }

    private ConnectorCreator() {
    }

    public static ConnectorCreator getInstance() {
        if (instance == null) {
            instance = new ConnectorCreator();
        }
        return instance;
    }

    /**
     * Produce database connection.
     *
     * @return the proxy connection
     */
    public ProxyConnection createConnection() throws SQLException {

        Connection connection = DriverManager.getConnection(DATABASE_URL, properties);
        return new ProxyConnection(connection);

    }

    public int getPoolSizeMin() {
        return POOL_SIZE_MIN;
    }

    public int getPoolSizeMax() {
        return POOL_SIZE_MAX;
    }
}
