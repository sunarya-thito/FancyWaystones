package thito.fancywaystones.effect;

import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;
import org.mozilla.javascript.*;
import thito.fancywaystones.*;
import thito.fancywaystones.location.*;
import xyz.xenondevs.particle.*;


public class CircleParticle implements Effect {

    private Script script;
    private ParticleEffect particleEffect;
    private double deltaX, deltaY, deltaZ;
    private double offsetX, offsetY, offsetZ;
    private float speed;
    private double radius;
    private boolean global;
    private int count;
    private int gap;
    private int location;

    public CircleParticle(ConfigurationSection section) {
        try {
            particleEffect = ParticleEffect.valueOf(section.getString("Particle"));
        } catch (Throwable t) {
            t.printStackTrace();
            particleEffect = ParticleEffect.VILLAGER_HAPPY;
        }
        global = section.getBoolean("Global");
        deltaX = section.getDouble("Width");
        deltaY = section.getDouble("Height");
        deltaZ = section.getDouble("Length");
        offsetX = section.getDouble("Offset X");
        offsetY = section.getDouble("Offset Y");
        offsetZ = section.getDouble("Offset Z");
        speed = (float) section.getDouble("Speed");
        count = section.getInt("Count", 1);
        radius = section.getDouble("Radius", 1);
        gap = Math.max(1, section.getInt("Gap"));
        String loc = section.getString("Location");
        if (loc != null) {
            if (loc.equalsIgnoreCase("WAYSTONE_TARGET")) {
                location = 2;
            } else if (loc.equalsIgnoreCase("WAYSTONE_SOURCE")) {
                location = 1;
            } else {
                location = 0;
            }
        }
        try {
            script = FancyWaystones.getPlugin().compile(section.getString("Script"));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public EffectHandler createHandler(Player player, WaystoneData waystoneData, WaystoneData target) {
        return new CircleHandler(script, particleEffect, deltaX, deltaY, deltaZ, offsetX, offsetY, offsetZ, speed, radius, count, gap, player, waystoneData, target, global, location);
    }

    public static class CircleHandler implements EffectHandler {
        private Scriptable scriptable;
        public ParticleEffect particleEffect;
        public double deltaX, deltaY, deltaZ;
        public double offsetX, offsetY, offsetZ;
        public int rotateX, rotateY, rotateZ;
        public float speed;
        public double radius;
        public int count;
        public int gap;
        public boolean global;
        public Player player;
        public WaystoneData waystoneData, target;
        public int tick;
        public boolean visible = true;
        private Script script;
        public int location;

        public CircleHandler(Script script, ParticleEffect particleEffect, double deltaX, double deltaY, double deltaZ, double offsetX, double offsetY, double offsetZ, float speed, double radius, int count, int gap, Player player, WaystoneData waystoneData, WaystoneData target, boolean global, int location) {
            scriptable = JavaAdapter.createAdapterWrapper(FancyWaystones.getPlugin().getRoot(), this);
            this.particleEffect = particleEffect;
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.deltaZ = deltaZ;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.speed = speed;
            this.radius = radius;
            this.count = count;
            this.gap = gap;
            this.player = player;
            this.waystoneData = waystoneData;
            this.target = target;
            this.script = script;
            this.global = global;
            this.location = location;
        }

        @Override
        public void tick(int tickTime) {
            tick = tickTime;
            if (visible) {
                Location location = null;
                if (this.location == 0) {
                    location = player.getLocation();
                } else if (this.location == 1) {
                    if (waystoneData != null && waystoneData.getLocation() instanceof LocalLocation) {
                        location = ((LocalLocation) waystoneData.getLocation()).getLocation().clone().add(0.5, 0, 0.5);
                    }
                } else if (this.location == 2) {
                    if (target != null && target.getLocation() instanceof LocalLocation) {
                        location = ((LocalLocation) target.getLocation()).getLocation().clone().add(0.5, 0, 0.5);
                    }
                }
                if (location != null) {
                    for (int i = 0; i < 360; i+=gap) {
                        double sin = Util.sin(i);
                        double cos = Util.cos(i);
                        Location loc = location.clone()
                                .add(offsetX, offsetY, offsetZ)
                                .add(sin * radius, 0, cos * radius);
                        loc = Util.rotate(location.clone(), loc, rotateX, rotateY, rotateZ);
                        if (global) {
                            particleEffect.display(loc,
                                    new Vector(deltaX, deltaY, deltaZ), speed, count,
                                    null,
                                    Bukkit.getOnlinePlayers());
                        } else {
                            particleEffect.display(loc,
                                    new Vector(deltaX, deltaY, deltaZ), speed, count,
                                    null,
                                    player);
                        }
                    }
                }
            }
            if (script != null && scriptable != null) {
                script.exec(FancyWaystones.getPlugin().getContext(), scriptable);
            }
        }
    }
}
