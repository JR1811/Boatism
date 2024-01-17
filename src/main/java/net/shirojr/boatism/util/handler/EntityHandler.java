package net.shirojr.boatism.util.handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.boatism.entity.BoatismEntities;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.util.nbt.BoatEngineNbtHelper;
import net.shirojr.boatism.util.nbt.NbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused")
public class EntityHandler {
    private EntityHandler() {
    }

    public static Optional<BoatEngineEntity> getBoatEngineEntityFromUuid(@Nullable UUID uuid, World world, Vec3d pos, int searchSize) {
        if (uuid == null) return Optional.empty();
        List<BoatEngineEntity> possibleEntities = world.getEntitiesByType(BoatismEntities.BOAT_ENGINE,
                Box.of(pos, searchSize, searchSize, searchSize),
                boatEngine -> boatEngine.getUuid().equals(uuid));
        if (possibleEntities.size() < 1) return Optional.empty();
        return Optional.ofNullable(possibleEntities.get(0));
    }

    public static void removePossibleBoatEngineEntry(Entity entity) {
        if (!(entity instanceof BoatEntity boatEntity)) return;
        ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid()
                .flatMap(uuid -> EntityHandler.getBoatEngineEntityFromUuid(uuid, boatEntity.getWorld(),
                        boatEntity.getPos(), 10))
                .ifPresent(boatEngineEntity -> {
                    if (!(boatEngineEntity.getWorld() instanceof ServerWorld)) return;
                    dropMountedInventory(boatEngineEntity, true, false);
                    boatEngineEntity.removeBoatEngine(boatEntity);
                });
    }

    public static void dropItemStackFromMountedInventory(ItemStack itemStack, BoatEngineEntity boatEngineEntity) {
        boatEngineEntity.dropStack(itemStack);
    }

    public static void dropMountedInventory(BoatEngineEntity boatEngineEntity, boolean dropEngine, boolean dropEquipment) {
        List<ItemStack> allEngineStacks = new ArrayList<>();
        if (dropEngine) {
            allEngineStacks.add(BoatEngineNbtHelper.getItemStackFromBoatEngineEntity(boatEngineEntity));
        }
        if (dropEquipment) {
            allEngineStacks.addAll(BoatEngineNbtHelper.getMountedItemsFromBoatEngineEntity(boatEngineEntity));
        }
        for (ItemStack entry : allEngineStacks) {
            boatEngineEntity.dropStack(entry);
        }
        for (int i = 0; i < boatEngineEntity.getMountedInventory().size(); i++) {
            boatEngineEntity.getMountedInventory().setStack(i, ItemStack.EMPTY);
        }
    }

    public static void engineLinkCleanUp(BoatEntity boatEntity) {
        if (!(boatEntity instanceof BoatEngineCoupler boatLink)) return;
        if (boatLink.boatism$getBoatEngineEntityUuid().isEmpty()) return;
        if (boatEntity.getPassengerList().stream().noneMatch(entity -> entity instanceof BoatEngineEntity)) {
            boatLink.boatism$setBoatEngineEntity(null);
        }
    }
}
