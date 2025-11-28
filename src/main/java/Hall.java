public class Hall {
    private Integer id;
    private String name;
    private int floor;
    private Integer capacity;

    public Hall() {}

    public Hall(Integer id, String name, int floor, Integer capacity) {
        this.id = id;
        this.name = name;
        this.floor = floor;
        this.capacity = capacity;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    @Override
    public String toString() {
        return String.format("[%d] %s (поверх %d)", id, name, floor);
    }
}
