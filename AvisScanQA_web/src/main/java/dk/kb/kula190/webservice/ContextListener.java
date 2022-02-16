package dk.kb.kula190.webservice;

import dk.kb.kula190.dao.NewspaperQADaoFactory;
import dk.kb.avischk.qa.web.ContentLocationResolver;
import dk.kb.util.yaml.YAML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.beans.PropertyVetoException;
import java.io.IOException;

/**
 * Listener to handle the various setups and configuration sanity checks that can be carried out at when the
 * context is deployed/initalized.
 */
public class ContextListener implements ServletContextListener {
    private final Logger log = LoggerFactory.getLogger(getClass());


    /**
     * On context initialisation this
     * i) Initialises the logging framework (logback).
     * ii) Initialises the configured DorqBackends
     * @param sce
     * @throws RuntimeException if anything at all goes wrong.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            log.info("Initializing WebQA service v{}", getClass().getPackage().getImplementationVersion());
            InitialContext ctx = new InitialContext();
            String configFile = (String) ctx.lookup("java:/comp/env/application-config");
            YAML serviceConfig = YAML.resolveLayeredConfigs(configFile);
    
            String jdbcConnectionString = (String) serviceConfig.getString("avischk-web-qa.jdbc-connection-string");
            String jdbcUser = (String) serviceConfig.getString("avischk-web-qa.jdbc-user");
            String jdbcPassword = (String) serviceConfig.getString("avischk-web-qa.jdbc-password");
            String jdbcDriver = (String) serviceConfig.getString("avischk-web-qa.jdbc-driver");
            NewspaperQADaoFactory.initialize(jdbcConnectionString, jdbcUser, jdbcPassword, jdbcDriver);
            
            String httpContentBase = (String) serviceConfig.getString("avischk-web-qa.http-content-base-string");
            String imageContentBase = (String) serviceConfig.getString("avischk-web-qa.image-content-base-string");
            String iipsrvBase = (String) serviceConfig.getString("avischk-web-qa.iipsrv-base-string");
            
            ContentLocationResolver.setHttpContentBase(httpContentBase);
            ContentLocationResolver.setIipsrvBase(iipsrvBase);
            ContentLocationResolver.setImageContentBase(imageContentBase);
        } catch (NamingException | IOException e) {
            throw new RuntimeException("Failed to lookup settings", e);
        } catch (PropertyVetoException e) {
            throw new RuntimeException("Database connection driver issue", e);
        }
        log.info("WebQA service initialized.");
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.debug("WebQA service destroyed");
    }

}
