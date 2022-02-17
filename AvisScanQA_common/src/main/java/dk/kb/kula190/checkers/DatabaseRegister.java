package dk.kb.kula190.checkers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.Failure;
import dk.kb.kula190.generated.Reference;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DatabaseRegister extends DecoratedEventHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Driver jdbcDriver;
    private final String jdbcURL;
    private final String jdbcUser;
    private final String jdbcPassword;
    private final Map<Reference,Failure> registeredFailures;
    
    private BasicDataSource dataSource;
    
    public DatabaseRegister(ResultCollector resultCollector,
                            Driver jdbcDriver,
                            String jdbcURL,
                            String jdbcUser,
                            String jdbcPassword,
                            List<Failure> registeredFailures) {
        super(resultCollector);
        this.jdbcDriver   = jdbcDriver;
        this.jdbcURL      = jdbcURL;
        this.jdbcUser     = jdbcUser;
        this.jdbcPassword = jdbcPassword;
        this.registeredFailures = registeredFailures.stream().collect(Collectors.toMap(Failure::getReference, f->f));
    }
    
    @Override
    public void batchBegins(DecoratedNodeParsingEvent event,
                            String avis,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) {
        dataSource = new BasicDataSource();
        
        if (jdbcUser != null) {
            dataSource.setUsername(jdbcUser);
        }
        
        if (jdbcPassword != null) {
            dataSource.setPassword(jdbcPassword);
        }
        dataSource.setUrl(jdbcURL);
        
        dataSource.setDefaultReadOnly(false);
        dataSource.setDefaultAutoCommit(false);
        
        dataSource.setRemoveAbandonedTimeout(60); // 60 sec
        dataSource.setMaxWaitMillis(60000); // 1 min
        dataSource.setMaxTotal(2); // Change to 10 when running as WAR
        
        dataSource.setDriver(jdbcDriver);
    }
    
    @Override
    public void batchEnds(DecoratedNodeParsingEvent event,
                          String avis,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) {
        try {
            if (dataSource != null) {
                dataSource.close();
            }
        } catch (Exception e) {
            // ignore errors during shutdown, we cant do anything about it anyway
            log.error("shutdown failed", e);
        }
    }
    
    private boolean matchThisPage(Reference reference, DecoratedNodeParsingEvent event){
        return Objects.equals(event.getAvis(), reference.getAvis()) &&
               Objects.equals(event.getEditionDate(), reference.getEditionDate()) &&
               Objects.equals(event.getUdgave(), reference.getUdgave()) &&
               Objects.equals(event.getSectionName(), reference.getSectionName())
                //TODO
                ;
        
    }
    
    private boolean matchThisPage(Reference reference, DecoratedAttributeParsingEvent event){
    return false;
    }
    
    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        
        
        try (Connection connection = dataSource.getConnection()) {
            
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO newspaperarchive(orig_relpath, format_type, edition_date, single_page, page_number, avisid, avistitle, shadow_path, section_title, edition_title, delivery_date, side_label, fraktur) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) "
                    + "ON CONFLICT DO NOTHING ")) {
                int param = 1;
                //orig_relpath
                preparedStatement.setString(param++, event.getLocation().substring(event.getLocation().indexOf(avis)));
                //format_type
                preparedStatement.setString(param++, "tiff");
                //edition_date
                preparedStatement.setDate(param++, Date.valueOf(editionDate));
                //single_page
                preparedStatement.setBoolean(param++, true);
                //page_numer
                preparedStatement.setInt(param++, pageNumber);
                //avis_id
                preparedStatement.setString(param++, avis);
                //avis_title,
                preparedStatement.setString(param++, CaseUtils.toCamelCase(avis,true ));
                //shadow_path
                preparedStatement.setString(param++, event.getName());
                //section_title
                preparedStatement.setString(param++, sectionName);
                //edition_title
                preparedStatement.setString(param++, udgave);
                //delivery_date
                preparedStatement.setDate(param++, new Date(new File(event.getLocation()).lastModified()));
                //side_label
                preparedStatement.setString(param++, "");
                //fraktur
                preparedStatement.setBoolean(param++, true);
                
                boolean result = preparedStatement.execute();
            }
            connection.commit();
            
        } catch (SQLException e) {
            //TODO
            throw new IOException(e);
        }
    }
}
