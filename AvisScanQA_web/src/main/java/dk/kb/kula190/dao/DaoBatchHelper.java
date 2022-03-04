package dk.kb.kula190.dao;

import dk.kb.kula190.model.Batch;
import dk.kb.kula190.model.Note;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class DaoBatchHelper {
    static List<Batch> getLatestBatches(String avisID, Connection conn) throws SQLException {
        List<Batch> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select batchid, avisid, roundtrip, start_date, end_date, delivery_date, problems, state, num_problems, username, lastmodified "
                + "from batch b "
                + "where avisid = ? and "
                + "b.lastmodified >= ALL(SELECT lastmodified FROM batch WHERE batchid=b.batchid)")) {
            ps.setString(1, avisID);
            
            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    
                    String batchID = res.getString("batchid");
                    results.add(new Batch().batchid(batchID)
                                           .avisid(avisID)
                                           .roundtrip(res.getInt("roundtrip"))
                                           .startDate(res.getDate("start_date").toLocalDate())
                                           .endDate(res.getDate("end_date").toLocalDate())
                                           .deliveryDate(res.getDate("delivery_date").toLocalDate())
                                           .problems(res.getString("problems"))
                                           .state(res.getString("state"))
                                           .numProblems(res.getInt("num_problems"))
                                           .username(res.getString("username"))
                                           .lastModified(OffsetDateTime.ofInstant(res.getTimestamp("lastmodified")
                                                                                     .toInstant(),
                                                                                  ZoneId.systemDefault()))
                                           .numNotes(getNumNotes(batchID, conn)));
                }
            }
            return results;
        }
    }
    
    static Batch getLatestBatch(String batchID, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "select batchid, avisid, roundtrip, start_date, end_date, delivery_date, problems, state, num_problems, username, lastmodified "
                + "from batch b "
                + "where batchid = ? and "
                + "b.lastmodified >= ALL(SELECT lastmodified FROM batch WHERE batchid=b.batchid) limit 1")) {
            ps.setString(1, batchID);
            
            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    
                    return new Batch().batchid(batchID)
                                      .avisid(res.getString("avisid"))
                                      .roundtrip(res.getInt(
                                              "roundtrip"))
                                      .startDate(res.getDate("start_date").toLocalDate())
                                      .endDate(res.getDate(
                                              "end_date").toLocalDate())
                                      .deliveryDate(res.getDate("delivery_date").toLocalDate())
                                      .problems(res.getString("problems"))
                                      .state(res.getString("state"))
                                      .numProblems(res.getInt("num_problems"))
                                      .username(res.getString("username"))
                                      .lastModified(OffsetDateTime.ofInstant(res.getTimestamp("lastmodified")
                                                                                .toInstant(),
                                                                             ZoneId.systemDefault()))
                                      .numNotes(getNumNotes(batchID, conn));
                }
            }
        }
        return null;
    }
    
    static List<Batch> getAllBatches(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT b1.batchid, b1.avisid, roundtrip, start_date, end_date, delivery_date, problems, "
                + "num_problems, state, b1.username, lastmodified, n.notes as notes "
                + "FROM batch b1"
                + "   LEFT JOIN  notes n on b1.batchid = n.batchid and "
                + "                         b1.avisid = n.avisid and "
                + "                         n.edition_date is null and "
                + "                         n.edition_title is null and "
                + "                         n.section_title is null and "
                + "                         n.page_number is null"
                + " WHERE"
                + " lastmodified >= ALL("
                + "     SELECT lastmodified FROM batch b2 WHERE b1.batchid = b2.batchid)")) {
            List<Batch> list = new ArrayList<>();
            try (ResultSet res = ps.executeQuery()) {
                
                while (res.next()) {
                    list.add(new Batch().batchid(res.getString("batchid"))
                                        .avisid(res.getString("avisid"))
                                        .roundtrip(res.getInt("roundtrip"))
                                        .startDate(res.getDate("start_date").toLocalDate())
                                        .endDate(res.getDate("end_date").toLocalDate())
                                        .deliveryDate(res.getDate("delivery_date").toLocalDate())
                                        .problems(res.getString("problems"))
                                        .state(res.getString("state"))
                                        .notes(res.getString("notes"))
                                        .numProblems(res.getInt("num_problems"))
                                        .username(res.getString("username"))
                                        .lastModified(OffsetDateTime.ofInstant(res.getTimestamp("lastmodified")
                                                                                  .toInstant(),
                                                                               ZoneId.systemDefault())));
                }
                
            }
            for (Batch batch : list) {
                batch.setNumNotes(getNumNotes(batch.getBatchid(), conn));
            }
            return list;
        }
    }
    
    public static Integer getNumNotes(@Nonnull String batchID, Connection conn) throws SQLException {
        //TODO this should probably be included in the callers SQL statement, rather being a separate statement
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT count(*) as numNotes "
                + " from notes "
                + " where batchid = ? "
                + " limit 1")) {
            int param = 1;
            ps.setString(param++, batchID);
            try (ResultSet res = ps.executeQuery()) {
                List<Note> notes = new ArrayList<>();
                if (res.next()) {
                    return res.getInt("numNotes");
                } else {
                    return null;
                }
                
            }
        }
    }
    
    static void setBatchState(String batchID, String state, String username, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO batch(batchid, avisid, roundtrip, start_date, end_date, delivery_date, problems, state, num_problems, username, lastmodified)  "
                + " ( SELECT batchid, avisid, roundtrip, start_date, end_date, delivery_date, problems, ?, num_problems, ?, now() "
                + " FROM batch "
                + " WHERE batchid=? AND "
                + " lastmodified >= ALL("
                + "     SELECT lastmodified FROM batch WHERE batchid=?)"
                + "      )")) {
            int param = 1;
            ps.setString(param++, state);
            ps.setString(param++, username);
            ps.setString(param++, batchID);
            ps.setString(param++, batchID);
            ps.execute();
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        }
    }
    
    static Stream<LocalDate> batchDays(Batch batch) {
        return LongStream.rangeClosed(0, ChronoUnit.DAYS.between(batch.getStartDate(), batch.getEndDate()))
                         .mapToObj((long t) -> batch.getStartDate().plusDays(t));
    }
}
