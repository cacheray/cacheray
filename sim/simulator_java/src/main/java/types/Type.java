package types;

public class Type {
    private Long id;
    private String name;

    public String getName() {
        return name;
    }

    public Integer getSize() {
        return size;
    }

    private Integer size;

    public Long getId() {
        return id;
    }

    public Type(Long id, String name, Integer size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }
}
