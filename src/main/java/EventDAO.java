import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    public Event create(Event e) throws SQLException {
        String sql = "INSERT INTO events(name, event_type, event_date, hall_id, curator_id, max_visitors, description) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getEventType());
            ps.setString(3, e.getEventDate());
            setNullableInt(ps, 4, e.getHallId());
            setNullableInt(ps, 5, e.getCuratorId());
            setNullableInt(ps, 6, e.getMaxVisitors());
            ps.setString(7, e.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) e.setId(rs.getInt(1));
            }
        }
        return e;
    }

    /**
     * Створення події з перевіркою доступності залу (транзакція)
     */
    public Event createWithValidation(Event e) throws SQLException {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // Перевіряємо, чи немає іншої події в цьому залі в цей день
            if (e.getHallId() != null) {
                String checkSql = "SELECT COUNT(*) FROM events WHERE hall_id = ? AND event_date = ?";
                try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                    checkPs.setInt(1, e.getHallId());
                    checkPs.setString(2, e.getEventDate());
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("Зал вже зайнятий на цю дату!");
                        }
                    }
                }
            }

            // Створюємо подію
            String sql = "INSERT INTO events(name, event_type, event_date, hall_id, curator_id, max_visitors, description) VALUES(?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, e.getName());
                ps.setString(2, e.getEventType());
                ps.setString(3, e.getEventDate());
                setNullableInt(ps, 4, e.getHallId());
                setNullableInt(ps, 5, e.getCuratorId());
                setNullableInt(ps, 6, e.getMaxVisitors());
                ps.setString(7, e.getDescription());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) e.setId(rs.getInt(1));
                }
            }

            conn.commit();
            return e;
        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException rollbackEx) { rollbackEx.printStackTrace(); }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException closeEx) { closeEx.printStackTrace(); }
            }
        }
    }

    public Event findById(int id) throws SQLException {
        String sql = "SELECT e.*, h.name as hall_name, c.name as cur_name " +
                "FROM events e " +
                "LEFT JOIN halls h ON e.hall_id = h.id " +
                "LEFT JOIN curators c ON e.curator_id = c.id " +
                "WHERE e.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Event> findAll(String eventType, Integer hallId, String dateFrom, String dateTo, String sortBy, boolean asc) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT e.*, h.name as hall_name, c.name as cur_name " +
                "FROM events e " +
                "LEFT JOIN halls h ON e.hall_id = h.id " +
                "LEFT JOIN curators c ON e.curator_id = c.id " +
                "WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (eventType != null && !eventType.isEmpty()) {
            sb.append(" AND e.event_type = ?");
            params.add(eventType);
        }
        if (hallId != null) {
            sb.append(" AND e.hall_id = ?");
            params.add(hallId);
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            sb.append(" AND e.event_date >= ?");
            params.add(dateFrom);
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            sb.append(" AND e.event_date <= ?");
            params.add(dateTo);
        }

        String orderField = "e.event_date";
        if ("name".equalsIgnoreCase(sortBy)) orderField = "e.name";
        else if ("event_type".equalsIgnoreCase(sortBy)) orderField = "e.event_type";

        sb.append(" ORDER BY ").append(orderField).append(asc ? " ASC" : " DESC");

        List<Event> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Event> findUpcoming() throws SQLException {
        String sql = "SELECT e.*, h.name as hall_name, c.name as cur_name " +
                "FROM events e " +
                "LEFT JOIN halls h ON e.hall_id = h.id " +
                "LEFT JOIN curators c ON e.curator_id = c.id " +
                "WHERE e.event_date >= date('now') " +
                "ORDER BY e.event_date ASC";
        List<Event> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean update(Event e) throws SQLException {
        String sql = "UPDATE events SET name=?, event_type=?, event_date=?, hall_id=?, curator_id=?, max_visitors=?, description=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getEventType());
            ps.setString(3, e.getEventDate());
            setNullableInt(ps, 4, e.getHallId());
            setNullableInt(ps, 5, e.getCuratorId());
            setNullableInt(ps, 6, e.getMaxVisitors());
            ps.setString(7, e.getDescription());
            ps.setInt(8, e.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM events WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) ps.setInt(index, value);
        else ps.setNull(index, Types.INTEGER);
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("id"));
        e.setName(rs.getString("name"));
        e.setEventType(rs.getString("event_type"));
        e.setEventDate(rs.getString("event_date"));
        
        int hallId = rs.getInt("hall_id");
        if (!rs.wasNull()) e.setHallId(hallId);
        
        int curId = rs.getInt("curator_id");
        if (!rs.wasNull()) e.setCuratorId(curId);
        
        int maxVis = rs.getInt("max_visitors");
        if (!rs.wasNull()) e.setMaxVisitors(maxVis);
        
        e.setDescription(rs.getString("description"));

        try {
            e.setHallName(rs.getString("hall_name"));
            e.setCuratorName(rs.getString("cur_name"));
        } catch (SQLException ignored) {}

        return e;
    }
}
