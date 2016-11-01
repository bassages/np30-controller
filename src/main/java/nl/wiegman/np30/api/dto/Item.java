package nl.wiegman.np30.api.dto;

import java.util.List;

public class Item {

    private String id;
    private String title;
    private List<Item> children;
    private boolean isContainer;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Item> getChildren() {
        return children;
    }

    public void setChildren(List<Item> children) {
        this.children = children;
    }

    public boolean isContainer() {
        return isContainer;
    }

    public void setIsContainer(boolean isContainer) {
        this.isContainer = isContainer;
    }
}
