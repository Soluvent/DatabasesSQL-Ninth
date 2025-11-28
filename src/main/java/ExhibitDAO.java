import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExhibitDAO {

    public Exhibit create(Exhibit e) throws SQLException {
        String sql = "INSERT INTO exhibits(name, description, creation_period, arrival_date, category_id, hall_id, curator_id) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getDescription());
            ps.setString(3, e.getCreationPeriod());
            ps.setString(4, e.getArrivalDate());
            setNullableInt(ps, 5, e.getCategoryId());
            setNullableInt(ps, 6, e.getHallId());
            setNullableInt(ps, 7, e.getCuratorId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) e.setId(rs.getInt(1));
            }
        }
        return e;
    }

    public Exhibit findById(int id) throws SQLException {
        String sql = "SELECT e.*, c.name as cat_name, h.name as hall_name, cu.name as cur_name " +
                "FROM exhibits e " +
                "LEFT JOIN categories c ON e.category_id = c.id " +
                "LEFT JOIN halls h ON e.hall_id = h.id " +
                "LEFT JOIN curators cu ON e.curator_id = cu.id " +
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

    /**
     * Універсальний метод пошуку з фільтрацією та сортуванням
     * @param nameLike - пошук за назвою (LIKE)
     * @param periodLike - пошук за періодом створення (LIKE)
     * @param hallId - фільтр за залом
     * @param categoryId - фільтр за категорією
     * @param sortBy - поле сортування: "name", "arrival_date", "creation_period"
     * @param asc - напрямок сортування
     */
    public List<Exhibit> findAll(String nameLike, String periodLike, Integer hallId, 
                                  Integer categoryId, String sortBy, boolean asc) throws SQLException {
        StringBuilder sb = new StringBuilder(
                "SELECT e.*, c.name as cat_name, h.name as hall_name, cu.name as cur_name " +
                "FROM exhibits e " +
                "LEFT JOIN categories c ON e.category_id = c.id " +
                "LEFT JOIN halls h ON e.hall_id = h.id " +
                "LEFT JOIN curators cu ON e.curator_id = cu.id " +
                "WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (nameLike != null && !nameLike.isEmpty()) {
            sb.append(" AND e.name LIKE ?");
            params.add("%" + nameLike + "%");
        }
        if (periodLike != null && !periodLike.isEmpty()) {
            sb.append(" AND e.creation_period LIKE ?");
            params.add("%" + periodLike + "%");
        }
        if (hallId != null) {
            sb.append(" AND e.hall_id = ?");
            params.add(hallId);
        }
        if (categoryId != null) {
            sb.append(" AND e.category_id = ?");
            params.add(categoryId);
        }

        // Безпечне сортування - лише дозволені поля
        String orderField = "e.arrival_date";
        if ("name".equalsIgnoreCase(sortBy)) orderField = "e.name";
        else if ("creation_period".equalsIgnoreCase(sortBy)) orderField = "e.creation_period";
        else if ("arrival_date".equalsIgnoreCase(sortBy)) orderField = "e.arrival_date";

        sb.append(" ORDER BY ").append(orderField).append(asc ? " ASC" : " DESC");

        List<Exhibit> list = new ArrayList<>();
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

    public List<Exhibit> findByHall(int hallId) throws SQLException {
        return findAll(null, null, hallId, null, "name", true);
    }

    public List<Exhibit> findByCategory(int categoryId) throws SQLException {
        return findAll(null, null, null, categoryId, "name", true);
    }

    public List<Exhibit> searchByName(String name) throws SQLException {
        return findAll(name, null, null, null, "name", true);
    }

    public List<Exhibit> searchByPeriod(String period) throws SQLException {
        return findAll(null, period, null, null, "creation_period", true);
    }

    public boolean update(Exhibit e) throws SQLException {
        String sql = "UPDATE exhibits SET name=?, description=?, creation_period=?, arrival_date=?, " +
                "category_id=?, hall_id=?, curator_id=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getDescription());
            ps.setString(3, e.getCreationPeriod());
            ps.setString(4, e.getArrivalDate());
            setNullableInt(ps, 5, e.getCategoryId());
            setNullableInt(ps, 6, e.getHallId());
            setNullableInt(ps, 7, e.getCuratorId());
            ps.setInt(8, e.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM exhibits WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int countByHall(int hallId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM exhibits WHERE hall_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) ps.setInt(index, value);
        else ps.setNull(index, Types.INTEGER);
    }

    private Exhibit mapRow(ResultSet rs) throws SQLException {
        Exhibit e = new Exhibit();
        e.setId(rs.getInt("id"));
        e.setName(rs.getString("name"));
        e.setDescription(rs.getString("description"));
        e.setCreationPeriod(rs.getString("creation_period"));
        e.setArrivalDate(rs.getString("arrival_date"));
        
        int catId = rs.getInt("category_id");
        if (!rs.wasNull()) e.setCategoryId(catId);
        
        int hallId = rs.getInt("hall_id");
        if (!rs.wasNull()) e.setHallId(hallId);
        
        int curId = rs.getInt("curator_id");
        if (!rs.wasNull()) e.setCuratorId(curId);

        // Додаткові поля з JOIN
        try {
            e.setCategoryName(rs.getString("cat_name"));
            e.setHallName(rs.getString("hall_name"));
            e.setCuratorName(rs.getString("cur_name"));
        } catch (SQLException ignored) {}

        return e;
    }
}
