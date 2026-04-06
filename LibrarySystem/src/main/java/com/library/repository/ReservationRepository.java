package com.library.repository;

import com.library.db.DatabaseConnection;
import com.library.model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationRepository {

    public Reservation createReservation(int userId, int bookId,
                                         String resDate) throws SQLException {
        String resId       = generateResId();
        int    queuePos    = getNextQueuePosition(bookId);
        String status      = queuePos == 1 ? "Ready for Pickup!" : "Waiting";

        String sql = """
            INSERT INTO reservations (res_id, user_id, book_id, res_date, queue_position, status)
            VALUES (?,?,?,?,?,?)
            """;
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
        ps.setString(1, resId);
        ps.setInt(2, userId);
        ps.setInt(3, bookId);
        ps.setString(4, resDate);
        ps.setInt(5, queuePos);
        ps.setString(6, status);
        ps.executeUpdate();
        ps.close();

        ResultSet keys = DatabaseConnection.getConnection()
            .createStatement().executeQuery("SELECT last_insert_rowid()");
        int newId = keys.next() ? keys.getInt(1) : -1;
        keys.close();
        return findById(newId);
    }

    public List<Reservation> findActiveByUserId(int userId) throws SQLException {
        String sql = """
            SELECT r.*, b.title as book_title
            FROM reservations r JOIN books b ON r.book_id = b.id
            WHERE r.user_id = ? AND r.status != 'Cancelled'
            ORDER BY r.res_date ASC
            """;
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
        ps.setInt(1, userId);
        return executeQuery(ps);
    }

    public Reservation findById(int id) throws SQLException {
        String sql = """
            SELECT r.*, b.title as book_title
            FROM reservations r JOIN books b ON r.book_id = b.id
            WHERE r.id = ?
            """;
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
        ps.setInt(1, id);
        List<Reservation> list = executeQuery(ps);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean hasActiveReservation(int userId, int bookId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM reservations
            WHERE user_id=? AND book_id=? AND status NOT IN ('Cancelled')
            """;
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, bookId);
        ResultSet rs = ps.executeQuery();
        boolean has = rs.next() && rs.getInt(1) > 0;
        rs.close(); ps.close();
        return has;
    }

    public boolean cancelReservation(int resId, int userId) throws SQLException {
        String sql = """
            UPDATE reservations SET status = 'Cancelled'
            WHERE id = ? AND user_id = ? AND status != 'Cancelled'
            """;
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
        ps.setInt(1, resId);
        ps.setInt(2, userId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public int countActiveByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE user_id=? AND status NOT IN ('Cancelled')";
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close(); ps.close();
        return count;
    }

    private int getNextQueuePosition(int bookId) throws SQLException {
        String sql = """
            SELECT COALESCE(MAX(queue_position), 0) + 1
            FROM reservations WHERE book_id = ? AND status != 'Cancelled'
            """;
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
        ps.setInt(1, bookId);
        ResultSet rs = ps.executeQuery();
        int pos = rs.next() ? rs.getInt(1) : 1;
        rs.close(); ps.close();
        return pos;
    }

    private String generateResId() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations";
        ResultSet rs = DatabaseConnection.getConnection().createStatement().executeQuery(sql);
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close();
        return String.format("RES-%04d", count + 1);
    }

    private List<Reservation> executeQuery(PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        List<Reservation> list = new ArrayList<>();
        while (rs.next()) list.add(mapRow(rs));
        rs.close(); ps.close();
        return list;
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setResId(rs.getString("res_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setBookId(rs.getInt("book_id"));
        r.setResDate(rs.getString("res_date"));
        r.setQueuePosition(rs.getInt("queue_position"));
        r.setStatus(rs.getString("status"));
        try { r.setBookTitle(rs.getString("book_title")); } catch (Exception ignored) {}
        return r;
    }
}
