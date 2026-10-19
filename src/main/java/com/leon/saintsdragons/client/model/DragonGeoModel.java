package com.leon.saintsdragons.client.model;

import com.leon.saintsdragons.client.render26.PosedGeoModel;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import net.minecraft.resources.Identifier;

public abstract class DragonGeoModel<T extends DragonEntity> extends PosedGeoModel<T> {
    protected final Identifier model;
    protected final Identifier babyModel;
    protected final Identifier animation;
    protected final Identifier babyAnimation;
    protected final Identifier maleTexture;
    protected final Identifier femaleTexture;
    protected final Identifier babyMaleTexture;
    protected final Identifier babyFemaleTexture;

    protected DragonGeoModel(String dragonId) {
        this(dragonId, true);
    }

    protected DragonGeoModel(String dragonId, boolean hasBabyResources) {
        this(SaintsDragonsCommon.rl(dragonId), hasBabyResources);
    }

    protected DragonGeoModel(Identifier dragonId) {
        this(dragonId, true);
    }

    protected DragonGeoModel(Identifier dragonId, boolean hasBabyResources) {
        this(dragonId, Resources.conventional(dragonId, hasBabyResources));
    }

    protected DragonGeoModel(Identifier dragonId, Resources resources) {
        this.model = resources.model();
        this.babyModel = resources.babyModel();
        this.animation = resources.animation();
        this.babyAnimation = resources.babyAnimation();
        this.maleTexture = resources.maleTexture();
        this.femaleTexture = resources.femaleTexture();
        this.babyMaleTexture = resources.babyMaleTexture();
        this.babyFemaleTexture = resources.babyFemaleTexture();
    }

    /** Model and animation ids are GeckoLib 5 ids (under geckolib/models and geckolib/animations). */
    public record Resources(Identifier model, Identifier babyModel,
                            Identifier animation, Identifier babyAnimation,
                            Identifier maleTexture, Identifier femaleTexture,
                            Identifier babyMaleTexture, Identifier babyFemaleTexture) {
        public Resources {
            java.util.Objects.requireNonNull(model, "model");
            java.util.Objects.requireNonNull(babyModel, "babyModel");
            java.util.Objects.requireNonNull(animation, "animation");
            java.util.Objects.requireNonNull(babyAnimation, "babyAnimation");
            java.util.Objects.requireNonNull(maleTexture, "maleTexture");
            java.util.Objects.requireNonNull(femaleTexture, "femaleTexture");
            java.util.Objects.requireNonNull(babyMaleTexture, "babyMaleTexture");
            java.util.Objects.requireNonNull(babyFemaleTexture, "babyFemaleTexture");
        }

        public static Resources conventional(Identifier id, boolean babies) {
            String path = id.getPath();
            String name = path.substring(path.lastIndexOf('/') + 1);
            Identifier model = resource(id, "entity/" + path + "/" + name);
            Identifier male = resource(id, "textures/entity/" + path + "/" + name + ".png");
            Identifier female = resource(id, "textures/entity/" + path + "/" + name + "_female.png");
            Identifier baby = babies ? resource(id, "entity/" + path + "/baby_" + name) : model;
            return new Resources(model, baby, model, baby, male, female,
                    babies ? resource(id, "textures/entity/" + path + "/baby_" + name + ".png") : male,
                    babies ? resource(id, "textures/entity/" + path + "/baby_" + name + "_female.png") : female);
        }

        private static Identifier resource(Identifier id, String path) {
            return Identifier.fromNamespaceAndPath(id.getNamespace(), path);
        }
    }

    @Override
    public Identifier getModelResource(T entity) {
        return entity != null && entity.isBaby() ? babyModel : model;
    }

    @Override
    public Identifier getTextureResource(T entity) {
        if (entity == null) {
            return maleTexture;
        }
        if (entity.isBaby()) {
            return getBabyTexture(entity);
        }
        return getAdultTexture(entity);
    }

    @Override
    public Identifier getAnimationResource(T entity) {
        return entity != null && entity.isBaby() ? babyAnimation : animation;
    }

    /** Baby files only carry the basic clips; anything else (flight, abilities, falling) plays the adult clip instead of dropping to the bind pose. */
    @Override
    public Identifier[] getAnimationResourceFallbacks(T entity) {
        return entity != null && entity.isBaby() && !babyAnimation.equals(animation) ? new Identifier[] {animation} : new Identifier[0];
    }

    protected Identifier getAdultTexture(T entity) {
        if (entity.hasCustomTextureVariant()) {
            return entity.getCustomAdultTextureResource(entity.isFemale());
        }
        return entity.isFemale() ? femaleTexture : maleTexture;
    }

    protected Identifier getBabyTexture(T entity) {
        return entity.isFemale() ? babyFemaleTexture : babyMaleTexture;
    }
}
