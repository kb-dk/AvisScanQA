package dk.kb.kula190.dao;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import dk.kb.kula190.model.Batch;
import dk.kb.kula190.model.CharacterizationInfo;
import dk.kb.kula190.model.NewspaperDate;
import dk.kb.kula190.model.NewspaperDay;
import dk.kb.kula190.model.NewspaperEdition;
import dk.kb.kula190.model.NewspaperEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class NewspaperQADao {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final ComboPooledDataSource connectionPool;
    
    public NewspaperQADao(ComboPooledDataSource connectionPool) {
        this.connectionPool = connectionPool;
    }
    
    
    public void setNotes(@Nonnull String batchID,
                         @Nullable LocalDate date,
                         @Nonnull String notes,
                         @Nullable String avis,
                         @Nullable String edition,
                         @Nullable String section,
                         @Nullable Integer page) throws DAOFailureException {
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO notes(batchid, avisid, edition_date, edition_title, section_title, page_number, notes) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (batchid, avisid, edition_date, edition_title, section_title, page_number) DO UPDATE SET notes=excluded.notes")) {
            int param = 1;
            ps.setString(param++, batchID);
            param = setNullable(ps, param, avis);
            param = setNullable(ps, param, date);
            param = setNullable(ps, param, edition);
            param = setNullable(ps, param, section);
            param = setNullable(ps, param, page);
            
            ps.setString(param++, notes);
            
            
            ps.execute();
            //            conn.commit();
        } catch (SQLException e) {
            log.error("Failed to lookup newspaper ids", e);
            throw new DAOFailureException("Err looking up newspaper ids", e);
        }
    }
    
    private int setNullable(PreparedStatement ps, int param, String value) throws SQLException {
        if (value == null) {
            ps.setNull(param++, Types.VARCHAR);
        } else {
            ps.setString(param++, value);
        }
        return param;
    }
    
    private int setNullable(PreparedStatement ps, int param, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(param++, Types.INTEGER);
        } else {
            ps.setInt(param++, value);
        }
        return param;
    }
    
    private int setNullable(PreparedStatement ps, int param, LocalDate value) throws SQLException {
        if (value == null) {
            ps.setNull(param++, Types.DATE);
        } else {
            ps.setDate(param++, Date.valueOf(value));
        }
        return param;
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
                = "SELECT batch.batchid, batch.avisid, roundtrip, start_date, end_date, delivery_date, problems, state, n.notes as notes FROM batch left join notes n on batch.batchid = n.batchid and batch.avisid = n.avisid and n.edition_date is null and n.edition_title is null and n.section_title is null and n.page_number is null";
        
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
                    batch.setNotes(res.getString("notes"));
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
    
    public List<NewspaperDate> getDatesForNewspaperID(String avisID, String year) throws DAOFailureException {
        log.debug("Looking up dates for newspaper id: '{}', in year {}", avisID, year);
        
        
        Map<LocalDate, NewspaperDate> resultMap = new HashMap<>();
        String
                SQL1
                = "select batchid, avisid, roundtrip, start_date, end_date, delivery_date, problems, state  from batch where avisid = ? and ( EXTRACT(YEAR FROM start_date) = ? or EXTRACT(YEAR FROM end_date) = ? ) ";
        
        try (Connection conn = connectionPool.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL1)) {
            ps.setString(1, avisID);
            ps.setInt(2, Integer.parseInt(year));
            ps.setInt(3, Integer.parseInt(year));
            
            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    String batchID = res.getString("batchid");
                    Integer roundTrip = res.getInt("roundtrip");
                    LocalDate startDate = res.getDate("start_date").toLocalDate();
                    LocalDate endDate = res.getDate("end_date").toLocalDate();
                    String state = res.getString("state");
                    LongStream.rangeClosed(0, ChronoUnit.DAYS.between(startDate, endDate))
                              .mapToObj(startDate::plusDays)
                              .forEach(date -> {
                                  final NewspaperDate newspaperDate = new NewspaperDate();
                                  newspaperDate.setBatchid(batchID);
                                  newspaperDate.setAvisid(avisID);
                                  newspaperDate.setRoundtrip(roundTrip);
                                  newspaperDate.setDate(date);
                                  newspaperDate.setPageCount(0);
                                  newspaperDate.setProblems("");
                                  newspaperDate.setEditionCount(0);
                                  newspaperDate.setState(state);
                                  resultMap.put(date, newspaperDate);
                              });
                }
            }
        } catch (SQLException e) {
            log.error("Failed to lookup edition dates for newspaper id {}", avisID, e);
            throw new DAOFailureException("Err looking up dates for newspaper id", e);
        }
        
        String SQL =
                "select edition_date, "
                + " b.batchid, "
                + " b.state, "
                + " count(DISTINCT(edition_title)) as numEditions, "
                + " count(*) as numPages, "
                + " string_agg(newspaperarchive.problems, '\\n') as allProblems "
                + " from newspaperarchive "
                + " join batch b on b.batchid = newspaperarchive.batchid "
                + " where newspaperarchive.avisid = ? and EXTRACT(YEAR FROM edition_date) = ? "
                + " group by edition_date, b.batchid";
        
        try (Connection conn = connectionPool.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            
            ps.setString(1, avisID);
            ps.setInt(2, Integer.parseInt(year));
            //ps.setString(3, year);
            try (ResultSet res = ps.executeQuery()) {
                
                while (res.next()) {
                    
                    java.sql.Date date = res.getDate("edition_date");
                    int editionCount = res.getInt("numEditions");
                    int pageCount = res.getInt("numPages");
                    String problems = res.getString("allProblems").translateEscapes().trim();
                    String state = res.getString("state");
                    String batchID = res.getString("batchid");
                    
                    NewspaperDate result = new NewspaperDate();
                    final LocalDate localDate = date.toLocalDate();
                    result.setDate(localDate);
                    result.setPageCount(pageCount);
                    result.setEditionCount(editionCount);
                    result.setProblems(problems);
                    result.setBatchid(batchID);
                    result.setAvisid(avisID);
                    result.setState(state);
                    
                    resultMap.put(localDate, result);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to lookup edition dates for newspaper id {}", avisID, e);
            throw new DAOFailureException("Err looking up dates for newspaper id", e);
        }
        return resultMap.values()
                        .stream()
                        .sorted(Comparator.comparing(NewspaperDate::getDate))
                        .collect(Collectors.toList());
    }
    
    public List<NewspaperDate> getDatesForBatchID(String batchID, String yearString) throws DAOFailureException {
        
        Map<LocalDate, NewspaperDate> resultMap = new HashMap<>();
        
        final int year = Integer.parseInt(yearString);
        try (Connection conn = connectionPool.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "select start_date, end_date, state, avisid from batch where batchid = ?")) {
                ps.setString(1, batchID);
                
                try (ResultSet res = ps.executeQuery()) {
                    while (res.next()) {
                        LocalDate startDate = res.getDate("start_date").toLocalDate();
                        LocalDate endDate = res.getDate("end_date").toLocalDate();
                        String state = res.getString("state");
                        String avisID = res.getString("avisid");
                        LongStream.rangeClosed(0, ChronoUnit.DAYS.between(startDate, endDate))
                                  .mapToObj(startDate::plusDays)
                                  .filter(date -> date.getYear() == year) //We could also make limits on the range...
                                  .forEach(date -> {
                                      final NewspaperDate newspaperDate = new NewspaperDate();
                                      newspaperDate.setDate(date);
                                      newspaperDate.setPageCount(0);
                                      newspaperDate.setProblems("");
                                      newspaperDate.setEditionCount(0);
                                      newspaperDate.setBatchid(batchID);
                                      newspaperDate.setAvisid(avisID);
                                      newspaperDate.setState(state);
                                      resultMap.put(date, newspaperDate);
                                  });
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "select edition_date, "
                    + " b.state, "
                    + " count(DISTINCT(edition_title)) as numEditions, "
                    + " count(*) as numPages, "
                    + " string_agg(newspaperarchive.problems, '\\n') as allProblems "
                    + " from newspaperarchive "
                    + " join batch b on b.batchid = newspaperarchive.batchid "
                    + " where newspaperarchive.batchid = ? and "
                    + " EXTRACT(YEAR FROM edition_date) = ? "
                    + " group by edition_date, b.batchid ")) {
                
                ps.setString(1, batchID);
                ps.setInt(2, year);
                //ps.setString(3, year);
                try (ResultSet res = ps.executeQuery()) {
                    
                    while (res.next()) {
                        
                        java.sql.Date date = res.getDate("edition_date");
                        int editionCount = res.getInt("numEditions");
                        int pageCount = res.getInt("numPages");
                        String problems = res.getString("allProblems").translateEscapes().trim();
                        String state = res.getString("state");
                        
                        NewspaperDate result = new NewspaperDate();
                        final LocalDate localDate = date.toLocalDate();
                        result.setDate(localDate);
                        result.setPageCount(pageCount);
                        result.setEditionCount(editionCount);
                        result.setProblems(problems);
                        result.setBatchid(batchID);
                        result.setState(state);
                        result.setAvisid(resultMap.get(localDate).getAvisid());
                        resultMap.put(localDate, result);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Failed to lookup edition dates for batch id {}", batchID, e);
            throw new DAOFailureException("Err looking up dates for newspaper id", e);
        }
        return resultMap.values()
                        .stream()
                        .sorted(Comparator.comparing(NewspaperDate::getDate))
                        .collect(Collectors.toList());
    }
    
    public NewspaperDay getNewspaperEditions(String batchID, String newspaperID, LocalDate date)
            throws DAOFailureException {
        //        log.debug("Looking up dates for newspaper id: '{}' on date '{}'", id, date);
        
        NewspaperDay result = new NewspaperDay();
        result.setBatchid(batchID);
        result.setAvisid(newspaperID);
        result.setDate(date);
        
        
        try (Connection conn = connectionPool.getConnection()) {
            
            
            String dayNotes = getDayNotes(batchID, newspaperID, date, conn);
            result.setNotes(dayNotes);
            
            Map<String, NewspaperEdition> pages = getPages(batchID,
                                                           newspaperID,
                                                           date,
                                                           conn);
            
            Map<String, String> editionNotes = getEditionNotes(batchID, newspaperID, date, conn);
            pages.forEach((key, value) -> value.setNotes(editionNotes.get(key)));
            
            result.setEditions(new ArrayList<>(pages.values()));
            
            return result;
        } catch (SQLException e) {
            log.error("Failed to lookup edition dates for newspaper id {}", newspaperID, e);
            throw new DAOFailureException("Err looking up dates for newspaper id", e);
        }
    }
    
    private Map<String, String> getEditionNotes(String batchID, String newspaperID, LocalDate date, Connection
            conn)
            throws SQLException {
        Map<String, String> editionNotes = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT batchid, avisid, edition_date, edition_title, section_title, page_number, notes "
                + "from  notes "
                + "where batchid = ? and avisid = ? and edition_date = ? and section_title is null and page_number is null")) {
            int param = 1;
            ps.setString(param++, batchID);
            ps.setString(param++, newspaperID);
            ps.setDate(param++, Date.valueOf(date));
            
            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    
                    String editionNote = res.getString("notes");
                    
                    editionNotes.put(res.getString("edition_title"), editionNote);
                }
            }
        }
        return editionNotes;
    }
    
    private Map<String, NewspaperEdition> getPages(String batchID,
                                                   String newspaperID,
                                                   LocalDate date,
                                                   Connection conn) throws SQLException {
        Map<String, NewspaperEdition> editions = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT orig_relpath, format_type, p.edition_date, single_page, p.page_number, p.avisid, avistitle, shadow_path, p.section_title, p.edition_title, delivery_date, handle, side_label, fraktur, problems, p.batchid, notes "
                + " FROM newspaperarchive as p "
                + " left join notes as n on "
                + " p.batchid = n.batchid and "
                + " p.avisid = n.avisid and "
                + " p.edition_date = n.edition_date and "
                + " p.section_title = n.section_title and "
                + " p.edition_title = n.edition_title and "
                + " p.edition_date = n.edition_date and "
                + " p.page_number = n.page_number "
                + " WHERE p.batchid = ? AND p.avisid = ? AND p.edition_date = ?"
                + " ORDER BY p.section_title, p.page_number ASC")) {
            
            ps.setString(1, batchID);
            ps.setString(2, newspaperID);
            ps.setDate(3, Date.valueOf(date));
            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    String edition_title = res.getString("edition_title");
                    
                    NewspaperEdition edition = editions.getOrDefault(edition_title, new NewspaperEdition());
                    editions.put(edition_title, edition);
                    edition.setBatchid(batchID);
                    edition.setEdition(edition_title);
                    edition.setDate(date);
                    edition.setAvisid(newspaperID);
                    if (edition.getPages() == null) {
                        edition.setPages(new ArrayList<>());
                    }
                    
                    NewspaperEntity entity = new NewspaperEntity();
                    entity.setOrigRelpath(res.getString("orig_relpath"));
                    entity.setFormatType(res.getString("format_type"));
                    entity.setEditionDate(res.getDate("edition_date").toLocalDate());
                    entity.setSinglePage(res.getBoolean("single_page"));
                    entity.setPageNumber(res.getInt("page_number"));
                    entity.setBatchid(batchID);
                    entity.setAvisid(res.getString("avisid"));
                    entity.setAvistitle(res.getString("avistitle"));
                    entity.setShadowPath(res.getString("shadow_path"));
                    entity.setSectionTitle(res.getString("section_title"));
                    
                    entity.setEditionTitle(edition_title);
                    entity.setDeliveryDate(res.getDate("delivery_date").toLocalDate());
                    entity.setHandle(res.getLong("handle"));
                    entity.setFraktur(res.getBoolean("fraktur"));
                    entity.setProblems(res.getString("problems"));
                    entity.setNotes(res.getString("notes"));
                    edition.addPagesItem(entity);
                    
                }
                
            }
        }
        return editions;
    }
    
    private String getDayNotes(String batchID, String newspaperID, LocalDate date, Connection conn)
            throws SQLException {
        String dayNotes = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT notes "
                + "from  notes "
                + "where "
                + "batchid = ? and "
                + "avisid = ? and "
                + "edition_date = ? and "
                + " edition_title is null and "
                + "section_title is null and "
                + "page_number is null")) {
            int param = 1;
            ps.setString(param++, batchID);
            ps.setString(param++, newspaperID);
            ps.setDate(param++, Date.valueOf(date));
            
            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    dayNotes = res.getString("notes");
                    
                }
            }
        }
        return dayNotes;
    }
    
    public String getOrigRelPath(long handle) throws DAOFailureException {
        log.debug("Looking up relpath by handle for handle {}", handle);
        String SQL = "SELECT orig_relpath FROM newspaperarchive WHERE handle = ?";
        
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            
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
        
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            
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
