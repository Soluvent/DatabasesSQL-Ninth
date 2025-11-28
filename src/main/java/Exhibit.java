public class Exhibit {
    private Integer id;
    private String name;
    private String description;
    private String creationPeriod;
    private String arrivalDate;
    private Integer categoryId;
    private Integer hallId;
    private Integer curatorId;

    // Додаткові поля для відображення
    private String categoryName;
    private String hallName;
    private String curatorName;

    public Exhibit() {}

    public Exhibit(Integer id, String name, String description, String creationPeriod,
                   String arrivalDate, Integer categoryId, Integer hallId, Integer curatorId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationPeriod = creationPeriod;
        this.arrivalDate = arrivalDate;
        this.categoryId = categoryId;
        this.hallId = hallId;
        this.curatorId = curatorId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreationPeriod() { return creationPeriod; }
    public void setCreationPeriod(String creationPeriod) { this.creationPeriod = creationPeriod; }

    public String getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(String arrivalDate) { this.arrivalDate = arrivalDate; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public Integer getHallId() { return hallId; }
    public void setHallId(Integer hallId) { this.hallId = hallId; }

    public Integer getCuratorId() { return curatorId; }
    public void setCuratorId(Integer curatorId) { this.curatorId = curatorId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }

    public String getCuratorName() { return curatorName; }
    public void setCuratorName(String curatorName) { this.curatorName = curatorName; }

    @Override
    public String toString() {
        return String.format("[%d] %s | Період: %s | Надходження: %s | Зал: %s | Категорія: %s",
                id, name,
                creationPeriod != null ? creationPeriod : "-",
                arrivalDate,
                hallName != null ? hallName : "-",
                categoryName != null ? categoryName : "-");
    }
}
