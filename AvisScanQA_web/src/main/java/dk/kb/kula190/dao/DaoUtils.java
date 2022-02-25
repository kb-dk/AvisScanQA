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
    
    public static LocalDate nullableDate(String date) {
        date = nullable(date);
        if (date != null) {
            return LocalDate.parse(date);
        }
        return null;
    }
    
    public static Integer nullableInteger(String integer) {
        integer = nullable(integer);
        if (integer != null) {
            return Integer.parseInt(integer);
        }
        return null;
    }
    
    public static String nullable(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("null")) {
            return null;
        }
        return value;
    }
}
