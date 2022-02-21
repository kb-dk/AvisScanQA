package dk.kb.kula190.dao;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import dk.kb.kula190.model.Batch;
import dk.kb.kula190.model.CharacterizationInfo;
import dk.kb.kula190.model.NewspaperDate;
import dk.kb.kula190.model.NewspaperEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class NewspaperQADao {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final ComboPooledDataSource connectionPool;
    
    public NewspaperQADao(ComboPooledDataSource connectionPool) {
        this.connectionPool = connectionPool;
    }
    
    public List<String> getNewspaperIDs() throws DAOFailureException {
        log.debug("Looking up newspaper ids");
        String SQL = "SELECT distinct(avisid) FROM newspaperarchive";
        
        try (Connection conn = connectionPool.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            try (ResultSet res = ps.executeQuery()) {
                List<String> list = new ArrayList<>();
                while (res.next()) {
                    list.add(res.getString(1));
                }
                return list;
            }
        } catch (SQLException e) {
            log.error("Failed to lookup newspaper ids", e);
            throw new DAOFailureException("Err looking up newspaper ids", e);
        }
    }
    
    public List<Batch> getBatchIDs() throws DAOFailureException {
        log.debug("Looking up batch ids");
        String
                SQL
                = "SELECT batchid, avisid, roundtrip, start_date, end_date, delivery_date, problems, state FROM batch";
        
        try (Connection conn = connectionPool.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            try (ResultSet res = ps.executeQuery()) {
                List<Batch> list = new ArrayList<>();
                while (res.next()) {
                    Batch batch = new Batch();
                    batch.setBatchid(res.getString("batchid"));
                    batch.setAvisid(res.getString("avisid"));
                    batch.setRoundtrip(res.getInt("roundtrip"));
                    batch.setStartDate(res.getDate("start_date").toLocalDate());
                    batch.setEndDate(res.getDate("end_date").toLocalDate());
                    batch.setDeliveryDate(res.getDate("delivery_date").toLocalDate());
                    batch.setProblems(res.getString("problems"));
                    batch.setState(res.getString("state"));
                    list.add(batch);
                }
                return list;
            }
        } catch (SQLException e) {
            log.error("Failed to lookup batch ids", e);
            throw new DAOFailureException("Err looking up batch ids", e);
        }
    }
    
    
    public List<String> getYearsForNewspaperID(String id) throws DAOFailureException {
        log.debug("Looking up dates for newspaper id: '{}'", id);
        String
                SQL
                = "SELECT distinct(EXTRACT(YEAR from edition_date)) AS year FROM newspaperarchive WHERE avisid = ? ORDER BY year ASC";
        
        try (Connection conn = connectionPool.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            
            ps.setString(1, id);
            try (ResultSet res = ps.executeQuery()) {
                List<String> list = new ArrayList<>();
                
                while (res.next()) {
                    list.add("" + res.getInt(1));
                }
                return list;
            }
        } catch (SQLException e) {
            log.error("Failed to lookup edition dates for newspaper id {}", id, e);
            throw new DAOFailureException("Err looking up dates for newspaper id", e);
        }
    }
    
    public List<NewspaperDate> getDatesForNewspaperID(String id, String year) throws DAOFailureException {
        log.debug("Looking up dates for newspaper id: '{}', in year {}", id, year);
        
        
        Map<LocalDate, NewspaperDate> resultMap = new HashMap<>();
        String
                SQL1
                = "select start_date, end_date  from batch where avisid = ? and ( EXTRACT(YEAR FROM start_date) = ? or EXTRACT(YEAR FROM end_date) = ? ) ";
        
        try (Connection conn = connectionPool.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL1)) {
            ps.setString(1, id);
            ps.setInt(2, Integer.parseInt(year));
            ps.setInt(3, Integer.parseInt(year));
            
            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    LocalDate startDate = res.getDate("start_date").toLocalDate();
                    LocalDate endDate = res.getDate("end_date").toLocalDate();
                    LongStream.rangeClosed(0, ChronoUnit.DAYS.between(startDate, endDate))
                              .mapToObj(startDate::plusDays)
                              .forEach(date -> {
                                  final NewspaperDate newspaperDate = new NewspaperDate();
                                  newspaperDate.setDate(date);
                                  newspaperDate.setPageCount(0);
                                  newspaperDate.setProblems("");
                                  newspaperDate.setEditionCount(0);
                                  resultMap.put(date, newspaperDate);
                              });
                }
            }
        } catch (SQLException e) {
            log.error("Failed to lookup edition dates for newspaper id {}", id, e);
            throw new DAOFailureException("Err looking up dates for newspaper id", e);
        }
        
        
        String SQL =
                "select edition_date, count(DISTINCT(edition_title)) as numEditions, count(*) as numPages, string_agg(problems, '\\n') as allProblems "
                + " from newspaperarchive "
                + " where avisid = ? and EXTRACT(YEAR FROM edition_date) = ? group by edition_date";
        
        try (Connection conn = connectionPool.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            
            ps.setString(1, id);
            ps.setInt(2, Integer.parseInt(year));
            //ps.setString(3, year);
            try (ResultSet res = ps.executeQuery()) {
                
                while (res.next()) {
                    
                    java.sql.Date date = res.getDate("edition_date");
                    int editionCount = res.getInt("numEditions");
                    int pageCount = res.getInt("numPages");
                    String problems = res.getString("allProblems").translateEscapes().trim();
                    
                    NewspaperDate result = new NewspaperDate();
                    final LocalDate localDate = date.toLocalDate();
                    result.setDate(localDate);
                    result.setPageCount(pageCount);
                    result.setEditionCount(editionCount);
                    result.setProblems(problems);
                    
                    resultMap.put(localDate, result);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to lookup edition dates for newspaper id {}", id, e);
            throw new DAOFailureException("Err looking up dates for newspaper id", e);
        }
        return resultMap.values()
                        .stream()
                        .sorted(Comparator.comparing(NewspaperDate::getDate))
                        .collect(Collectors.toList());
    }
    
    public List<NewspaperEntity> getEditionsForNewspaperOnDate(String id, String date) throws DAOFailureException {
        log.debug("Looking up dates for newspaper id: '{}' on date '{}'", id, date);
        String SQL = "SELECT * FROM newspaperarchive WHERE avisid = ? AND edition_date = ?"
                     + " ORDER BY section_title, page_number ASC";
        
        try (Connection conn = connectionPool.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            
            ps.setString(1, id);
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet res = ps.executeQuery()) {
                List<NewspaperEntity> list = new ArrayList<>();
                
                while (res.next()) {
                    NewspaperEntity entity = new NewspaperEntity();
                    entity.setOrigRelpath(res.getString("orig_relpath"));
                    entity.setFormatType(res.getString("format_type"));
                    entity.setEditionDate(res.getDate("edition_date").toLocalDate());
                    entity.setSinglePage(res.getBoolean("single_page"));
                    entity.setPageNumber(res.getInt("page_number"));
                    entity.setAvisid(res.getString("avisid"));
                    entity.setAvistitle(res.getString("avistitle"));
                    entity.setShadowPath(res.getString("shadow_path"));
                    entity.setSectionTitle(res.getString("section_title"));
                    entity.setEditionTitle(res.getString("edition_title"));
                    entity.setDeliveryDate(res.getDate("delivery_date").toLocalDate());
                    entity.setHandle(res.getLong("handle"));
                    entity.setFraktur(res.getBoolean("fraktur"));
                    entity.setProblems(res.getString("problems"));
                    list.add(entity);
                }
                return list;
            }
        } catch (SQLException e) {
            log.error("Failed to lookup edition dates for newspaper id {}", id, e);
            throw new DAOFailureException("Err looking up dates for newspaper id", e);
        }
    }
    
    public Map<String, List<NewspaperEntity>> getMappedEditionsForNewspaperOnDate(String id, String date)
            throws DAOFailureException {
        List<NewspaperEntity> entities = getEditionsForNewspaperOnDate(id, date);
        
        Map<String, List<NewspaperEntity>> map = new HashMap<>();
        for (NewspaperEntity entity : entities) {
            List<NewspaperEntity> currentEntities = map.get(entity.getEditionTitle());
            if (currentEntities == null) {
                currentEntities = new ArrayList<>();
            }
            currentEntities.add(entity);
            map.put(entity.getEditionTitle(), currentEntities);
        }
        
        return map;
    }
    
    public String getOrigRelPath(long handle) throws DAOFailureException {
        log.debug("Looking up relpath by handle for handle {}", handle);
        String SQL = "SELECT orig_relpath FROM newspaperarchive WHERE handle = ?";
        
        try (Connection conn = connectionPool.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            
            ps.setLong(1, handle);
            try (ResultSet res = ps.executeQuery()) {
                String path = "";
                
                while (res.next()) {
                    path = res.getString(1);
                }
                return path;
            }
        } catch (SQLException e) {
            log.error("Failed to lookup relpath handle {}", handle, e);
            throw new DAOFailureException("Err looking up relpath for handle", e);
        }
    }
    
    public List<CharacterizationInfo> getCharacterizationForEntity(long handle) throws DAOFailureException {
        log.debug("Looking up characterization for newspaper handle: '{}'", handle);
        String origRelpath = getOrigRelPath(handle);
        String SQL = "SELECT * FROM characterisation_info WHERE orig_relpath = ?";
        
        try (Connection conn = connectionPool.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            
            ps.setString(1, origRelpath);
            try (ResultSet res = ps.executeQuery()) {
                List<CharacterizationInfo> list = new ArrayList<>();
                
                while (res.next()) {
                    CharacterizationInfo info = new CharacterizationInfo();
                    info.setOrigRelpath(res.getString("orig_relpath"));
                    info.setTool(res.getString("tool"));
                    info.setCharacterisationDate(res.getDate("characterisation_date").toLocalDate());
                    info.setToolOutput(res.getString("tool_output"));
                    info.setStatus(res.getString("status"));
                    list.add(info);
                }
                return list;
            }
        } catch (SQLException e) {
            log.error("Failed to lookup characterization info for newspaper entity {}", origRelpath, e);
            throw new DAOFailureException("Err looking up characterization info for newspaper entity", e);
        }
    }
    
}
