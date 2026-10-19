package com.leon.saintsdragons.client.render26;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.model.GeoBone;
import org.jetbrains.annotations.Nullable;

/**
 * A bone as the 1.20.1 posing code saw it: rotation includes the rest rotation of the bone, and the rest
 * pose is available as an "initial snapshot". GeckoLib 5 keeps the rest rotation on the baked bone and only
 * the per-frame offset in the snapshot, so this adds and subtracts the rest rotation.
 */
public final class PoseBone {
    private final GeoBone bone;
    private final BoneSnapshot snapshot;
    private final PoseBoneLookup lookup;

    PoseBone(GeoBone bone, BoneSnapshot snapshot, PoseBoneLookup lookup) {
        this.bone = bone;
        this.snapshot = snapshot;
        this.lookup = lookup;
    }

    @FunctionalInterface
    interface PoseBoneLookup {
        @Nullable PoseBone find(String name);
    }

    public GeoBone bone() {
        return bone;
    }

    public String getName() {
        return bone.name();
    }

    @Nullable
    public PoseBone getParent() {
        return bone.parent() == null ? null : lookup.find(bone.parent().name());
    }

    public float getRotX() { return bone.baseRotX() + snapshot.getRotX(); }
    public float getRotY() { return bone.baseRotY() + snapshot.getRotY(); }
    public float getRotZ() { return bone.baseRotZ() + snapshot.getRotZ(); }
    public void setRotX(float value) { snapshot.setRotX(value - bone.baseRotX()); }
    public void setRotY(float value) { snapshot.setRotY(value - bone.baseRotY()); }
    public void setRotZ(float value) { snapshot.setRotZ(value - bone.baseRotZ()); }

    public void updateRotation(float x, float y, float z) {
        setRotX(x);
        setRotY(y);
        setRotZ(z);
    }

    public float getPosX() { return snapshot.getTranslateX(); }
    public float getPosY() { return snapshot.getTranslateY(); }
    public float getPosZ() { return snapshot.getTranslateZ(); }
    public void setPosX(float value) { snapshot.setTranslateX(value); }
    public void setPosY(float value) { snapshot.setTranslateY(value); }
    public void setPosZ(float value) { snapshot.setTranslateZ(value); }

    public void updatePosition(float x, float y, float z) {
        snapshot.setTranslation(x, y, z);
    }

    public float getScaleX() { return snapshot.getScaleX(); }
    public float getScaleY() { return snapshot.getScaleY(); }
    public float getScaleZ() { return snapshot.getScaleZ(); }
    public void setScaleX(float value) { snapshot.setScaleX(value); }
    public void setScaleY(float value) { snapshot.setScaleY(value); }
    public void setScaleZ(float value) { snapshot.setScaleZ(value); }

    public void updateScale(float x, float y, float z) {
        snapshot.setScale(x, y, z);
    }

    public float getPivotX() { return bone.pivotX(); }
    public float getPivotY() { return bone.pivotY(); }
    public float getPivotZ() { return bone.pivotZ(); }

    public boolean isHidden() { return snapshot.isHidden(); }
    public void setHidden(boolean hidden) { snapshot.skipRender(hidden); }
    public boolean isHidingChildren() { return snapshot.areChildrenHidden(); }
    public void setChildrenHidden(boolean hidden) { snapshot.skipChildrenRender(hidden); }

    public Initial getInitialSnapshot() {
        return new Initial(bone);
    }

    /** The rest pose of a bone. */
    public record Initial(GeoBone bone) {
        public float getRotX() { return bone.baseRotX(); }
        public float getRotY() { return bone.baseRotY(); }
        public float getRotZ() { return bone.baseRotZ(); }
        public float getOffsetX() { return 0.0F; }
        public float getOffsetY() { return 0.0F; }
        public float getOffsetZ() { return 0.0F; }
        public float getScaleX() { return 1.0F; }
        public float getScaleY() { return 1.0F; }
        public float getScaleZ() { return 1.0F; }
    }
}
