package net.shirojr.boatism.util.rendering;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RenderUtil {
    public static void renderQuad(VertexConsumer consumer, MatrixStack.Entry entry, Vector3f pos1, Vector3f pos2,
                                  Vector3f normal, QuadUV uv, int color, int light, int overlay, boolean reverse) {
        List<Runnable> vertices = Arrays.asList(
                () -> consumer.vertex(entry, pos1)
                        .color(color)
                        .texture(uv.minU(), uv.minV())
                        .light(light)
                        .overlay(overlay)
                        .normal(normal.x(), normal.y(), normal.z()),
                () -> consumer.vertex(entry, pos1.x(), pos2.y(), pos1.z())
                        .color(color)
                        .texture(uv.minU(), uv.maxV())
                        .light(light)
                        .overlay(overlay)
                        .normal(normal.x(), normal.y(), normal.z()),
                () -> consumer.vertex(entry, pos2)
                        .color(color)
                        .texture(uv.maxU(), uv.maxV())
                        .light(light)
                        .overlay(overlay)
                        .normal(normal.x(), normal.y(), normal.z()),
                () -> consumer.vertex(entry, pos2.x(), pos1.y(), pos2.z())
                        .color(color)
                        .texture(uv.maxU(), uv.minV())
                        .light(light)
                        .overlay(overlay)
                        .normal(normal.x(), normal.y(), normal.z())
        );

        if (reverse) {
            Collections.reverse(vertices);
        }
        vertices.forEach(Runnable::run);
    }

    public record QuadUV(float minU, float minV, float maxU, float maxV) {
    }
}
