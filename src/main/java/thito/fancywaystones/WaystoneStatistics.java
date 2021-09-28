package thito.fancywaystones;

import org.bukkit.configuration.*;

public class WaystoneStatistics {
    private int totalVisitors; // unique visitor
    private int totalVisits; // visit by player
    private int totalUsers;
    private long lastVisit;
    private long lastVisited;
    private long lastUsed;
    private long dateCreated;

    public void load(ConfigurationSection section) {
        if (section != null) {
            totalVisitors = section.getInt("total-visitors");
            totalVisits = section.getInt("total-visits");
            totalUsers = section.getInt("total-users");
            dateCreated = section.getLong("date-created", System.currentTimeMillis());
            lastVisit = section.getLong("last-visit", System.currentTimeMillis());
            lastVisited = section.getLong("last-visited", System.currentTimeMillis());
            lastUsed = section.getLong("last-used", System.currentTimeMillis());
        } else {
            lastVisit = lastVisited = lastUsed = dateCreated = System.currentTimeMillis();
        }
    }

    public void save(ConfigurationSection section) {
        section.set("total-visitors", totalVisitors);
        section.set("total-visits", totalVisits);
        section.set("total-users", totalUsers);
        section.set("date-created", dateCreated);
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public void setLastVisit(long lastVisit) {
        this.lastVisit = lastVisit;
    }

    public void setLastVisited(long lastVisited) {
        this.lastVisited = lastVisited;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public long getLastVisit() {
        return lastVisit;
    }

    public long getLastVisited() {
        return lastVisited;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public void setTotalVisitors(int totalVisitors) {
        this.totalVisitors = totalVisitors;
    }

    public void setTotalVisits(int totalVisits) {
        this.totalVisits = totalVisits;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public int getTotalVisitors() {
        return totalVisitors;
    }

    public int getTotalVisits() {
        return totalVisits;
    }
}
