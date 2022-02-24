package dk.kb.kula190.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

public class DaoUtils {
    static int setNullable(PreparedStatement ps, int param, String value) throws SQLException {
        if (value == null) {
            ps.setNull(param++, Types.VARCHAR);
        } else {
            ps.setString(param++, value);
        }
        return param;
    }
    
    static int setNullable(PreparedStatement ps, int param, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(param++, Types.INTEGER);
        } else {
            ps.setInt(param++, value);
        }
        return param;
    }
    
    static int setNullable(PreparedStatement ps, int param, LocalDate value) throws SQLException {
        if (value == null) {
            ps.setNull(param++, Types.DATE);
        } else {
            ps.setDate(param++, Date.valueOf(value));
        }
        return param;
    }
}
