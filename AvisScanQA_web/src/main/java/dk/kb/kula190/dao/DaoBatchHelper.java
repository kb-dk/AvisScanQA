package dk.kb.kula190.dao;

import dk.kb.kula190.model.Batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static dk.kb.kula190.dao.DaoUtils.toOffsetDateTime;

public class DaoBatchHelper {
    static List<Batch> getLatestBatches(String avisID, Connection conn) throws SQLException {
        List<Batch> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select * "
                + "from batch b "
                + "where avisid = ? and "
                + "b.lastmodified >= ALL(SELECT lastmodified FROM batch WHERE batchid=b.batchid)")) {
            ps.setString(1, avisID);
            
            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    final Batch e = readBatch(conn, res);
                    results.add(e);
                }
            }
            return results;
        }
    }
    
    
    static Batch getLatestBatch(String batchID, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "select * "
                + "from batch b "
                + "where batchid = ? and "
                + "      b.lastmodified >= ALL(SELECT lastmodified FROM batch WHERE batchid=b.batchid) limit 1")) {
            ps.setString(1, batchID);
            
            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    return readBatch(conn, res);
                }
            }
        }
        return null;
    }
    
    static List<Batch> getAllBatches(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * "
                                                          + " FROM batch b1"
                                                          + " WHERE lastmodified >= ALL(SELECT lastmodified FROM batch b2 WHERE b1.batchid = b2.batchid)")) {
            List<Batch> list = new ArrayList<>();
            try (ResultSet res = ps.executeQuery()) {
                
                while (res.next()) {
                    list.add(readBatch(conn, res));
                }
                
            }
            return list;
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
    
    
    private static Batch readBatch(Connection conn, ResultSet res) throws SQLException {
        String batchID = res.getString("batchid");
        return new Batch().batchid(batchID)
                          .avisid(res.getString("avisid"))
                          .roundtrip(res.getInt("roundtrip"))
                          .notes(DaoNoteHelper.getBatchLevelNotes(batchID,conn))
                          .startDate(res.getDate("start_date").toLocalDate())
                          .endDate(res.getDate("end_date").toLocalDate())
                          .deliveryDate(res.getDate("delivery_date").toLocalDate())
                          .problems(res.getString("problems"))
                          .state(res.getString("state"))
                          .numProblems(res.getInt("num_problems"))
                          .username(res.getString("username"))
                          .lastModified(toOffsetDateTime(res.getTimestamp("lastmodified")))
                          .numNotes(DaoNoteHelper.getNumNotes(batchID, conn));
    }
    
    static Stream<LocalDate> batchDays(Batch batch) {
        return LongStream.rangeClosed(0, ChronoUnit.DAYS.between(batch.getStartDate(), batch.getEndDate()))
                         .mapToObj((long t) -> batch.getStartDate().plusDays(t));
    }
}
