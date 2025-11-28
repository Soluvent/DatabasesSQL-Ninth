import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HallDAO {

    public Hall create(Hall h) throws SQLException {
        String sql = "INSERT INTO halls(name, floor, capacity) VALUES(?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, h.getName());
            ps.setInt(2, h.getFloor());
            if (h.getCapacity() != null) ps.setInt(3, h.getCapacity());
            else ps.setNull(3, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) h.setId(rs.getInt(1));
            }
        }
        return h;
    }

    public Hall findById(int id) throws SQLException {
        String sql = "SELECT id, name, floor, capacity FROM halls WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Hall> findAll() throws SQLException {
        String sql = "SELECT id, name, floor, capacity FROM halls ORDER BY floor, name";
        List<Hall> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean update(Hall h) throws SQLException {
        String sql = "UPDATE halls SET name=?, floor=?, capacity=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, h.getName());
            ps.setInt(2, h.getFloor());
            if (h.getCapacity() != null) ps.setInt(3, h.getCapacity());
            else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, h.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM halls WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Hall mapRow(ResultSet rs) throws SQLException {
        Hall h = new Hall();
        h.setId(rs.getInt("id"));
        h.setName(rs.getString("name"));
        h.setFloor(rs.getInt("floor"));
        int cap = rs.getInt("capacity");
        if (!rs.wasNull()) h.setCapacity(cap);
        return h;
    }
}
