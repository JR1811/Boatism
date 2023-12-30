package net.shirojr.boatism.util;

import net.minecraft.entity.EntityDimensions;
import org.joml.Vector3f;

public interface CustomBoatEngineAttachment {
    Vector3f boatism$attachmentPos(EntityDimensions dimensions);
}
