package thito.fancywaystones;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import thito.fancywaystones.protocol.FakeArmorStand;

import java.io.*;
import java.util.Base64;

public class DataDumper {
    public static void dump(Writer writer) throws IOException {
        PrintWriter printWriter = new PrintWriter(writer);
        printWriter.println("FancyWaystones Dump File");
        FancyWaystones fancyWaystones = FancyWaystones.getPlugin();
        printWriter.println(fancyWaystones.getDescription().getVersion());
        WaystoneManager manager = WaystoneManager.getManager();
        WaystoneStorage storage = manager.getStorage();
        if (storage == null) {
            printWriter.println("Storage: NULL");
        } else {
            printWriter.println("Storage: "+storage);
        }
        printWriter.println("Fake Armor Stand "+ FakeArmorStand.availableIds+" "+FakeArmorStand.customNameMode+" "+FakeArmorStand.customNameOptional+" "+FakeArmorStand.dataWatcherIndex);
        printWriter.println("There is "+manager.getLoadedCount()+" waystones loaded");
        printWriter.println();
        printWriter.println("Waystone Model List:");
        for (WaystoneModel waystoneModel : manager.getModelMap().values()) {
            printWriter.println("- "+waystoneModel.getName()+" (" + waystoneModel.getId()+") "+waystoneModel);
        }
        printWriter.println();
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        if (plugin == null) {
            printWriter.println("ProtocolLib: Not Installed");
        } else {
            printWriter.println("ProtocolLib: "+plugin.getDescription().getVersion()+" by "+String.join(", ", plugin.getDescription().getAuthors()));
        }
        File df = fancyWaystones.getDataFolder();
        File[] dumped = {new File(df, "config.yml"), new File(df, "effects.yml"), new File(df, "books.yml"),
                        new File(df, "gui.yml"), new File(df, "messages.yml"), new File(df, "models.yml"),
                        new File(df, "recipes.yml"), new File(df, "structures.yml"), new File(df, "waystones.yml")};
        printWriter.println("File Configuration:");
        for (File d : dumped) {
            try (FileInputStream fileInputStream = new FileInputStream(d)) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int len;
                byte[] buff = new byte[1024 * 8];
                while ((len = fileInputStream.read(buff, 0, buff.length)) != -1) {
                    bos.write(buff, 0, len);
                }
                printWriter.println(":"+d.getPath());
                printWriter.println(Base64.getEncoder().encodeToString(bos.toByteArray()));
                printWriter.println();
            } catch (IOException t) {
                printWriter.println("Cant read "+d.getPath());
                t.printStackTrace(printWriter);
            }
        }
    }
}
