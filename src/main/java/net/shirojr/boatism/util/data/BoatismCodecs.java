package net.shirojr.boatism.util.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.EulerAngle;

public class BoatismCodecs {
    public static final Codec<EulerAngle> EULER_ANGLE = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("pitch").forGetter(EulerAngle::getPitch),
            Codec.FLOAT.fieldOf("yaw").forGetter(EulerAngle::getYaw),
            Codec.FLOAT.fieldOf("roll").forGetter(EulerAngle::getRoll)
    ).apply(instance, EulerAngle::new));
}
