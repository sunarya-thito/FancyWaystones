package thito.fancywaystones;

import org.bukkit.configuration.*;

public class Language {
    private ConfigurationSection section;

    public Language(ConfigurationSection section) {
        this.section = section;
    }

    public String get(String... s) {
        String s1 = String.join(".", s);
        return section.getString(s1, "{unknown_language:"+ s1 +"}");
    }

    public static Language getLanguage() {
        return FancyWaystones.getPlugin().getLanguage();
    }
}
