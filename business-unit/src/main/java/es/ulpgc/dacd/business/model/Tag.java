package es.ulpgc.dacd.business.model;

public class Tag {
    private final String name;
    private final int count;

    public Tag(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }
}
