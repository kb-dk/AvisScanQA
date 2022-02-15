package dk.kb.kula190.dao;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;
import java.util.Properties;

public class NewspaperQADaoFactory {
    private final static Logger log = LoggerFactory.getLogger(NewspaperQADaoFactory.class);
    
    private static final String POSTGREQL_DB_DRIVER = "org.postgresql.Driver";
    private static final String H2_DRIVER = "org.h2.Driver";
    private static boolean initialized = false;
    private static ComboPooledDataSource connectionPool;
    private static NewspaperQADao daoInstance = null;
    
    public static synchronized void initialize(String jdbcConnectionString, String jdbcUser,
            String jdbcPassword) throws PropertyVetoException {
        if(! initialized) {
            log.info("Initializing NewspaperQADaoFactory");
            initialized = true;
            
            Properties p = new Properties(System.getProperties());
            p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
            p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF"); // or any other
            System.setProperties(p);
            
            connectionPool = new ComboPooledDataSource();
            connectionPool.setDriverClass(H2_DRIVER);
            connectionPool.setJdbcUrl(jdbcConnectionString);
            connectionPool.setUser(jdbcUser);
            connectionPool.setPassword(jdbcPassword);
        }
    }
    
    public static synchronized NewspaperQADao getInstance() {
        if(! initialized) {
            throw new RuntimeException("NewspaperQADaoFactory has not been initialized");
        }
        
        if(daoInstance == null) {
            daoInstance = new NewspaperQADao(connectionPool);
        }
        
        return daoInstance;
    }
    
    
}
