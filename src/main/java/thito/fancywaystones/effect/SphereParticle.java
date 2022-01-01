package thito.fancywaystones.effect;

import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;
import org.mozilla.javascript.*;
import thito.fancywaystones.*;
import xyz.xenondevs.particle.*;


public class SphereParticle implements Effect {

    private ParticleEffect particleEffect;
    private double deltaX, deltaY, deltaZ;
    private double offsetX, offsetY, offsetZ;
    private float speed;
    private double radius;
    private int count;
    private int horizontalGap, verticalGap;
    private boolean global;
    private Script script;

    public SphereParticle(ConfigurationSection section) {
        try {
            particleEffect = ParticleEffect.valueOf(section.getString("Particle"));
        } catch (Throwable t) {
            t.printStackTrace();
            particleEffect = ParticleEffect.VILLAGER_HAPPY;
        }
        deltaX = section.getDouble("Width");
        deltaY = section.getDouble("Height");
        deltaZ = section.getDouble("Length");
        offsetX = section.getDouble("Offset X");
        offsetY = section.getDouble("Offset Y");
        offsetZ = section.getDouble("Offset Z");
        global = section.getBoolean("Global");
        speed = (float) section.getDouble("Speed");
        count = section.getInt("Count", 1);
        radius = section.getDouble("Radius", 1);
        verticalGap = Math.max(1, section.getInt("Vertical Gap"));
        horizontalGap = Math.max(1, section.getInt("Horizontal Gap"));
        try {
            script = Context.enter().compileString(section.getString("Script"), "effects.yml", 0, null);
//            script = FancyWaystones.getPlugin().compile(section.getString("Script"));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public EffectHandler createHandler(Player player, WaystoneData waystoneData, WaystoneData target) {
        return new SphereHandler(script, particleEffect, deltaX, deltaY, deltaZ, offsetX, offsetY, offsetZ, speed, radius,
                count, horizontalGap, verticalGap, player, waystoneData, target, global);
    }

    public static class SphereHandler implements EffectHandler {
        public ParticleEffect particleEffect;
        public double deltaX, deltaY, deltaZ;
        public double offsetX, offsetY, offsetZ;
        public int rotateX, rotateY, rotateZ;
        public float speed;
        public double radius;
        public int count;
        public int horizontalGap, verticalGap;
        public Player player;
        public WaystoneData waystoneData, target;
        public boolean global;
        public int tick;
        public boolean visible = true;
        private Script script;
//        private Scriptable scriptable;

        public SphereHandler(Script script, ParticleEffect particleEffect, double deltaX, double deltaY, double deltaZ, double offsetX, double offsetY, double offsetZ, float speed, double radius, int count, int horizontalGap, int verticalGap, Player player, WaystoneData waystoneData, WaystoneData target, boolean global) {
            this.particleEffect = particleEffect;
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.deltaZ = deltaZ;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.global = global;
            this.offsetZ = offsetZ;
            this.speed = speed;
            this.radius = radius;
            this.count = count;
            this.horizontalGap = horizontalGap;
            this.verticalGap = verticalGap;
            this.player = player;
            this.waystoneData = waystoneData;
            this.target = target;
            this.script = script;
//            scriptable = JavaAdapter.createAdapterWrapper(FancyWaystones.getPlugin().getRoot(), this);
        }

        @Override
        public void tick(int tickTime) {
            tick = tickTime;
            if (visible) {
                Location location = player.getLocation();
                for (int j = 0; j < 360; j+=verticalGap) {
                    double rad = Util.sin(j);
                    double y = Util.cos(j);
                    for (int i = 0; i < 360; i+= horizontalGap) {
                        double sin = Util.sin(i);
                        double cos = Util.cos(i);
                        Location loc = location.clone()
                                .add(offsetX, offsetY, offsetZ)
                                .add(sin * radius * rad, y * radius, cos * radius * rad);
                        loc = Util.rotate(location.clone(), loc, rotateX, rotateY, rotateZ);
                        if (global) {
                            particleEffect.display(loc, new Vector(deltaX, deltaY, deltaZ), speed, count, null, Bukkit.getOnlinePlayers());
                        } else {
                            particleEffect.display(loc, new Vector(deltaX, deltaY, deltaZ), speed, count, null, player);
                        }
                    }
                }
            }
            Context context = Context.enter();
            Scriptable scriptable = JavaAdapter.createAdapterWrapper(context.initSafeStandardObjects(), this);
            if (script != null) {
                script.exec(context, scriptable);
            }
        }
    }
}
