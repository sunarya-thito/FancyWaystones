package thito.fancywaystones;

import org.bukkit.entity.Entity;
import thito.fancywaystones.reflection.ClassFinder;
import thito.fancywaystones.reflection.ConstructorFinder;
import thito.fancywaystones.reflection.MethodFinder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class EntityStream {
    public static final MethodFinder SAVE =
            MethodFinder.find(new MethodFinder("{nms}.Entity", "f", "NBTTagCompound").simpleParameter(),
                    new MethodFinder("{nms}.Entity", "save", "NBTTagCompound"));
    public static final MethodFinder LOAD =
            MethodFinder.find(new MethodFinder("{nms}.Entity", "e", "NBTTagCompound").simpleParameter(),
                    new MethodFinder("{nms}.Entity", "load", "NBTTagCompound").simpleParameter());
    public static final MethodFinder LOAD_NBT =
            MethodFinder.find(new MethodFinder(ClassFinder.find("{nms}.NBTCompressedStreamTools", "{nms}.nbt.NBTCompressedStreamTools"), "a", InputStream.class));
    public static final MethodFinder SAVE_NBT =
            MethodFinder.find(new MethodFinder(ClassFinder.find("{nms}.NBTCompressedStreamTools", "{nms}.nbt.NBTCompressedStreamTools"), "a", "NBTTagCompound", "OutputStream").simpleParameter());
    public static final MethodFinder GET_HANDLE =
            MethodFinder.find(new MethodFinder("{cb}.entity.CraftEntity", "getHandle"));
    public static final ConstructorFinder NEW_NBTTAGCOMPOUND =
            ConstructorFinder.find(new ConstructorFinder(ClassFinder.find("{nms}.NBTTagCompound", "{nms}.nbt.NBTTagCompound")));
    public static byte[] serializeEntity(Entity entity) {
        Object nbt = SAVE.invoke(GET_HANDLE.invoke(entity), NEW_NBTTAGCOMPOUND.newInstance());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        SAVE_NBT.invoke(null, nbt, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static void deserializeEntity(Entity entity, byte[] data) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        LOAD.invoke(GET_HANDLE.invoke(entity), LOAD_NBT.invoke(null, byteArrayInputStream));
    }
}
