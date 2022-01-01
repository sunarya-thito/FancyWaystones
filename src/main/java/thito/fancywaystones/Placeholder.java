package thito.fancywaystones;

import org.bukkit.*;
import org.bukkit.entity.*;
import thito.fancywaystones.economy.*;
import thito.fancywaystones.location.*;
import thito.fancywaystones.ui.*;

import java.util.*;
import java.util.stream.*;

public class Placeholder {

    public static final VariableContent<WaystoneMember> MEMBER = new VariableContent<>(WaystoneMember.class);
    public static final VariableContent<Player> VIEWER = new VariableContent<>(Player.class);
    public static final VariableContent<Player> PLAYER = new VariableContent<>(Player.class);
    public static final VariableContent<WaystoneData> WAYSTONE = new VariableContent<>(WaystoneData.class);
    public static final VariableContent<WaystoneData> SOURCE_WAYSTONE = new VariableContent<>(WaystoneData.class);
    public static final VariableContent<ItemSort> SORT = new VariableContent<>(ItemSort.class);

    public static final Map<String, Variable> DEFAULT_VARIABLES = new HashMap<>();

    public static final Placeholder EMPTY = REPLACE_NOTHING();
    private static Placeholder REPLACE_NOTHING() {
        return new Placeholder() {

            @Override
            public List<String> replace(List<String> list) {
                return list;
            }

            @Override
            public List<String> replaceWithBreakableLines(List<String> list) {
                return list;
            }

            @Override
            public String replace(String s) {
                return s;
            }
        };
    }

    static {
        DEFAULT_VARIABLES.put("type", placeholder -> placeholder.get(WAYSTONE).getType().name());
        DEFAULT_VARIABLES.put("environment", placeholder -> placeholder.get(WAYSTONE).getEnvironment().name());
        DEFAULT_VARIABLES.put("model", placeholder -> placeholder.get(WAYSTONE).getModel().getName());
        DEFAULT_VARIABLES.put("player_name", placeholder -> placeholder.get(PLAYER).getName());
        DEFAULT_VARIABLES.put("player_name_uppercase", placeholder -> placeholder.get(PLAYER).getName().toUpperCase());
        DEFAULT_VARIABLES.put("player_display_name", placeholder -> {
            Player player = placeholder.get(PLAYER);
            return player.getDisplayName();
        });

        DEFAULT_VARIABLES.put("viewer_name", placeholder -> placeholder.get(VIEWER).getName());
        DEFAULT_VARIABLES.put("viewer_name_uppercase", placeholder -> placeholder.get(VIEWER).getName().toUpperCase());
        DEFAULT_VARIABLES.put("viewer_display_name", placeholder -> {
            Player player = placeholder.get(VIEWER);
            return player.getDisplayName();
        });

        DEFAULT_VARIABLES.put("waystone_member_name", p -> p.get(MEMBER).getName());
        DEFAULT_VARIABLES.put("waystone_member_uuid", p -> p.get(MEMBER).getUUID());

        DEFAULT_VARIABLES.put("waystone_name", placeholder -> placeholder.get(WAYSTONE).getName());
//        DEFAULT_VARIABLES.put("waystone_world_name", placeholder -> {
//            WaystoneLocation location = placeholder.get(WAYSTONE).getLocation();
//            if (location instanceof LocalLocation) {
//                return ((LocalLocation) location).getLocation().getWorld().getName();
//            }
//            if (location instanceof ProxyLocation) {
//                return location.getWorldUUID().toString();
//            }
//            return "?";
//        });
        DEFAULT_VARIABLES.put("waystone_x", p -> p.get(WAYSTONE).getLocation().getBlockX());
        DEFAULT_VARIABLES.put("waystone_y", p -> p.get(WAYSTONE).getLocation().getBlockY());
        DEFAULT_VARIABLES.put("waystone_z", p -> p.get(WAYSTONE).getLocation().getBlockZ());
        DEFAULT_VARIABLES.put("waystone_name_uppercase", p -> p.get(WAYSTONE).getName().toUpperCase());
        DEFAULT_VARIABLES.put("waystone_owner_name", p -> p.get(WAYSTONE).getOwnerName());
        DEFAULT_VARIABLES.put("waystone_owner_name_uppercase", p -> {
            String ownerName = p.get(WAYSTONE).getOwnerName();
            return ownerName == null ? null : ownerName.toUpperCase();
        });
        DEFAULT_VARIABLES.put("waystone_owner_uuid",  p -> p.get(WAYSTONE).getOwnerUUID());
        DEFAULT_VARIABLES.put("waystone_type_display_name", p -> p.get(WAYSTONE).getType().getDisplayName(p));
        DEFAULT_VARIABLES.put("waystone_type_name", p -> p.get(WAYSTONE).getType().name());
        DEFAULT_VARIABLES.put("waystone_environment", p -> ItemEconomyService.capitalizeFully(p.get(WAYSTONE).getEnvironment().name().replace('_', ' ')));
        DEFAULT_VARIABLES.put("waystone_teleportation_cost", p -> {
            List<Cost> cost = p.get(WAYSTONE).getType().calculateCost(new LocalLocation(p.get(PLAYER).getLocation()), p.get(WAYSTONE));
            return cost.isEmpty() ? "{language.free}" : cost.stream().map(x -> x.getService().formattedCurrency(x.getAmount())).collect(Collectors.joining(" {language.and} "));
        });
        DEFAULT_VARIABLES.put("waystone_statistics_visitors", p -> p.get(WAYSTONE).getStatistics().getTotalVisitors());
        DEFAULT_VARIABLES.put("waystone_statistics_visits", p -> p.get(WAYSTONE).getStatistics().getTotalVisits());
        DEFAULT_VARIABLES.put("waystone_statistics_users", p -> p.get(WAYSTONE).getStatistics().getTotalUsers());
        DEFAULT_VARIABLES.put("waystone_statistics_age", p -> {
            long date = System.currentTimeMillis() - p.get(WAYSTONE).getStatistics().getDateCreated();
            String format = "";
            long seconds = (date / 1000) % 60;
            long minutes = (date / (1000 * 60)) % 60;
            long hours = (date / (1000 * 60 * 60)) % 60;
            long days = (date / (1000 * 60 * 60 * 24));
            if (days > 0) {
                format += days+" {language.time.days}";
            }
            if (hours > 0) {
                if (!format.isEmpty()) format += " ";
                format += hours+" {language.time.hours}";
            }
            if (minutes > 0) {
                if (!format.isEmpty()) format += " ";
                format += minutes+" {language.time.minutes}";
            }
            if (seconds > 0) {
                if (!format.isEmpty()) format += " ";
                format += seconds+" {language.time.seconds}";
            }
            return format;
        });
        DEFAULT_VARIABLES.put("waystone_statistics_date_created", p -> {
            long date = p.get(WAYSTONE).getStatistics().getDateCreated();
            return formatTime(p, date);
        });
        DEFAULT_VARIABLES.put("order_ascend", p -> p.get(SORT).getAscendMessage());
        DEFAULT_VARIABLES.put("order_descend", p -> p.get(SORT).getDescendMessage());
        DEFAULT_VARIABLES.put("waystone_type", p -> p.get(WAYSTONE).getType().getDisplayName(p));
        DEFAULT_VARIABLES.put("waystone_model", p -> {
            WaystoneData waystoneData = p.get(WAYSTONE);
            return waystoneData.getModel() == null ? WaystoneManager.getManager().getDefaultModel().getName() : waystoneData.getModel().getName();
        });
    }

    private static String formatTime(Placeholder p, long date) {
        String format = p.replace("{language.date-format}");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        String day = "?";
        switch (weekday) {
            case Calendar.MONDAY:
                day = "{language.day-name.monday}";
                break;
            case Calendar.TUESDAY:
                day = "{language.day-name.tuesday}";
                break;
            case Calendar.WEDNESDAY:
                day = "{language.day-name.wednesday}";
                break;
            case Calendar.THURSDAY:
                day = "{language.day-name.thursday}";
                break;
            case Calendar.FRIDAY:
                day = "{language.day-name.friday}";
                break;
            case Calendar.SATURDAY:
                day = "{language.day-name.saturday}";
                break;
            case Calendar.SUNDAY:
                day = "{language.day-name.sunday}";
                break;
        }
        format = format.replace("{dd}", String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)))
        .replace("{MM}", String.valueOf(calendar.get(Calendar.MONTH) + 1))
        .replace("{yyyy}", String.valueOf(calendar.get(Calendar.YEAR)))
        .replace("{day}", day);
        return format;
    }

    private Map<VariableContent, Object> contents = new HashMap<>();
    private Map<String, Variable> variables = new HashMap<>(DEFAULT_VARIABLES);

    public Placeholder clone() {
        return new Placeholder().putAll(variables).putAllContent(contents);
    }

    public <T> Placeholder putContent(VariableContent<T> content, T value) {
        if (value != null) {
            contents.put(content, value);
        } else {
            contents.remove(content);
        }
        return this;
    }

    public boolean hasContent(VariableContent<?> content) {
        return contents.containsKey(content);
    }
    public Placeholder putAllContent(Map<VariableContent, Object> variables) {
        contents.putAll(variables);
        return this;
    }
    public Placeholder putAll(Map<String, Variable> variables) {
        this.variables.putAll(variables);
        return this;
    }

    public Placeholder combine(Placeholder placeholder) {
        contents.putAll(placeholder.contents);
        variables.putAll(placeholder.variables);
        return this;
    }

    public Placeholder put(String variableName, Variable variable) {
        variables.put(variableName, variable);
        return this;
    }

    public <T> T get(VariableContent<T> variableContent) {
        return (T) contents.get(variableContent);
    }

    public List<String> replace(List<String> list) {
        if (list == null) return null;
        List<String> newList = new ArrayList<>();
        for (String s : list) {
            newList.add(replace(s));
        }
        return newList;
    }

    public List<String> replaceWithBreakableLines(List<String> list) {
        if (list == null) return null;
        List<String> newList = new ArrayList<>();
        for (String s : list) {
            newList.addAll(replaceWithNewLines(s));
        }
        return newList;
    }

    public List<String> replaceWithNewLines(String s) {
        if (s == null) return null;
        List<String> list = new ArrayList<>();
        for (String x : s.split("\n")) {
            list.add(replace(x));
        }
        return list;
    }

    public String replace(String s) {
        //replace(s, 0)
        s = replace(s, 0);
        if (PlaceholderAPISupport.enableSupport && s != null) {
            s = PlaceholderAPISupport.attemptReplace(this, s);
        }
        return s;
    }

    private String replace(String s, int iterations) {
        if (s == null) return null;
        s = ChatColor.translateAlternateColorCodes('&', s);
        StringBuilder builder = new StringBuilder(s.length());
        int last = 0;
        int index;
        int endIndex;
        while ((index = s.indexOf('{', last)) != -1 && (endIndex = s.indexOf('}', index)) != -1) {
            builder.append(s, last, index);
            String key = s.substring(index + 1, endIndex);
            if (key.startsWith("language.")) {
                key = key.substring(9);
                String lang = Language.getLanguage().get(key);
                if (lang != null) {
                    builder.append(ChatColor.translateAlternateColorCodes('&', lang));
                } else {
                    builder.append("{").append(key).append("}");
                }
            } else {
                Variable var = variables.get(key);
                if (var != null) {
                    try {
                        builder.append(var.get(this));
                    } catch (Throwable t) {
                        builder.append("{").append(key).append("}");
                    }
                } else {
                    builder.append("{"+key+"}");
                }
            }
            last = endIndex + 1;
        }
        builder.append(s, last, s.length());
        String result = builder.toString();
        if (result.contains("{") && result.contains("}") && iterations < 10) {
            return replace(result, iterations+1);
        }
        return result;
    }

}
