package dk.kb.kula190.api.impl;

import dk.kb.avischk.qa.web.ContentLocationResolver;
import dk.kb.kula190.api.DefaultApi;
import dk.kb.kula190.dao.DAOFailureException;
import dk.kb.kula190.dao.NewspaperQADao;
import dk.kb.kula190.dao.NewspaperQADaoFactory;
import dk.kb.kula190.model.Batch;
import dk.kb.kula190.model.CharacterizationInfo;
import dk.kb.kula190.model.NewspaperDate;
import dk.kb.kula190.model.NewspaperEntity;
import dk.kb.kula190.webservice.ServiceExceptionMapper;
import dk.kb.kula190.webservice.exception.InternalServiceException;
import dk.kb.kula190.webservice.exception.ServiceException;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.io.FileNotFoundException;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * AvisScanQA_web
 *
 * <p>This pom can be inherited by projects wishing to integrate to the SBForge development platform.
 */
public class DefaultApiServiceImpl implements DefaultApi {
    private Logger log = LoggerFactory.getLogger(this.toString());
    
    
    
    /* How to access the various web contexts. See https://cxf.apache.org/docs/jax-rs-basics.html#JAX-RSBasics-Contextannotations */
    
    @Context
    private transient UriInfo uriInfo;
    
    @Context
    private transient SecurityContext securityContext;
    
    @Context
    private transient HttpHeaders httpHeaders;
    
    @Context
    private transient Providers providers;
    
    @Context
    private transient Request request;
    
    @Context
    private transient ContextResolver<?> contextResolver;
    
    @Context
    private transient HttpServletRequest httpServletRequest;
    
    @Context
    private transient HttpServletResponse httpServletResponse;
    
    @Context
    private transient ServletContext servletContext;
    
    @Context
    private transient ServletConfig servletConfig;
    
    @Context
    private transient MessageContext messageContext;
    
    
    private NewspaperQADao dao;
    
    public DefaultApiServiceImpl() {
        log.info("Initializing service");
        dao = NewspaperQADaoFactory.getInstance();
    }
    
    @Override
    public List<String> getNewspaperIDs() {
        try {
            List<String> IDs = dao.getNewspaperIDs();
            return IDs;
        } catch (DAOFailureException e) {
            log.error("Could not get newspaper IDs from backend");
            throw handleException(e);
        }
    }
    
    @Override
    public List<String> getYearsForNewspaper(String newspaperID) {
        try {
            List<String> years = dao.getYearsForNewspaperID(newspaperID);
            return years;
        } catch (DAOFailureException e) {
            log.error("Could not get dates for newspaper ID {}", newspaperID);
            throw handleException(e);
        }
    }

    @Override
    public void setDayNotes(String dayNotes, String submit, String edition, LocalDate date, String avis, String batch) {
        log.info("{}, {}, {}, {}, {}, {}", dayNotes, submit, edition, date, avis, batch);
    }

    @Override
    public void setEditionNotes(String editionNotes,
                                String submit,
                                String edition,
                                LocalDate date,
                                String avis,
                                String batch) {
        log.info("{}, {}, {}, {}, {}, {}", editionNotes, submit, edition, date, avis, batch);
    }

    @Override
    public void setPageNotes(String pageNotes,
                             String submit,
                             String edition,
                             LocalDate date,
                             String avis,
                             String batch) {
        log.info("{}, {}, {}, {}, {}, {}", pageNotes, submit, edition, date, avis, batch);

    }

    @Override
    public List<Batch> getBatchIDs() {
        try {
            List<Batch> IDs = dao.getBatchIDs();
            return IDs;
        } catch (DAOFailureException e) {
            log.error("Could not get newspaper IDs from backend");
            throw handleException(e);
        }
    }
    
    
    @Override
    public Batch getBatchForNewspaper(String batchID) {
        try {
            //TODO this is a bad way to get a specific batch...
            return dao.getBatchIDs().stream().filter(batch -> batch.getBatchid().equals(batchID)).findFirst().orElse(null);
        } catch (DAOFailureException e) {
            log.error("Could not get dates for newspaper ID {}", batchID);
            throw handleException(e);
        }
    }
    
    @Override
    public List<NewspaperDate> getBatchDatesForNewspaper(String batchID, String year) {
        try {
            List<NewspaperDate> dates = dao.getDatesForBatchID(batchID, year);
            return dates;
        } catch (DAOFailureException e) {
            log.error("Could not get dates for batch ID {}", batchID);
            throw handleException(e);
        }

    }
    
    
    @Override
    public Map<String, List<NewspaperEntity>> getMappedEntititesForNewspaperDate(String newspaperID, String date) {
        try {
            Map<String, List<NewspaperEntity>> entities = dao.getMappedEditionsForNewspaperOnDate(newspaperID, date);
            return entities;
        } catch (DAOFailureException e) {
            log.error("Could not get entities for date {} for newspaper ID {}", date, newspaperID);
            throw handleException(e);
        }
        
    }
    
    @Override
    public List<NewspaperDate> getDatesForNewspaperYear(String newspaperID, String year) {
        try {
            List<NewspaperDate> dates = dao.getDatesForNewspaperID(newspaperID, year);
            return dates;
        } catch (DAOFailureException e) {
            log.error("Could not get dates for newspaper ID {}", newspaperID);
            throw handleException(e);
        }
    }
    
    //This is used for the content display, which we do not need any more
    @Override
    public List<CharacterizationInfo> getEntityCharacterization(Long handle) {
        try {
            List<CharacterizationInfo> characterisations = dao.getCharacterizationForEntity(handle);
            return characterisations;
        } catch (DAOFailureException e) {
            log.error("Could not get characterisation for newspaper with handle {}", handle);
            throw handleException(e);
        }
    }
    
    //This is used for the content display, which we do not need any more
    @Override
    public URI getEntityURL(Long handle, String type) {
        String relPath;
        try {
            relPath = dao.getOrigRelPath(handle);
            
        } catch (DAOFailureException e) {
            log.error("Could not get relative path for handle {}", handle);
            throw handleException(e);
        }
        try {
            return URI.create(ContentLocationResolver.getContent(relPath, type));
        } catch (FileNotFoundException e) {
            log.error("Could not find file {} in filesystem", relPath);
            throw handleException(e);
        }
    }
    
 
    
    
    /**
     * This method simply converts any Exception into a Service exception
     *
     * @param e: Any kind of exception
     * @return A ServiceException
     * @see ServiceExceptionMapper
     */
    private ServiceException handleException(Exception e) {
        if (e instanceof ServiceException) {
            return (ServiceException) e; // Do nothing - this is a declared ServiceException from within module.
        } else {// Unforseen exception (should not happen). Wrap in internal service exception
            log.error("ServiceException(HTTP 500):", e); //You probably want to log this.
            return new InternalServiceException(e.getMessage());
        }
    }
    
}
