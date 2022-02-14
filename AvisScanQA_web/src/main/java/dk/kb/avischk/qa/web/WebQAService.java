package dk.kb.avischk.qa.web;

import dk.kb.avischk.dao.DAOFailureException;
import dk.kb.avischk.dao.NewspaperQADao;
import dk.kb.avischk.dao.NewspaperQADaoFactory;
import dk.kb.avischk.dto.CharacterizationInfo;
import dk.kb.avischk.dto.NewspaperEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Path("/")
public class WebQAService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private NewspaperQADao dao;
    
    private DateFormat sdf = new SimpleDateFormat("YYYY-mm-DD");
    
    public WebQAService() {
        log.info("Initializing service");
        dao = NewspaperQADaoFactory.getInstance();
    }
    
    @GET
    @Path("getNewspaperIDs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewspaperIDs() {
        List<String> IDs;
        try {
            IDs = dao.getNewspaperIDs();
            return Response.ok(IDs, MediaType.APPLICATION_JSON).build();
        } catch (DAOFailureException e) {
            log.error("Could not get newspaper IDs from backend");
            ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity("Could not get newspaper IDs from backend");
            return builder.build();
        }
    }
    
    /*
    @GET
    @Path("dates/{ID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatesForNewspaper(@PathParam("ID") String ID) {
        if(ID == null) {
            log.error("ID not supplied in request");
            ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity("ID must be supplied");
            return builder.build();
        }
        
        List<Date> dates;
        try {
            dates = dao.getDatesForNewspaperID(ID);
            return Response.ok(dates, MediaType.APPLICATION_JSON).build();
        } catch (DAOFailureException e) {
            log.error("Could not get dates for newspaper ID {}", ID);
            ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity("Could not get dates from backend");
            return builder.build();
        }
    }*/
    
    @GET
    @Path("years/{ID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getYearsForNewspaper(@PathParam("ID") String ID) {
        if(ID == null) {
            log.error("ID not supplied in request");
            ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity("ID must be supplied");
            return builder.build();
        }
        
        List<String> years;
        try {
            years = dao.getYearsForNewspaperID(ID);
            return Response.ok(years, MediaType.APPLICATION_JSON).build();
        } catch (DAOFailureException e) {
            log.error("Could not get dates for newspaper ID {}", ID);
            ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity("Could not get dates from backend");
            return builder.build();
        }
    }
    
    @GET
    @Path("dates/{ID}/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatesForNewspaper(@PathParam("ID") String ID, @PathParam("year") String year) {
        if(ID == null) {
            log.error("ID not supplied in request");
            ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity("ID must be supplied");
            return builder.build();
        }
        if(year == null) {
            log.error("year not supplied in request");
            ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity("year must be supplied");
            return builder.build();
        }
        
        List<Date> dates;
        try {
            dates = dao.getDatesForNewspaperID(ID, year);
            return Response.ok(dates, MediaType.APPLICATION_JSON).build();
        } catch (DAOFailureException e) {
            log.error("Could not get dates for newspaper ID {}", ID);
            ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity("Could not get dates from backend");
            return builder.build();
        }
    }
    /*
    @GET
    @Path("dates/{ID}/{date}/entities")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEntitiesForDate(@PathParam("ID") String ID, @PathParam("date") String date) {
        if(ID == null) {
            log.error("ID not supplied in request");
            ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity("ID must be supplied");
            return builder.build();
        }
        if(date == null) {
            log.error("Date not supplied in request");
            ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity("Date must be supplied");
            return builder.build();
        } else {
            try {
                Date d = sdf.parse(date);
            } catch (ParseException e1) {
                log.error("Date could not be parsed");
                ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
                builder.entity("Date must be in the format YYYY-mm-DD");
                return builder.build();
            }
        }
        
        List<NewspaperEntity> entities;
        try {
            entities = dao.getEditionsForNewspaperOnDate(ID, date);
            return Response.ok(entities, MediaType.APPLICATION_JSON).build();
        } catch (DAOFailureException e) {
            log.error("Could not get entities for date {} for newspaper ID {}", date, ID);
            ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity("Could not get entities from backend");
            return builder.build();
        }
    }
    */
    @GET
    @Path("dates/{ID}/{date}/mappedEntities")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappedEntitiesForDate(@PathParam("ID") String ID, @PathParam("date") String date) {
        if(ID == null) {
            log.error("ID not supplied in request");
            ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity("ID must be supplied");
            return builder.build();
        }
        if(date == null) {
            log.error("Date not supplied in request");
            ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity("Date must be supplied");
            return builder.build();
        } else {
            try {
                Date d = sdf.parse(date);
            } catch (ParseException e1) {
                log.error("Date could not be parsed");
                ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
                builder.entity("Date must be in the format YYYY-mm-DD");
                return builder.build();
            }
        }
        
        Map<String, List<NewspaperEntity>> entities;
        try {
            entities = dao.getMappedEditionsForNewspaperOnDate(ID, date);
            return Response.ok(entities, MediaType.APPLICATION_JSON).build();
        } catch (DAOFailureException e) {
            log.error("Could not get entities for date {} for newspaper ID {}", date, ID);
            ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity("Could not get entities from backend");
            return builder.build();
        }
    }
    
    @GET
    @Path("entity/{handle}/url/{type}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getEntityUrl(@PathParam("handle") String handle, @PathParam("type") String type) {
        log.info("Looking up relative path for entity {}", handle);
        long parsedHandle = Long.parseLong(handle);
        String relPath = null;
        try {
            relPath = dao.getOrigRelPath(parsedHandle);
            String url = ContentLocationResolver.getContent(relPath, type);
            return Response.ok(url, MediaType.APPLICATION_JSON).build();
        } catch (DAOFailureException e) {
            log.error("Could not get relative path for handle {} (parsed: {})", handle, parsedHandle);
            ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity("Could not find handle in backend");
            return builder.build();
        } catch (FileNotFoundException e) {
            log.error("Could not find file {} in filesystem", relPath);
            ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity("Could not find file");
            return builder.build();
        }
    }
    
    @GET
    @Path("entity/{handle}/characterization")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEntityCharacterisation(@PathParam("handle") String handle) {
        long parsedHandle = Long.parseLong(handle);
        
        List<CharacterizationInfo> characterisations;
        try {
            characterisations = dao.getCharacterizationForEntity(parsedHandle);
            return Response.ok(characterisations, MediaType.APPLICATION_JSON).build();
        } catch (DAOFailureException e) {
            log.error("Could not get characterisation for newspaper with handle {}", handle);
            ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity("Could not get dates from backend");
            return builder.build();
        }
    }
}
