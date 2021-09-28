package thito.fancywaystones.ui;

public enum ItemSort {
    NAME("By Name", "name"),
    DATE_CREATED("By Date Created", "date-created"),
    USERS("By Users", "users"),
    VISITS("By Visits", "visits"),
    VISITORS("By Visitors", "visitors"),
    USED_TIME("By Used Time", "used-time")
    ;

    String path, message;

    ItemSort(String path, String message) {
        this.path = path;
        this.message = message;
    }

    public String getAscendMessage() {
        return "{language.order.ascend."+message+"}";
    }

    public String getPath() {
        return path;
    }

    public String getDescendMessage() {
        return "{language.order.descend."+message+"}";
    }
}
