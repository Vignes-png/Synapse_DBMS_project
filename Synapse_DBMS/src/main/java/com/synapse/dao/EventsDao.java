package com.synapse.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.synapse.db.ConnectionUtil;
import com.synapse.model.Event;

public class EventsDao {

    public Integer create(Event e) throws SQLException {
        String sql = """
            INSERT INTO events (event_name, event_description, event_type, schedule, prize_money, venue_id)
            VALUES (?, ?, ?, ?, ?, ?) RETURNING event_id
        """;
        try (Connection con = ConnectionUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getEventName());
            ps.setString(2, e.getEventDescription());
            ps.setString(3, e.getEventType());
            ps.setTimestamp(4, Timestamp.valueOf(e.getSchedule()));
            ps.setBigDecimal(5, e.getPrizeMoney());
            ps.setInt(6, e.getVenueId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    // READ by ID
    public Event findById(int id) throws SQLException {
        String sql = """
            SELECT event_id, event_name, event_description, event_type, schedule, prize_money, venue_id
            FROM events WHERE event_id = ?
        """;
        try (Connection con = ConnectionUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    // READ all
    public List<Event> findAll() throws SQLException {
        String sql = """
            SELECT event_id, event_name, event_description, event_type, schedule, prize_money, venue_id
            FROM events ORDER BY event_id
        """;
        List<Event> list = new ArrayList<>();
        try (Connection con = ConnectionUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // UPDATE
    public boolean update(int id, String name, String desc, String type,
                          LocalDateTime schedule, BigDecimal prize, int venueId) throws SQLException {
        String sql = """
            UPDATE events
            SET event_name=?, event_description=?, event_type=?, schedule=?, prize_money=?, venue_id=?
            WHERE event_id=?
        """;
        try (Connection con = ConnectionUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, desc);
            ps.setString(3, type);
            ps.setTimestamp(4, Timestamp.valueOf(schedule));
            ps.setBigDecimal(5, prize);
            ps.setInt(6, venueId);
            ps.setInt(7, id);
            return ps.executeUpdate() == 1;
        }
    }

    // DELETE
    public boolean delete(int id) throws SQLException {
        try (Connection con = ConnectionUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM events WHERE event_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    // Helper
    private Event map(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setEventId(rs.getInt("event_id"));
        e.setEventName(rs.getString("event_name"));
        e.setEventDescription(rs.getString("event_description"));
        e.setEventType(rs.getString("event_type"));
        e.setSchedule(rs.getTimestamp("schedule").toLocalDateTime());
        e.setPrizeMoney(rs.getBigDecimal("prize_money"));
        e.setVenueId(rs.getInt("venue_id"));
        return e;
    }
}
