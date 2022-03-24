package dk.kb.kula190.dao;

import dk.kb.kula190.model.Note;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DaoNoteHelper {
    
    static List<Note> getNotes(String batchID,
                               String newspaperID,
                               LocalDate date,
                               String editionTitle,
                               String sectionTitle,
                               Integer pageNumber,
                               Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                """
                SELECT *
                from  notes
                where
                    (?=1 or batchid = ? ) and
                    (?=1 or avisid = ?) and
                    (?=1 or edition_date = ?) and
                    edition_title = ? and
                    section_title = ? and
                    page_number = ?
                ORDER BY id desc""")) {
            int param = 1;
            
            param = setString(ps, param, batchID);
            param = setString(ps, param, newspaperID);
            param = setDate(ps, param, date);
            param = setString(ps, param, editionTitle);
            param = setString(ps, param, sectionTitle);
            param = setInteger(ps, param, pageNumber);
            
            try (ResultSet res = ps.executeQuery()) {
                List<Note> notes = new ArrayList<>();
                while (res.next()) {
                    notes.add(readNote(res));
                }
                return notes;
            }
        }
    }
    
    
    static Integer getNoteCount(String batchID,
                               String newspaperID,
                               LocalDate date,
                               String editionTitle,
                               String sectionTitle,
                               Integer pageNumber,
                               Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                """
                SELECT count(*)
                from  notes
                where
                    batchid = ? and
                    avisid = ? and
                    edition_date = ? and
                    edition_title = ? and
                    section_title = ? and
                    page_number = ?
                ORDER BY id desc""")) {
            int param = 1;
            param = setString(ps, param, batchID);
            param = setString(ps, param, newspaperID);
            param = setDate(ps, param, date);
            param = setString(ps, param, editionTitle);
            param = setString(ps, param, sectionTitle);
            param = setInteger(ps, param, pageNumber);
            
            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                   return res.getInt(0);
                }
            }
        }
        return null;
    }
    
    private static int setInteger(PreparedStatement ps, int param, Integer value) throws SQLException {
        if (value == null){
            ps.setNull(param++,Types.INTEGER);
        } else {
            ps.setInt(param++, value);
        }
        return param;
    }
    
    private static int setDate(PreparedStatement ps, int param, LocalDate value) throws SQLException {
        if (value == null){
            ps.setNull(param++,Types.DATE);
        } else {
            ps.setDate(param++, Date.valueOf(value));
        }
        return param;
    }
    
    private static int setString(PreparedStatement ps, int param, String value) throws SQLException {
        if (value == null){
            ps.setNull(param++,Types.VARCHAR);
        } else {
            ps.setString(param++, value);
        }
        return param;
    }
    
    static List<Note> getAllNotes(String batchID, Connection conn) throws SQLException {
        // return getNotes(batchID)
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
        return getNotes(batchID, null, null, null, null, null, conn);
    }
    
    
    static List<Note> getNewspaperLevelNotes(String avisID, Connection conn) throws SQLException {
        return getNotes(null, avisID, null, null, null, null, conn);
    }
    
    static List<Note> getDayLevelNotes(String batchID, String newspaperID, LocalDate date, Connection conn)
            throws SQLException {
        return getNotes(batchID, newspaperID,date, null, null, null, conn);
    }
    
    
    static Map<String, List<Note>> getEditionLevelNotes(String batchID,
                                                        String newspaperID,
                                                        LocalDate date,
                                                        Connection conn)
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
    
}
