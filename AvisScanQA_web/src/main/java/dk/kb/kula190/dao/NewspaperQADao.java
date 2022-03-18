package dk.kb.kula190.dao;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import dk.kb.kula190.model.Batch;
import dk.kb.kula190.model.CharacterizationInfo;
import dk.kb.kula190.model.NewspaperDate;
import dk.kb.kula190.model.NewspaperDay;
import dk.kb.kula190.model.NewspaperEdition;
import dk.kb.kula190.model.NewspaperPage;
import dk.kb.kula190.model.Note;
import dk.kb.kula190.model.SlimBatch;
import dk.kb.kula190.webservice.exception.NotFoundServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NewspaperQADao {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final ComboPooledDataSource connectionPool;
    
    public NewspaperQADao(ComboPooledDataSource connectionPool) {
        this.connectionPool = connectionPool;
    }
    
    
    public List<Note> getNotes(@Nonnull String batchID) throws DAOFailureException {
        try (Connection conn = connectionPool.getConnection()) {
            return DaoNoteHelper.getAllNotes(batchID, conn);
        } catch (SQLException e) {
            log.error("Failed to retrieve notes from {}", batchID, e);
            throw new DAOFailureException("Failed to retrieve notes from " + batchID, e);
        }
        
    }
    
    public void setNotes(@Nonnull String batchID,
                         @Nullable LocalDate date,
                         @Nonnull String notes,
                         @Nullable String avis,
                         @Nullable String edition,
                         @Nullable String section,
                         @Nullable Integer page,
                         @Nonnull String username) throws DAOFailureException {
        try (Connection conn = connectionPool.getConnection()) {
            
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO notes(batchid, avisid, edition_date, edition_title, section_title, page_number, "
                    + "username, notes,created) "
                    + "VALUES (?, ?, ?, ?, ?, ?,?,?,now()) ON CONFLICT (id) DO UPDATE SET notes=excluded.notes")) {
                int param = 1;
                ps.setString(param++, batchID);
                param = DaoUtils.setNullable(ps, param, avis);
                param = DaoUtils.setNullable(ps, param, date);
                param = DaoUtils.setNullable(ps, param, edition);
                param = DaoUtils.setNullable(ps, param, section);
                param = DaoUtils.setNullable(ps, param, page);
                ps.setString(param++, username);
                ps.setString(param++, notes);
                
                
                ps.execute();
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
            }
        } catch (SQLException e) {
            log.error("Failed to lookup newspaper ids", e);
            throw new DAOFailureException("Err looking up newspaper ids", e);
        }
    }
    
    public List<Note> getNewspaperNotes(@Nonnull String avisID) throws DAOFailureException {
        try (Connection conn = connectionPool.getConnection()) {
            return DaoNoteHelper.getNewspaperLevelNotes(avisID, conn);
        } catch (SQLException e) {
            log.error("Failed to retrieve notes from {}", avisID, e);
            throw new DAOFailureException("Failed to retrieve notes from " + avisID, e);
        }
        
    }
    
    public void setNewspaperNotes(@Nonnull String avis,
                                  @Nullable LocalDate date,
                                  @Nonnull String notes,
                                  @Nullable String batchID,
                                  @Nullable String edition,
                                  @Nullable String section,
                                  @Nullable Integer page,
                                  @Nonnull String username) throws DAOFailureException {
        try (Connection conn = connectionPool.getConnection()) {
            
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO notes(batchid, avisid, edition_date, edition_title, section_title, page_number, "
                    + "username, notes,created) "
                    + "VALUES (?, ?, ?, ?, ?, ?,?,?,now()) ON CONFLICT (id) DO UPDATE SET notes=excluded.notes")) {
                int param = 1;
                param = DaoUtils.setNullable(ps, param, batchID);
                ps.setString(param++, avis);
                param = DaoUtils.setNullable(ps, param, date);
                param = DaoUtils.setNullable(ps, param, edition);
                param = DaoUtils.setNullable(ps, param, section);
                param = DaoUtils.setNullable(ps, param, page);
                ps.setString(param++, username);
                ps.setString(param++, notes);
                
                
                ps.execute();
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
            }
        } catch (SQLException e) {
            log.error("Failed to lookup newspaper ids", e);
            throw new DAOFailureException("Err looking up newspaper ids", e);
        }
    }
    
    public void setState(@Nonnull String batchID, @Nullable String state, @Nonnull String username)
            throws DAOFailureException {
        try (Connection conn = connectionPool.getConnection()) {
            DaoBatchHelper.setBatchState(batchID, state, username, conn);
        } catch (SQLException e) {
            log.error("Failed to lookup batch ID", e);
            throw new DAOFailureException("Err looking up batch ID", e);
        }
    }
    
    public List<String> getNewspaperIDs() throws DAOFailureException {
        log.debug("Looking up newspaper ids");
        String SQL = "SELECT distinct(avisid) FROM newspaperarchive";
        
        try (Connection conn = connectionPool.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(SQL)) {
                try (ResultSet res = ps.executeQuery()) {
                    List<String> list = new ArrayList<>();
                    while (res.next()) {
                        list.add(res.getString(1));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            log.error("Failed to lookup newspaper ids", e);
            throw new DAOFailureException("Err looking up newspaper ids", e);
        }
    }
    
    
    public Batch getBatch(String batchID) throws DAOFailureException {
        try (Connection conn = connectionPool.getConnection()) {
            return Optional.ofNullable(DaoBatchHelper.getLatestBatch(batchID, conn))
                           .orElseThrow(() -> new NotFoundServiceException("Batch " + batchID + " not found"));
        } catch (SQLException e) {
            log.error("Failed to lookup batch ids", e);
            throw new NotFoundServiceException("Err looking up batch id " + batchID, e);
        }
    }
    
    
    public List<SlimBatch> getBatchIDs() throws DAOFailureException {
        log.debug("Looking up batch ids");
        
        try (Connection conn = connectionPool.getConnection()) {
            return DaoBatchHelper.getAllBatches(conn);
        } catch (SQLException e) {
            log.error("Failed to lookup batch ids", e);
            throw new DAOFailureException("Err looking up batch ids", e);
        }
    }
    
    
    public List<String> getYearsForNewspaperID(String id) throws DAOFailureException {
        log.debug("Looking up dates for newspaper id: '{}'", id);
        
        try (Connection conn = connectionPool.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT distinct(EXTRACT(YEAR from edition_date)) AS year FROM newspaperarchive WHERE avisid = ? " +
                    "ORDER BY year ASC")) {
                
                ps.setString(1, id);
                try (ResultSet res = ps.executeQuery()) {
                    List<String> list = new ArrayList<>();
                    
                    while (res.next()) {
                        list.add("" + res.getInt(1));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            log.error("Failed to lookup edition dates for newspaper id {}", id, e);
            throw new DAOFailureException("Err looking up dates for newspaper id", e);
        }
    }
    
    public List<NewspaperDate> getDatesForNewspaperID(String avisID, String year) throws DAOFailureException {
        log.debug("Looking up dates for newspaper id: '{}', in year {}", avisID, year);
        
        Map<LocalDate, NewspaperDate> resultMap = new HashMap<>();
        
        try (Connection conn = connectionPool.getConnection()) {
            List<SlimBatch> batches = DaoBatchHelper.getLatestBatches(avisID, conn);
            for (SlimBatch batch : batches) {
                DaoBatchHelper.batchDays(batch).forEach(date -> {
                    final NewspaperDate newspaperDate = new NewspaperDate().date(date)
                                                                           .pageCount(0)
                                                                           .problems("")
                                                                           .editionCount(0)
                                                                           .batchid(batch.getBatchid())
                                                                           .avisid(batch.getAvisid())
                                                                           .roundtrip(batch.getRoundtrip())
                                                                           .state(batch.getState());
                    resultMap.put(date, newspaperDate);
                    
                });
            }
            
            
            try (PreparedStatement ps = conn.prepareStatement("select edition_date, "
                                                              +
                                                              "         batchid, "
                                                              +
                                                              "         count(DISTINCT(edition_title)) as numEditions, "
                                                              +
                                                              "         count(*) as numPages, "
                                                              +
                                                              "         string_agg(newspaperarchive.problems, '\\n') " +
                                                              "as allProblems "
                                                              +
                                                              " from newspaperarchive "
                                                              +
                                                              " where newspaperarchive.avisid = ? and "
                                                              +
                                                              "       EXTRACT(YEAR FROM edition_date) = ? "
                                                              +
                                                              " group by edition_date, batchid ")) {
                //ascending sort ensures that the highest roundtrips are last
                //last entry for a given date wins. So this way, the calender will show the latest roundtrips
                
                ps.setString(1, avisID);
                ps.setInt(2, Integer.parseInt(year));
                //ps.setString(3, year);
                try (ResultSet res = ps.executeQuery()) {
                    
                    while (res.next()) {
                        
                        Date date = res.getDate("edition_date");
                        String batchID = res.getString("batchid");
                        
                        int editionCount = res.getInt("numEditions");
                        int pageCount = res.getInt("numPages");
                        String problems = res.getString("allProblems").translateEscapes().trim();
                        Optional<SlimBatch> batch = batches.stream()
                                                           .filter(b -> b.getBatchid().equals(batchID))
                                                           .limit(1)
                                                           .findFirst();
                        final LocalDate localDate = date.toLocalDate();
                        
                        NewspaperDate result = new NewspaperDate().date(localDate)
                                                                  .pageCount(pageCount)
                                                                  .editionCount(editionCount)
                                                                  .problems(problems)
                                                                  .batchid(batchID)
                                                                  .avisid(avisID);
                        batch.ifPresent(val -> result.setAvisid(val.getAvisid()));
                        batch.ifPresent(val -> result.setBatchid(val.getBatchid()));
                        batch.ifPresent(val -> result.setState(val.getState()));
                        batch.ifPresent(val -> result.setRoundtrip(val.getRoundtrip()));
                        resultMap.put(localDate, result);
                    }
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
            SlimBatch batch = DaoBatchHelper.getLatestSlimBatch(batchID, conn);
            
            DaoBatchHelper.batchDays(batch)
                          .filter(date -> date.getYear() == year) //We could also make limits on the range...
                          .forEach(date -> {
                              final NewspaperDate newspaperDate = new NewspaperDate().date(date)
                                                                                     .pageCount(0)
                                                                                     .problems("")
                                                                                     .editionCount(0)
                                                                                     .batchid(batchID)
                                                                                     .avisid(batch.getAvisid())
                                                                                     .state(batch.getState());
                              resultMap.put(date, newspaperDate);
                          });
            
            
            try (PreparedStatement ps = conn.prepareStatement(
                    "select edition_date, count(DISTINCT(edition_title)) as numEditions,  count(*) as numPages,  " +
                    "string_agg(newspaperarchive.problems, '\\n') as allProblems "
                    + " from newspaperarchive "
                    + " join batch b on b.batchid = newspaperarchive.batchid and b.lastmodified = ? "
                    + " where newspaperarchive.batchid = ? and "
                    + " EXTRACT(YEAR FROM edition_date) = ? "
                    + " group by edition_date ")) {
                int param = 1;
                ps.setTimestamp(param++, Timestamp.from(batch.getLastModified().toInstant()));
                ps.setString(param++, batchID);
                ps.setInt(param++, year);
                //ps.setString(3, year);
                try (ResultSet res = ps.executeQuery()) {
                    
                    while (res.next()) {
                        
                        java.sql.Date date = res.getDate("edition_date");
                        int editionCount = res.getInt("numEditions");
                        int pageCount = res.getInt("numPages");
                        String problems = res.getString("allProblems").translateEscapes().trim();
                        
                        final LocalDate localDate = date.toLocalDate();
                        NewspaperDate result = new NewspaperDate().date(localDate)
                                                                  .pageCount(pageCount)
                                                                  .editionCount(editionCount)
                                                                  .problems(problems)
                                                                  .batchid(batchID)
                                                                  .state(batch.getState())
                                                                  .avisid(resultMap.get(localDate).getAvisid());
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
    
    public NewspaperDay getNewspaperEditions(String batchID, String newspaperID, LocalDate date, String batchesFolder)
            throws DAOFailureException {
        
        try (Connection conn = connectionPool.getConnection()) {
            
            SlimBatch result1;
            try {
                result1 = Optional.ofNullable(DaoBatchHelper.getLatestSlimBatch(batchID, conn))
                                  .orElseThrow(() -> new NotFoundServiceException("Batch " + batchID + " not found"));
            } catch (SQLException e) {
                log.error("Failed to lookup batch ids", e);
                throw new NotFoundServiceException("Err looking up batch id " + batchID, e);
            }
            SlimBatch batch = result1;
            if (date.isBefore(batch.getStartDate()) || date.isAfter(batch.getEndDate())) {
                throw new NotFoundServiceException("Date "
                                                   + date
                                                   + " is not within start ("
                                                   + batch.getStartDate()
                                                   + ") "
                                                   + "and end ("
                                                   + batch.getEndDate()
                                                   + ") of batch "
                                                   + batch.getBatchid());
            }
            
            NewspaperDay result = new NewspaperDay().batch(batch).date(date);
            
            
            List<Note> dayNotes = DaoNoteHelper.getDayLevelNotes(batchID, newspaperID, date, conn);
            result.setNotes(dayNotes);
            
            Map<String, NewspaperEdition> editions = getPages(batchID, newspaperID, date, conn, batchesFolder);
            
            Map<String, List<Note>> editionNotes = DaoNoteHelper.getEditionLevelNotes(batchID, newspaperID, date, conn);
            editions.forEach((key, value) -> value.setNotes(editionNotes.getOrDefault(key, new ArrayList<>())));
            
            List<String>
                    editionTitles =
                    editions.values().stream().map(NewspaperEdition::getEdition).distinct().toList();
            for (String editionTitle : editionTitles) {
                NewspaperEdition edition = editions.get(editionTitle);
                Map<Integer, List<Note>> pageNotes = DaoNoteHelper.getPageLevelNotes(batchID,
                                                                                     newspaperID,
                                                                                     date,
                                                                                     editionTitle,
                                                                                     conn);
                edition.getPages()
                       .forEach(page -> page.setNotes(pageNotes.getOrDefault(page.getPageNumber(), new ArrayList<>())));
            }
            
            result.setEditions(new ArrayList<>(editions.values()));
            
            return result;
        } catch (SQLException e) {
            log.error("Failed to lookup edition dates for newspaper id {}", newspaperID, e);
            throw new DAOFailureException("Err looking up dates for newspaper id", e);
        }
    }
    
    private Map<String, NewspaperEdition> getPages(String batchID,
                                                   String newspaperID,
                                                   LocalDate date,
                                                   Connection conn,
                                                   String batchesFolder) throws SQLException {
        Map<String, NewspaperEdition> editions = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT orig_relpath, format_type, p.edition_date, single_page, p.page_number, p.avisid, avistitle, " +
                "shadow_path, p.section_title, p.edition_title, delivery_date, handle, side_label, fraktur, problems," +
                " p.batchid"
                + " FROM newspaperarchive as p "
                + " WHERE p.batchid = ? AND p.avisid = ? AND p.edition_date = ?"
                + " ORDER BY p.section_title, p.page_number ASC")) {
            
            ps.setString(1, batchID);
            ps.setString(2, newspaperID);
            ps.setDate(3, Date.valueOf(date));
            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    String edition_title = res.getString("edition_title");
                    
                    
                    editions.put(edition_title, editions.getOrDefault(edition_title, new NewspaperEdition()));
                    NewspaperEdition edition = editions.get(edition_title)
                                                       .batchid(batchID)
                                                       .edition(edition_title)
                                                       .date(date)
                                                       .avisid(newspaperID);
                    if (edition.getPages() == null) {
                        edition.setPages(new ArrayList<>());
                    }
                    
                    final String orig_relpath = res.getString("orig_relpath");
                    String avisID = res.getString("avisid");
                    NewspaperPage page = new NewspaperPage().origRelpath(orig_relpath)
                                                            .shadowPath(res.getString("shadow_path"))
                                                            .origFullPath((
                                                                                  batchesFolder +
                                                                                  orig_relpath).replaceAll(
                                                                    Pattern.quote("/"),
                                                                    "\\\\"))
                                                            .formatType(res.getString("format_type"))
                                                            .batchid(batchID)
                                                            .avisid(avisID)
                                                            .avistitle(res.getString("avistitle"))
                                                            .editionDate(res.getDate("edition_date").toLocalDate())
                                                            .editionTitle(edition_title)
                                                            .sectionTitle(res.getString("section_title"))
                                                            .pageNumber(res.getInt("page_number"))
                                                            .deliveryDate(res.getDate("delivery_date").toLocalDate())
                                                            .handle(res.getLong("handle"))
                                                            .singlePage(res.getBoolean("single_page"))
                                                            .fraktur(res.getBoolean("fraktur"))
                                                            .problems(res.getString("problems"))
                                                            .notes(DaoNoteHelper.getNewspaperLevelNotes(avisID, conn));
                    edition.addPagesItem(page);
                    
                }
                
            }
        }
        return editions;
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
                    list.add(new CharacterizationInfo().origRelpath(res.getString("orig_relpath"))
                                                       .tool(res.getString("tool"))
                                                       .characterisationDate(res.getDate("characterisation_date")
                                                                                .toLocalDate())
                                                       .toolOutput(res.getString("tool_output"))
                                                       .status(res.getString("status")));
                }
                return list;
            }
        } catch (SQLException e) {
            log.error("Failed to lookup characterization info for newspaper entity {}", origRelpath, e);
            throw new DAOFailureException("Err looking up characterization info for newspaper entity", e);
        }
    }
    
    public void removeNotes(Integer id) throws DAOFailureException {
        try (Connection conn = connectionPool.getConnection()) {
            
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM notes WHERE id = ?")) {
                int param = 1;
                ps.setInt(param++, id);
                ps.execute();
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
            }
        } catch (SQLException e) {
            log.error("Failed to lookup newspaper ids", e);
            throw new DAOFailureException("Err deleting note from id", e);
        }
    }
    
}
