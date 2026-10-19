package com.leon.saintsdragons.client.render26;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.leon.saintsdragons.client.init.CommonClientModEvents.RendererRegistrar;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.registry.ModEntities;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import com.leon.saintsdragons.server.entity.dragons.Mossback;
import com.leon.saintsdragons.server.entity.effect.ignivorus.IgnivorusFireballEntity;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Entity renderer registration for the 26.2 pipeline.
 *
 * PORT TODO: this layer draws each entity's model, texture and keyframe animations only. The 1.20.1
 * renderers also did procedural posing (neck follow, tail drag, flight pitch), rider attachment sampling,
 * glow/emissive layers and the beam, breath and trail effects; those live in the not-yet-ported
 * client/renderer, client/model and client/particle packages. Effect-only entities render nothing for now.
 */
public final class ModEntityRenderers {
    public static void register(RendererRegistrar registrar) {
        dragon(registrar, ModEntities.RAEVYX, DragonModels.raevyx(), 1.25F, 3.0F);
        dragon(registrar, ModEntities.STEGONAUT, DragonModels.conventional("stegonaut"), 1.0F, 2.25F);
        dragon(registrar, ModEntities.CINDERVANE, DragonModels.cindervane(), 0.8F, 2.0F);
        dragon(registrar, ModEntities.VARASUCHUS, DragonModels.varasuchus(), 1.5F, 2.5F);
        dragon(registrar, ModEntities.IGNIVORUS, DragonModels.ignivorus(), 1.5F, 5.0F);
        dragon(registrar, ModEntities.VOLITANS, DragonModels.volitans(), 1.1F, 2.4F);
        dragon(registrar, ModEntities.NULLJAW, DragonModels.conventional("nulljaw"), 0.75F, 1.3F);
        dragon(registrar, ModEntities.ATROXIIA, DragonModels.conventional("atroxiia"), 0.45F, 2.0F);

        geo(registrar, ModEntities.LATCHER, swarm("latcher"), r -> r.shadow(0.45F));
        geo(registrar, ModEntities.WINGED, swarm("winged"), r -> r.shadow(0.35F));
        geo(registrar, ModEntities.WHETTLED, swarm("whettled"), r -> r.shadow(0.55F));
        geo(registrar, ModEntities.MOOP, entity("moop/moop"), r -> r.shadow(0.25F));
        geo(registrar, ModEntities.IVY_THE_DRAGON_MERCHANT, entity("ivy_oleander/ivy_oleander"), r -> r.shadow(0.6F));
        geo(registrar, ModEntities.MOSSBACK, mossback(), r -> r.shadow(0.3F));

        geo(registrar, ModEntities.CINDERVANE_MAGMA_BLOCK, object("shared/fireball/fireball_stage_1"),
                r -> r.renderType(RenderTypes::entityTranslucentEmissive));
        geo(registrar, ModEntities.IGNIVORUS_MAGMA_BLOCK, ignivorusFireball(),
                r -> r.renderType(RenderTypes::entityTranslucentEmissive)
                        .scale(fireball -> fireball.getVisualScale() >= 6.0F
                                ? (fireball.getVisualScale() >= 8.0F ? 3.0F : 2.4F)
                                : fireball.getVisualScale() * 0.35F));
        geo(registrar, ModEntities.IGNIVORUS_MAGMA_PILLAR, object("ignivorus/ignivorus_magma_pillar"), r -> r.shadow(1.0F));
        geo(registrar, ModEntities.STEGONAUT_AMETHYST_PILLAR, object("stegonaut/amethyst_pillar"),
                r -> r.shadow(0.5F).scale(pillar -> pillar.getVisualScale()));
        geo(registrar, ModEntities.VOLITANS_GROUND_CHUNK, ResolvedGeoModel.fixed(
                "geo/objects/volitans/ground_chunk.geo.json", "textures/block/dirt.png",
                "animations/objects/volitans/ground_chunk.animation.json", "minecraft"), r -> { });
        geo(registrar, ModEntities.VOLITANS_BURROW_MOUND, object("volitans/burrow_mound"),
                r -> r.renderType(RenderTypes::entityTranslucent));
        geo(registrar, ModEntities.VOLITANS_SPINE, object("volitans/volitans_spine"), r -> r.shadow(0.15F));
        geo(registrar, ModEntities.ARROW_OF_VENOM, object("volitans/arrow_of_venom"), r -> r.shadow(0.15F));
        geo(registrar, ModEntities.VOLITANS_POISON_BALL, object("volitans/volitans_poison_orb"),
                r -> r.scale(orb -> orb.getVisualScale() * 0.9F));

        // Effect-only entities: drawn entirely by the VFX code that is not ported yet.
        noop(registrar, ModEntities.RAEVYX_LIGHTNING_CHAIN);
        noop(registrar, ModEntities.RAEVYX_GROUND_REND_TRAIL);
        noop(registrar, ModEntities.IGNIVORUS_FLAME);
        noop(registrar, ModEntities.IGNIVORUS_NOVA);
        noop(registrar, ModEntities.IGNIVORUS_NOVA_RING);
        noop(registrar, ModEntities.STEGONAUT_GROUND_CHUNK);
        noop(registrar, ModEntities.STEGONAUT_IMPACT_RING);
        noop(registrar, ModEntities.GROUND_FISSURE);
        noop(registrar, ModEntities.DRAGON_WATER_SPLASH);
        noop(registrar, ModEntities.VISUAL_FALLING_BLOCK);
        noop(registrar, ModEntities.VOLITANS_WATER_BREATH);
    }

    private static <T extends DragonEntity> void dragon(RendererRegistrar registrar, Supplier<EntityType<T>> type,
                                                        GeoModel<T> model, float babyShadow, float adultShadow) {
        registrar.register(type.get(), context -> new ModGeoEntityRenderer<>(context, model)
                .shadow(dragon -> dragon.isBaby() ? babyShadow : adultShadow));
    }

    private static <T extends Entity & GeoAnimatable> void geo(RendererRegistrar registrar,
                                                               Supplier<EntityType<T>> type, GeoModel<T> model,
                                                               Consumer<ModGeoEntityRenderer<T, ?>> setup) {
        registrar.register(type.get(), context -> {
            ModGeoEntityRenderer<T, ?> renderer = new ModGeoEntityRenderer<>(context, model);
            setup.accept(renderer);
            return renderer;
        });
    }

    private static <T extends Entity> void noop(RendererRegistrar registrar, Supplier<EntityType<T>> type) {
        registrar.register(type.get(), NoopRenderer::new);
    }

    private static <T extends GeoAnimatable> ResolvedGeoModel<T> entity(String path) {
        return ResolvedGeoModel.fixed("geo/entity/" + path + ".geo.json", "textures/entity/" + path + ".png",
                "animations/entity/" + path + ".animation.json");
    }

    private static <T extends GeoAnimatable> ResolvedGeoModel<T> swarm(String name) {
        return entity("draconian_swarm/" + name + "/" + name);
    }

    private static <T extends GeoAnimatable> ResolvedGeoModel<T> object(String path) {
        return ResolvedGeoModel.fixed("geo/objects/" + path + ".geo.json", "textures/objects/" + path + ".png",
                "animations/objects/" + path + ".animation.json");
    }

    private static ResolvedGeoModel<Mossback> mossback() {
        Identifier adult = SaintsDragonsCommon.rl("entity/mossback/mossback");
        Identifier baby = SaintsDragonsCommon.rl("entity/mossback/baby_mossback");
        Identifier adultTexture = SaintsDragonsCommon.rl("textures/entity/mossback/mossback.png");
        Identifier babyTexture = SaintsDragonsCommon.rl("textures/entity/mossback/baby_mossback.png");
        return new ResolvedGeoModel<>(
                mossback -> mossback.isBaby() ? baby : adult,
                mossback -> mossback.isBaby() ? babyTexture : adultTexture,
                mossback -> mossback.isBaby() ? baby : adult,
                adult, adultTexture);
    }

    private static ResolvedGeoModel<IgnivorusFireballEntity> ignivorusFireball() {
        Identifier[] models = {
                SaintsDragonsCommon.rl("objects/shared/fireball/fireball_stage_1"),
                SaintsDragonsCommon.rl("objects/shared/fireball/fireball_stage_2"),
                SaintsDragonsCommon.rl("objects/ignivorus/ignivorus_fireball_stage_3")
        };
        Identifier[] textures = {
                SaintsDragonsCommon.rl("textures/objects/shared/fireball/fireball_stage_1.png"),
                SaintsDragonsCommon.rl("textures/objects/shared/fireball/fireball_stage_2.png"),
                SaintsDragonsCommon.rl("textures/objects/ignivorus/ignivorus_fireball_stage_3.png")
        };
        return new ResolvedGeoModel<>(
                fireball -> models[stage(fireball)], fireball -> textures[stage(fireball)],
                fireball -> models[stage(fireball)], models[0], textures[0]);
    }

    private static int stage(IgnivorusFireballEntity fireball) {
        float scale = fireball.getVisualScale();
        return scale >= 8.0F ? 2 : scale >= 6.0F ? 1 : 0;
    }

    private ModEntityRenderers() {
    }
}
