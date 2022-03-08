package dk.kb.kula190.dao;

import dk.kb.kula190.model.Note;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DaoNoteHelper {
    
    static List<Note> getAllNotes(String batchID, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * from notes where batchid = ?")) {
            int param = 1;
            ps.setString(param++, batchID);
            try (ResultSet res = ps.executeQuery()) {
                List<Note> notes = new ArrayList<>();
                while (res.next()) {
                    notes.add(readNote(res));
                }
                return notes;
            }
        }
    }
    
    static List<Note> getBatchLevelNotes(String batchID, Connection conn) throws SQLException {
        String dayNotes = null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT * "
                                                          + "from  notes "
                                                          + "where "
                                                          + " batchid = ? and "
                                                          + " edition_date is null and "
                                                          + " edition_title is null and "
                                                          + " section_title is null and "
                                                          + " page_number is null"
                                                          + " ORDER BY id desc ")) {
            int param = 1;
            ps.setString(param++, batchID);
            List<Note> result = new ArrayList<>();
            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    result.add(readNote(res));
                }
            }
            return result;
        }
    }
    static List<Note> getNewspaperLevelNotes(String avisID, Connection conn) throws SQLException {
        String dayNotes = null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT * "
                                                          + "FROM  notes "
                                                          + "WHERE "
                                                          + "avisid = ? AND"
                                                          + " batchid IS NULL AND "
                                                          + " edition_date IS NULL AND "
                                                          + " edition_title IS NULL AND "
                                                          + " section_title IS NULL AND "
                                                          + " page_number IS NULL"
                                                          + " ORDER BY id DESC ")) {
            int param = 1;
            ps.setString(param++, avisID);
            List<Note> result = new ArrayList<>();
            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    result.add(readNote(res));
                }
            }
            return result;
        }
    }
    
    static List<Note> getDayLevelNotes(String batchID, String newspaperID, LocalDate date, Connection conn)
            throws SQLException {
        String dayNotes = null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT * "
                                                          + "from notes "
                                                          + "where batchid = ? and "
                                                          + "      avisid = ? and "
                                                          + "      edition_date = ? and "
                                                          + "      edition_title is null and "
                                                          + "      section_title is null and "
                                                          + "      page_number is null"
                                                          + " ORDER BY id desc ")) {
            int param = 1;
            ps.setString(param++, batchID);
            ps.setString(param++, newspaperID);
            ps.setDate(param++, Date.valueOf(date));
            List<Note> result = new ArrayList<>();
            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    result.add(readNote(res));
                }
            }
            return result;
        }
    }
    
    
    static Map<String, List<Note>> getEditionLevelNotes(String batchID, String newspaperID, LocalDate date, Connection conn)
            throws SQLException {
        Map<String, List<Note>> editionNotes = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT* "
                                                          + " from  notes "
                                                          + " where batchid = ? and "
                                                          + "       avisid = ? and "
                                                          + "       edition_date = ? and "
                                                          + "       section_title is null and "
                                                          + "       page_number is null"
                                                          + " ORDER BY id desc ")) {
            int param = 1;
            ps.setString(param++, batchID);
            ps.setString(param++, newspaperID);
            ps.setDate(param++, Date.valueOf(date));
            
            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    Note note = readNote(res);
                    List<Note> editionNoteList = editionNotes.getOrDefault(note.getEditionTitle(), new ArrayList<>());
                    editionNoteList.add(note);
                    editionNotes.put(note.getEditionTitle(), editionNoteList);
                }
            }
        }
        return editionNotes;
    }
    
    static Map<Integer, List<Note>> getPageLevelNotes(String batchID,
                                                      String newspaperID,
                                                      LocalDate date,
                                                      String editionTitle,
                                                      Connection conn) throws SQLException {
        Map<Integer, List<Note>> pageNotes = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * "
                + "from  notes "
                + "where batchid = ? and "
                + "      avisid = ? and "
                + "      edition_date = ? and "
                + "      edition_title = ? and "
                + "      section_title is not null and "
                + "      page_number is not null"
                + " ORDER BY id desc ")) {
            int param = 1;
            ps.setString(param++, batchID);
            ps.setString(param++, newspaperID);
            ps.setDate(param++, Date.valueOf(date));
            ps.setString(param++, editionTitle);
            
            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    Note note = readNote(res);
                    List<Note> pageNoteList = pageNotes.getOrDefault(note.getPageNumber(), new ArrayList<>());
                    pageNoteList.add(note);
                    pageNotes.put(note.getPageNumber(), pageNoteList);
                }
            }
        }
        return pageNotes;
    }
    
    private static Note readNote(ResultSet res) throws SQLException {
        Note note = new Note().id(res.getInt("id"))
                              .batchid(res.getString("batchid"))
                              .avisid(res.getString("avisid"))
                              .editionDate(DaoUtils.readNullableDate(res, "edition_date"))
                              .editionTitle(res.getString("edition_title"))
                              .sectionTitle(res.getString("section_title"))
                              .pageNumber(DaoUtils.readNullableInt(res, "page_number"))
                              .username(res.getString("username"))
                              .note(res.getString("notes"))
                              .created(DaoUtils.toOffsetDateTime(res.getTimestamp("created")));
        return note;
    }
    
    
    public static Integer getNumNotes(@Nonnull String batchID, Connection conn) throws SQLException {
        //TODO this should probably be included in the callers SQL statement, rather being a separate statement
        try (PreparedStatement ps = conn.prepareStatement("SELECT count(*) as numNotes "
                                                          + " from notes "
                                                          + " where batchid = ? "
                                                          + " limit 1")) {
            int param = 1;
            ps.setString(param++, batchID);
            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    return res.getInt("numNotes");
                } else {
                    return null;
                }
                
            }
        }
    }
}
