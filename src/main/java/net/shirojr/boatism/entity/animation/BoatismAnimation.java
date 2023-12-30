package net.shirojr.boatism.entity.animation;

import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;

public class BoatismAnimation {
    public static float SPIN_DURATION_IN_SEC = 1f;
    private BoatismAnimation() {
    }

    public static final Animation SPIN_RIGHT = Animation.Builder.create(SPIN_DURATION_IN_SEC).looping()
            .addBoneAnimation("propeller",
                    new Transformation(Transformation.Targets.ROTATE,
                            new Keyframe(0f, AnimationHelper.createRotationalVector(0f, 0f, 0f),
                                    Transformation.Interpolations.LINEAR),
                            new Keyframe(1f, AnimationHelper.createRotationalVector(0f, 0f, -360f),
                                    Transformation.Interpolations.LINEAR))).build();
    public static final Animation SPIN_LEFT = Animation.Builder.create(SPIN_DURATION_IN_SEC).looping()
            .addBoneAnimation("propeller",
                    new Transformation(Transformation.Targets.ROTATE,
                            new Keyframe(0f, AnimationHelper.createRotationalVector(0f, 0f, 0f),
                                    Transformation.Interpolations.LINEAR),
                            new Keyframe(1f, AnimationHelper.createRotationalVector(0f, 0f, 360f),
                                    Transformation.Interpolations.LINEAR))).build();
}
