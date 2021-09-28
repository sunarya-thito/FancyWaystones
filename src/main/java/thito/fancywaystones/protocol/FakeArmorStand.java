package thito.fancywaystones.protocol;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.utility.*;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;
import org.bukkit.util.Vector;
import thito.fancywaystones.*;
import thito.fancywaystones.Util;
import thito.fancywaystones.model.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.function.Consumer;

public class FakeArmorStand {
    private static int dataWatcherIndex;
    private static int availableIds = Integer.MAX_VALUE / 2;
    private static int customNameMode;
    private static boolean customNameOptional;
    private static int requestId() {
        return availableIds--;
    }

    static {
        dataWatcherIndex = 10;
        int version = XMaterial.getVersion();
        if (version >= 9) {
            Class<?> DataWatcherObject = null;
            Class<?> EntityArmorStand = null;
            Class<?> Entity = null;
            if (version >= 17) {
                try {
                    DataWatcherObject = Class.forName("net.minecraft.network.syncher.DataWatcherObject");
                    EntityArmorStand = Class.forName("net.minecraft.world.entity.decoration.EntityArmorStand");
                    Entity = Class.forName("net.minecraft.world.entity.Entity");
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else {
                try {
                    DataWatcherObject = Util.getNMSClass("DataWatcherObject");
                    EntityArmorStand = Util.getNMSClass("EntityArmorStand");
                    Entity = Util.getNMSClass("Entity");
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            try {
                Field field2 = null;
                for (Field field3 : DataWatcherObject.getDeclaredFields()) {
                    if (field3.getType() == int.class) {
                        field2 = field3;
                        field2.setAccessible(true);
                        break;
                    }
                }
                for (Field field : EntityArmorStand.getDeclaredFields()) {
                    if (field.getType() == DataWatcherObject) {
                        field.setAccessible(true);
                        // first data watcher
                        Object DataWatcherObjectInstance = field.get(null);
                        dataWatcherIndex = field2.getInt(DataWatcherObjectInstance);
                        break;
                    }
                }
                if (version >= 17) {
                    customNameMode = 1;
                    customNameOptional = true;
                } else {
                    int index = 0;
                    for (Field field : Entity.getDeclaredFields()) {
                        if (field.getType() == DataWatcherObject) {
                            if (index == 2) {
                                field.setAccessible(true);
                                Type handleType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                                if (handleType instanceof ParameterizedType) {
                                    handleType = ((ParameterizedType) handleType).getActualTypeArguments()[0];
                                    customNameOptional = true;
                                }
                                if (handleType instanceof Class) {
                                    if (handleType == MinecraftReflection.getIChatBaseComponentClass()) {
                                        customNameMode = 1;
                                    }
                                }
                                break;
                            }
                            index++;
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private int entityId;
    private UUID entityUniqueId;
    private World world;
    private double x, y, z;
    private Function<Player, ArmorStandMeta> metaFactory;
    private BiConsumer<Player, Consumer<ArmorStandMeta>> asyncMetaFactory;
    private Set<Player> viewers = Collections.newSetFromMap(new WeakHashMap<>());
    private boolean lockMarker;
    private Task tickerTask;

    public FakeArmorStand(Location location) {
        world = location.getWorld();
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        entityId = requestId();
        entityUniqueId = UUID.randomUUID();
        tickerTask = new Task() {
            @Override
            public void run() {
                try {
                    tick();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        };
    }

    public void spawn() {
        tickerTask.schedule(FancyWaystones.getPlugin().getService(), 1L, 1L);
    }

    public void remove() {
        tickerTask.cancel();
        viewers.forEach(this::destroyFrom);
    }

    void tick() {
        double viewDistance = Bukkit.getViewDistance() * 16;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() == world) {
                Location location = player.getLocation();
                double distance = Math.sqrt(
                        Math.pow(location.getX() - x, 2) +
                                Math.pow(location.getY() - y, 2) +
                                Math.pow(location.getZ() - z, 2));
                if (distance < viewDistance && !player.isDead() && player.getTicksLived() > 20) {
                    if (viewers.add(player)) {
                        sendTo(player);
                        update(player);
                    }
                    testHitBox(player);
                } else {
                    markerOff.remove(player);
                    if (viewers.remove(player)) {
                        destroyFrom(player);
                    }
                }
            } else {
                markerOff.remove(player);
                if (viewers.remove(player)) {
                    destroyFrom(player);
                }
            }
        }
        markerOff.removeIf(x -> !x.isOnline());
        viewers.removeIf(x -> !x.isOnline());
    }

    private Set<Player> markerOff = new LinkedHashSet<>();
    private void testHitBox(Player player) {
        Location eyeLocation = player.getEyeLocation();
        Vector location = new Vector(x, y, z);
        double distance = Math.sqrt(
                Math.pow(eyeLocation.getX() - x, 2) +
                        Math.pow(eyeLocation.getZ() - z, 2));
        Vector look = eyeLocation.getDirection().normalize();
        Vector direction = location.subtract(eyeLocation.toVector()).normalize();
        double length = look.getY() - direction.getY();
//        a = length / distance;
        if (length / distance < 0.4 / distance) {
            if (markerOff.remove(player)) {
                update(player);
            }
        } else {
            if (!lockMarker && markerOff.add(player)) {
                update(player);
            }
        }
    }

//    private double a, b;

    private void sendTo(Player player) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        packet.getIntegers().write(0, entityId);
        if (packet.getEntityTypeModifier().size() > 0) {
            packet.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
        } else {
            packet.getIntegers().write(packet.getIntegers().size() - 2, 78);
        }
        if (packet.getUUIDs().size() > 0) {
            packet.getUUIDs().write(0, entityUniqueId);
        }
        if (packet.getDoubles().size() > 0) {
            packet.getDoubles().write(0, x)
                    .write(1, y)
                    .write(2, z);
        } else {
            packet.getIntegers().write(1, (int) Math.floor(x * 32))
                .write(2, (int) Math.floor(y * 32))
                .write(3, (int) Math.floor(z * 32));
        }
        Util.packet(player, packet);
    }

    private void destroyFrom(Player player) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        if (packet.getIntegerArrays().size() > 0) {
            packet.getIntegerArrays().write(0, new int[] {entityId});
        } else {
            packet.getIntLists().write(0, new ArrayList<>(Collections.singletonList(entityId)));
        }
        Util.packet(player, packet);
    }

    static boolean legacy;
    static <T> void writeRegistry(WrappedDataWatcher watcher, Class<T> type, int index, T value) {
        try {
            if (!legacy) {
                WrappedDataWatcher.WrappedDataWatcherObject object = new WrappedDataWatcher.WrappedDataWatcherObject(index, WrappedDataWatcher.Registry.get(type));
                watcher.setObject(object, value);
                return;
            }
        } catch (Throwable t) {
            legacy = true;
        }
        if (value instanceof Boolean) {
            watcher.setObject(index, (Boolean) value ? (byte)1 : (byte)0);
        } else {
            watcher.setObject(index, value);
        }
    }

    static void writeVector(WrappedDataWatcher watcher, int index, EulerAngle pose) {
        try {
            if (!legacy) {
                WrappedDataWatcher.WrappedDataWatcherObject object = new WrappedDataWatcher.WrappedDataWatcherObject(index, WrappedDataWatcher.Registry.getVectorSerializer());
                watcher.setObject(object, new Vector3F((float) Math.toDegrees(pose.getX()), (float) Math.toDegrees(pose.getY()), (float) Math.toDegrees(pose.getZ())));
                return;
            }
        } catch (Throwable t) {
            legacy = true;
        }
        watcher.setObject(index, new Vector3F((float) Math.toDegrees(pose.getX()), (float) Math.toDegrees(pose.getY()), (float) Math.toDegrees(pose.getZ())));
    }

    public void updateMetadata(Player player, ArmorStandMeta meta) {
        if (!lockMarker) {
            meta.setMarker(!markerOff.contains(player));
//            meta.setCustomName(""+meta.isMarker()+":"+a+":"+b);
//            meta.setCustomNameVisible(true);
        }
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entityId);

        WrappedDataWatcher watcher = new WrappedDataWatcher();

        byte flags = 0;
        if (meta.isInvisible()) {
            flags |= 0x20;
        }

        writeRegistry(watcher, Byte.class, 0, flags);

        writeRegistry(watcher, Boolean.class, 3, meta.isCustomNameVisible());

        String customName = meta.getCustomName();
        if (customNameMode == 0) {
            if (customNameOptional) {
                WrappedDataWatcher.WrappedDataWatcherObject customNameWatcher = new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.get(String.class, true));
                watcher.setObject(customNameWatcher, Optional.ofNullable(customName));
            } else {
                watcher.setObject(2, customName == null ? "" : customName);
            }
        } else if (customNameMode == 1) {
            if (customNameOptional) {
                WrappedDataWatcher.WrappedDataWatcherObject customNameWatcher = new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true));
                watcher.setObject(customNameWatcher, Optional.ofNullable(customName == null ? null : WrappedChatComponent.fromChatMessage(customName)[0].getHandle()));
            } else {
                WrappedDataWatcher.WrappedDataWatcherObject customNameWatcher = new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer());
                watcher.setObject(customNameWatcher, customName == null ? null : WrappedChatComponent.fromChatMessage(customName)[0].getHandle());
            }
        }

        byte armorStandFlags = 0;
        if (meta.isSmall()) {
            armorStandFlags |= 0x01;
        }
        if (meta.isArms()) {
            armorStandFlags |= 0x04;
        }
        if (meta.isNoBasePlate()) {
            armorStandFlags |= 0x08;
        }
        if (meta.isMarker() && ClientSideStandardModel.supportsMarker) {
            armorStandFlags |= 0x10;
        }

        int startIndex = dataWatcherIndex;

        writeRegistry(watcher, Byte.class, startIndex++, armorStandFlags);

        EulerAngle pose = meta.getHeadPose();
        writeVector(watcher, startIndex++, pose);

        pose = meta.getBodyPose();
        writeVector(watcher, startIndex++, pose);

        pose = meta.getLeftArmPose();
        writeVector(watcher, startIndex++, pose);

        pose = meta.getRightArmPose();
        writeVector(watcher, startIndex++, pose);

        pose = meta.getLeftLegPose();
        writeVector(watcher, startIndex++, pose);

        pose = meta.getRightLegPose();
        writeVector(watcher, startIndex+1, pose);

        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        Util.packet(player, packet);
    }

    public void updateEquipment(Player player, ArmorStandMeta meta) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet.getIntegers().write(0, entityId);
        if (packet.getIntegers().size() == 2) {
            // Main Hand
            packet.getIntegers().write(1, 0);
            packet.getItemModifier().write(0, meta.getMainHand());
            Util.packet(player, packet);
            // Boots
            packet.getIntegers().write(1, 1);
            packet.getItemModifier().write(0, meta.getBoots());
            Util.packet(player, packet);
            // Leggings
            packet.getIntegers().write(1, 2);
            packet.getItemModifier().write(0, meta.getLeggings());
            Util.packet(player, packet);
            // Chestplate
            packet.getIntegers().write(1, 3);
            packet.getItemModifier().write(0, meta.getChestplate());
            Util.packet(player, packet);
            // Head
            packet.getIntegers().write(1, 4);
            packet.getItemModifier().write(0, meta.getHelmet());
            Util.packet(player, packet);
        } else if (packet.getItemSlots().size() == 1) {
            packet.getItemSlots().write(0, EnumWrappers.ItemSlot.MAINHAND);
            packet.getItemModifier().write(0, meta.getMainHand());
            Util.packet(player, packet);

            packet.getItemSlots().write(0, EnumWrappers.ItemSlot.OFFHAND);
            packet.getItemModifier().write(0, meta.getOffHand());
            Util.packet(player, packet);

            packet.getItemSlots().write(0, EnumWrappers.ItemSlot.FEET);
            packet.getItemModifier().write(0, meta.getBoots());
            Util.packet(player, packet);

            packet.getItemSlots().write(0, EnumWrappers.ItemSlot.LEGS);
            packet.getItemModifier().write(0, meta.getLeggings());
            Util.packet(player, packet);

            packet.getItemSlots().write(0, EnumWrappers.ItemSlot.CHEST);
            packet.getItemModifier().write(0, meta.getChestplate());
            Util.packet(player, packet);

            packet.getItemSlots().write(0, EnumWrappers.ItemSlot.HEAD);
            packet.getItemModifier().write(0, meta.getHelmet());
            Util.packet(player, packet);
        } else if (packet.getSlotStackPairLists().size() == 1) {
            packet.getSlotStackPairLists().write(0, new ArrayList<>(
                    Arrays.asList(
                            new Pair<>(EnumWrappers.ItemSlot.HEAD, meta.getHelmet()),
                            new Pair<>(EnumWrappers.ItemSlot.CHEST, meta.getChestplate()),
                            new Pair<>(EnumWrappers.ItemSlot.LEGS, meta.getLeggings()),
                            new Pair<>(EnumWrappers.ItemSlot.FEET, meta.getBoots()),
                            new Pair<>(EnumWrappers.ItemSlot.MAINHAND, meta.getMainHand()),
                            new Pair<>(EnumWrappers.ItemSlot.OFFHAND, meta.getOffHand()))
            ));
            Util.packet(player, packet);
        }
    }

    public void update(Player player, ArmorStandMeta meta) {
        updateMetadata(player, meta);
        updateEquipment(player, meta);
    }

    public void update(Player player) {
        if (metaFactory != null) {
            ArmorStandMeta meta = metaFactory.apply(player);
            if (meta != null) {
                update(player, meta);
            }
        }
        if (asyncMetaFactory != null) {
            asyncMetaFactory.accept(player, meta -> {
                if (meta != null) {
                    update(player, meta);
                }
            });
            return;
        }
    }

    public void setAsyncMetaFactory(BiConsumer<Player, Consumer<ArmorStandMeta>> asyncMetaFactory) {
        this.asyncMetaFactory = asyncMetaFactory;
    }

    public BiConsumer<Player, Consumer<ArmorStandMeta>> getAsyncMetaFactory() {
        return asyncMetaFactory;
    }

    public void setMetaFactory(Function<Player, ArmorStandMeta> metaFactory) {
        this.metaFactory = metaFactory;
    }

    public Function<Player, ArmorStandMeta> getMetaFactory() {
        return metaFactory;
    }

    public boolean isLockMarker() {
        return lockMarker;
    }

    public void setLockMarker(boolean lockMarker) {
        this.lockMarker = lockMarker;
    }
}
