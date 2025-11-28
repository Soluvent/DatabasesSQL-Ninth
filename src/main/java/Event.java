public class Event {
    private Integer id;
    private String name;
    private String eventType;
    private String eventDate;
    private Integer hallId;
    private Integer curatorId;
    private Integer maxVisitors;
    private String description;

    // Додаткові поля для відображення
    private String hallName;
    private String curatorName;

    public Event() {}

    public Event(Integer id, String name, String eventType, String eventDate,
                 Integer hallId, Integer curatorId, Integer maxVisitors, String description) {
        this.id = id;
        this.name = name;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.hallId = hallId;
        this.curatorId = curatorId;
        this.maxVisitors = maxVisitors;
        this.description = description;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public Integer getHallId() { return hallId; }
    public void setHallId(Integer hallId) { this.hallId = hallId; }

    public Integer getCuratorId() { return curatorId; }
    public void setCuratorId(Integer curatorId) { this.curatorId = curatorId; }

    public Integer getMaxVisitors() { return maxVisitors; }
    public void setMaxVisitors(Integer maxVisitors) { this.maxVisitors = maxVisitors; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }

    public String getCuratorName() { return curatorName; }
    public void setCuratorName(String curatorName) { this.curatorName = curatorName; }

    @Override
    public String toString() {
        return String.format("[%d] %s | Тип: %s | Дата: %s | Зал: %s | Куратор: %s",
                id, name, eventType, eventDate,
                hallName != null ? hallName : "-",
                curatorName != null ? curatorName : "-");
    }
}
