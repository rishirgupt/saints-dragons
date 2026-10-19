package com.leon.saintsdragons.gametest;

import com.leon.saintsdragons.server.entity.base.DragonEntity;
import com.leon.saintsdragons.server.entity.dragons.raevyx.Raevyx;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dev-only scripted client session: spawns dragons, drives them and saves screenshots plus a state log,
 * so the port can be checked without a person at the keyboard.
 *
 * -Dsd.script is a ';'-separated list of steps:
 *   summon:<species>  tame  ride  golem:<z>  mouse:<ticks>  wait:<ticks>  state:<label>  shot:<name>
 *   hold:<key>  release:<key>  press:<key>  cam:<0|1|2>  look:<yaw>:<pitch>  cmd:<command>
 */
public class DragonClientTest implements FabricClientGameTest {
    private static final Logger LOG = LoggerFactory.getLogger("sd-test");

    @Override
    public void runTest(ClientGameTestContext context) {
        String script = System.getProperty("sd.script", "summon:raevyx;wait:20;shot:idle");
        java.nio.file.Path scriptFile = java.nio.file.Path.of("..", "sd-script.txt");
        if (java.nio.file.Files.exists(scriptFile)) {
            try {
                script = java.nio.file.Files.readString(scriptFile).trim();
            } catch (java.io.IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            TestServerContext server = world.getServer();
            world.getConnection().waitForChunksRender();
            server.runOnServer(s -> player(s).setGameMode(GameType.CREATIVE));
            server.runCommand("tp @p 0.5 -60 0.5 0 10");
            server.runCommand("time set noon");
            context.waitTicks(20);
            for (String step : script.split(";")) {
                run(context, server, step.trim());
            }
            // The throwaway world is not worth closing: with many mods loaded the framework's world close can
            // deadlock against the server thread and leave a frozen window. End the game right here.
            LOG.info("SCRIPT DONE");
            org.apache.logging.log4j.LogManager.shutdown();
            Runtime.getRuntime().halt(0);
        }
    }

    private void run(ClientGameTestContext context, TestServerContext server, String step) {
        if (step.isEmpty()) {
            return;
        }
        String[] p = step.split(":");
        switch (p[0]) {
            case "summon" -> {
                server.runCommand("summon saintsdragons:" + p[1] + " 0.5 -60 6" + (p.length > 2 ? " " + p[2] : ""));
                context.waitTicks(10);
            }
            case "tame" -> server.runOnServer(s -> {
                for (DragonEntity dragon : dragons(s)) {
                    dragon.tame(player(s));
                }
            });
            case "ride" -> server.runOnServer(s -> {
                DragonEntity dragon = dragons(s).get(0);
                dragon.setYRot(0);
                dragon.setYBodyRot(0);
                LOG.info("RIDING ok={}", player(s).startRiding(dragon, true, true));
            });
            case "dismount" -> server.runOnServer(s -> player(s).stopRiding());
            case "golem" -> server.runCommand("summon minecraft:" + (p.length > 2 ? p[2] : "husk") + " 0.5 -60 " + p[1] + " {NoAI:1b,PersistenceRequired:1b}");
            case "mouse" -> context.getInput().holdMouseFor(0, Integer.parseInt(p[1]));
            case "wait" -> context.waitTicks(Integer.parseInt(p[1]));
            case "state" -> state(server, p[1]);
            case "shot" -> context.takeScreenshot(p[1]);
            case "hold" -> context.getInput().holdKey(key(p[1]));
            case "release" -> context.getInput().releaseKey(key(p[1]));
            case "press" -> context.getInput().holdKeyFor(key(p[1]), 3);
            case "cam" -> context.runOnClient(mc -> mc.options.setCameraType(CameraType.values()[Integer.parseInt(p[1])]));
            case "look" -> context.getInput().lookAt(Float.parseFloat(p[1]), Float.parseFloat(p[2]));
            case "probe" -> server.runOnServer(s -> {
                DragonEntity dragon = dragons(s).get(0);
                for (Entity entity : dragon.level().getEntities(dragon, dragon.getBoundingBox().inflate(30))) {
                    if (entity instanceof LivingEntity living && !(entity instanceof ServerPlayer)) {
                        float before = living.getHealth();
                        boolean ok = living.hurtOrSimulate(dragon.level().damageSources().mobAttack(dragon), 5.0F);
                        LOG.info("PROBE {} ally={} attackable={} invuln={} hurt={} hp {} -> {} bodyRot={} dist={}", entity.getType().toShortString(), dragon.isAlly(entity), living.attackable(), living.isInvulnerable(), ok, before, living.getHealth(), dragon.yBodyRot, dragon.distanceTo(entity));
                    }
                }
            });
            case "ability" -> server.runOnServer(s -> {
                DragonEntity dragon = dragons(s).get(0);
                for (Entity entity : dragon.level().getEntities(dragon, dragon.getBoundingBox().inflate(40))) {
                    if (entity instanceof net.minecraft.world.entity.monster.Monster monster) {
                        dragon.setTarget(monster);
                    }
                }
                dragon.combatManager.forceUseAbility(com.leon.saintsdragons.common.registry.AbilityRegistry.get(p[1]));
                LOG.info("ABILITY {} active={}", p[1], dragon.combatManager.getActiveAbility());
            });
            case "noai" -> server.runOnServer(s -> dragons(s).forEach(d -> { d.setYRot(0); d.setYBodyRot(0); d.setYHeadRot(0); }));
            case "clear" -> server.runOnServer(s -> {
                for (ServerLevel level : s.getAllLevels()) {
                    List<Entity> all = new ArrayList<>();
                    level.getAllEntities().forEach(all::add);
                    for (Entity entity : all) {
                        if (!(entity instanceof ServerPlayer)) {
                            entity.discard();
                        }
                    }
                }
            });
            case "camlog" -> context.runOnClient(mc -> {
                if (mc.player == null) return;
                Entity vehicle = mc.player.getVehicle();
                LOG.info("CAMLOG[{}] cam={} player={} vehicle={} detached={} xRot={} entities={}", p[1], mc.gameRenderer.mainCamera().position(), mc.player.position(), vehicle == null ? null : vehicle.position(), mc.gameRenderer.mainCamera().isDetached(), mc.gameRenderer.mainCamera().xRot(), mc.level.getEntityCount());
            });
            case "anim" -> context.runOnClient(mc -> {
                if (mc.level == null) return;
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity instanceof com.geckolib.animatable.GeoEntity geo && !(entity instanceof net.minecraft.world.entity.player.Player)) {
                        StringBuilder sb = new StringBuilder();
                        var manager = geo.getAnimatableInstanceCache().getManagerForId(entity.getId());
                        for (var controller : manager.getAnimationControllers().values()) {
                            var point = controller.getCurrentAnimationPoint();
                            sb.append(controller.getName()).append("=").append(controller.getPlayState()).append(":")
                                    .append(point == null ? "-" : point.animation().name()).append(" ");
                        }
                        if (entity instanceof com.leon.saintsdragons.server.entity.base.RideableDragonBase rb) sb.append(" ground=").append(rb.getGroundMoveState()).append(" walking=").append(rb.isWalking()).append(" running=").append(rb.isRunning()).append(" vehicle=").append(rb.isVehicle()).append(" triggered=").append(manager.getAnimationControllers().get("movement") != null && manager.getAnimationControllers().get("movement").isPlayingTriggeredAnimation());
                        LOG.info("ANIM[{}] {} moving={} speed={} :: {}", p.length > 1 ? p[1] : "", entity.getType().toShortString(),
                                entity.getDeltaMovement().horizontalDistance() > 0.01, entity instanceof LivingEntity l ? l.walkAnimation.speed() : 0, sb);
                    }
                }
            });
            case "audit" -> {
                java.util.Map<Integer, int[]> idle = new java.util.HashMap<>();
                java.util.Map<Integer, String> lastDesc = new java.util.HashMap<>();
                int ticks = Integer.parseInt(p[1]);
                for (int i = 0; i < ticks; i++) {
                    final int tickNo = i;
                    context.runOnClient(mc -> {
                        if (mc.level == null) return;
                        for (Entity entity : mc.level.entitiesForRendering()) {
                            if (!(entity instanceof DragonEntity geo)) continue;
                            var manager = geo.getAnimatableInstanceCache().getManagerForId(entity.getId());
                            boolean any = false;
                            StringBuilder sb = new StringBuilder();
                            for (var controller : manager.getAnimationControllers().values()) {
                                var point = controller.getCurrentAnimationPoint();
                                if (controller.isAnimatingBones()) any = true;
                                sb.append(controller.getName()).append("=").append(point == null ? "-" : point.animation().name()).append(controller.isAnimatingBones() ? "+ " : "- ");
                            }
                            int[] c = idle.computeIfAbsent(entity.getId(), k -> new int[3]);
                            c[2]++;
                            if (!any) {
                                c[0]++;
                                c[1]++;
                                if (c[1] == 3) {
                                    LOG.info("AUDIT-FROZEN t={} {}#{} dist={} moving={} passenger={} vehicle={} :: {} (last good: {})", tickNo, entity.getType().toShortString(), entity.getId(), mc.player == null ? -1 : (int) mc.player.distanceTo(entity), entity.getDeltaMovement().horizontalDistance() > 0.01, entity.isPassenger(), entity.isVehicle(), sb, lastDesc.get(entity.getId()));
                                }
                            } else {
                                c[1] = 0;
                                lastDesc.put(entity.getId(), sb.toString());
                            }
                        }
                    });
                    context.waitTick();
                }
                idle.forEach((id, c) -> LOG.info("AUDIT-SUMMARY id={} frozenTicks={}/{}", id, c[0], c[2]));
            }
            case "trace" -> {
                java.util.Map<String, String> last = new java.util.HashMap<>();
                java.util.Map<String, Double> lastTime = new java.util.HashMap<>();
                int ticks = Integer.parseInt(p[1]);
                for (int i = 0; i < ticks; i++) {
                    final int tickNo = i;
                    context.runOnClient(mc -> {
                        if (mc.level == null) return;
                        for (Entity entity : mc.level.entitiesForRendering()) {
                            if (!(entity instanceof DragonEntity geo)) continue;
                            var manager = geo.getAnimatableInstanceCache().getManagerForId(entity.getId());
                            StringBuilder flags = new StringBuilder();
                            if (geo instanceof com.leon.saintsdragons.server.entity.base.RideableDragonBase rb) {
                                flags.append("fly=").append(rb.isFlying()).append(" take=").append(rb.isTakeoff()).append(" land=").append(rb.isLanding()).append(" hover=").append(rb.isHovering()).append(" veh=").append(rb.isVehicle());
                            }
                            flags.append(" sleep=").append(geo.isSleeping()).append(" sit=").append(geo.isInSittingPose()).append(" cmd=").append(geo.getCommand());
                            boolean any = false;
                            for (var controller : manager.getAnimationControllers().values()) {
                                var point = controller.getCurrentAnimationPoint();
                                String name = point == null ? "-" : point.animation().name();
                                double time = controller.getCurrentAnimationTime();
                                String key = entity.getId() + "/" + controller.getName();
                                String prev = last.put(key, name + (controller.isAnimatingBones() ? "+" : "-"));
                                Double prevTime = lastTime.put(key, time);
                                String now = name + (controller.isAnimatingBones() ? "+" : "-");
                                if (controller.isAnimatingBones()) any = true;
                                if (prev != null && !prev.equals(now)) {
                                    LOG.info("TRACE t={} {}#{} {}: {} -> {} | {}", tickNo, entity.getType().toShortString(), entity.getId(), controller.getName(), prev, now, flags);
                                } else if (prev != null && prevTime != null && name.equals(point == null ? "-" : name) && (time < prevTime - 0.001 || time > prevTime + 0.6)) {
                                    LOG.info("TRACE-JUMP t={} {}#{} {}: {} time {} -> {}", tickNo, entity.getType().toShortString(), entity.getId(), controller.getName(), name, String.format("%.2f", prevTime), String.format("%.2f", time));
                                }
                            }
                            if (!any) LOG.info("TRACE-NOBONES t={} {}#{} | {}", tickNo, entity.getType().toShortString(), entity.getId(), flags);
                        }
                    });
                    context.waitTick();
                }
            }
            case "fps" -> {
                long start = System.nanoTime();
                int[] frames = {0};
                int ticks = Integer.parseInt(p[1]);
                int[] last = {-1};
                long[] worst = {0};
                long[] prev = {System.nanoTime()};
                for (int i = 0; i < ticks; i++) {
                    context.runOnClient(mc -> {
                        long now = System.nanoTime();
                        worst[0] = Math.max(worst[0], now - prev[0]);
                        prev[0] = now;
                        frames[0] += mc.getFps();
                    });
                    context.waitTick();
                }
                long elapsed = System.nanoTime() - start;
                LOG.info("FPS[{}] avgFps={}", p.length > 2 ? p[2] : "", frames[0] / ticks);
            }
            case "watch" -> {
                for (int i = 0; i < Integer.parseInt(p[1]); i++) {
                    context.runOnClient(mc -> {
                        if (mc.level == null) return;
                        for (Entity entity : mc.level.entitiesForRendering()) {
                            if (entity instanceof DragonEntity geo) {
                                StringBuilder sb = new StringBuilder();
                                var manager = geo.getAnimatableInstanceCache().getManagerForId(entity.getId());
                                for (var controller : manager.getAnimationControllers().values()) {
                                    var point = controller.getCurrentAnimationPoint();
                                    if (point != null) {
                                        sb.append(controller.getName()).append("=").append(point.animation().name()).append("@")
                                                .append(String.format("%.2f", controller.getCurrentAnimationTime())).append(controller.isPlayingTriggeredAnimation() ? "T" : "").append("/").append(controller.getPlayState()).append(controller.isAnimatingBones() ? "+ " : "- ");
                                    }
                                }
                                LOG.info("WATCH {} | sleep={}/{}/{} cmd={} sitPose={} fly={} age={}", sb, geo.isSleeping(), geo.isSleepingEntering(), geo.isSleepingExiting(), geo.getCommand(), geo.isInSittingPose(), geo.isFlying(), geo.tickCount);
                            }
                        }
                    });
                    context.waitTick();
                }
            }
            case "rclick" -> context.getInput().holdMouseFor(1, 3);
            case "mdown" -> context.getInput().holdMouse(0);
            case "mup" -> context.getInput().releaseMouse(0);
            case "sleep" -> server.runOnServer(s -> dragons(s).forEach(d -> { if ("1".equals(p[1])) d.startSleepEnter(); else d.startSleepExit(); }));
            case "interact" -> server.runOnServer(s -> {
                ServerPlayer sp = player(s);
                sp.setShiftKeyDown(p.length > 1 && "sneak".equals(p[1]));
                if (p.length > 1 && "sneak".equals(p[1])) sp.setPose(net.minecraft.world.entity.Pose.CROUCHING);
                for (DragonEntity dragon : dragons(s)) {
                    var result = sp.interactOn(dragon, net.minecraft.world.InteractionHand.MAIN_HAND, dragon.position());
                    LOG.info("INTERACT {} holding={} result={} tame={} love={} age={}", dragon.getType().toShortString(), sp.getMainHandItem(), result, dragon.isTame(), dragon.isInLove(), dragon.getAge());
                }
            });
            case "count" -> server.runOnServer(s -> LOG.info("COUNT[{}] dragons={} babies={}", p[1], dragons(s).size(), dragons(s).stream().filter(DragonEntity::isBaby).count()));
            case "sit" -> server.runOnServer(s -> dragons(s).forEach(d -> d.setCommand(Integer.parseInt(p[1]))));
            case "cmd" -> server.runCommand(step.substring(4));
            default -> throw new IllegalArgumentException(step);
        }
    }

    private static int key(String name) {
        return InputConstants.getKey("key.keyboard." + name.toLowerCase(Locale.ROOT)).getValue();
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().get(0);
    }

    private static List<DragonEntity> dragons(MinecraftServer server) {
        List<DragonEntity> out = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof DragonEntity dragon) {
                    out.add(dragon);
                }
            }
        }
        return out;
    }

    private void state(TestServerContext server, String label) {
        server.runOnServer(s -> {
            for (DragonEntity dragon : dragons(s)) {
                var active = dragon.combatManager.getActiveAbility();
                if (dragon instanceof com.leon.saintsdragons.server.entity.base.RideableDragonBase rb) LOG.info("STATE[{}] ground={} lastFwd={} running={} accel={} aerial={} riderZza={}", label, rb.getGroundMoveState(), rb.getLastRiderForward(), rb.isRunning(), rb.isAccelerating(), rb.isAerial(), dragon.getControllingPassenger() instanceof net.minecraft.world.entity.player.Player pl ? pl.zza : -9);
                LOG.info("STATE[{}] sitProgress={} standingUp={} sittingDown={} cmd={}", label, dragon.getSitProgress(), dragon.isStandingUpAnimation(), dragon.isSittingDownAnimation(), dragon.getCommand());
                LOG.info("STATE[{}] hurt={} death={} baby={} sleeping={} sitting={} maxhp={} pose={}", label, dragon.hurtTime, dragon.deathTime, dragon.isBaby(), dragon.isSleeping(), dragon.isOrderedToSit(), dragon.getMaxHealth(), dragon.getPose());
                LOG.info("STATE[{}] {} hp={} pos={} flying={} active={} passengers={}", label,
                        dragon.getType().toShortString(), dragon.getHealth(), dragon.position(), dragon.isFlying(),
                        active == null ? "none" : active.getClass().getSimpleName() + "@" + active.getTicksInUse(),
                        dragon.getPassengers().size());
                if (dragon instanceof Raevyx raevyx) {
                    LOG.info("STATE[{}] raevyx beaming={} energy={}", label, raevyx.isBeaming(), raevyx.getBeamEnergy());
                }
                for (Entity rider : dragon.getPassengers()) {
                    LOG.info("STATE[{}] rider pos={}", label, rider.position());
                }
            }
            for (ServerLevel level : s.getAllLevels()) {
                for (Entity entity : level.getAllEntities()) {
                    if (entity instanceof DragonEntity || entity instanceof ServerPlayer) {
                        continue;
                    }
                    if (entity instanceof LivingEntity living) {
                        LOG.info("STATE[{}] other {} hp={}/{}", label, entity.getType().toShortString(),
                                living.getHealth(), living.getMaxHealth());
                    } else {
                        LOG.info("STATE[{}] entity {} at {}", label, entity.getType().toShortString(), entity.position());
                    }
                }
            }
        });
    }
}
