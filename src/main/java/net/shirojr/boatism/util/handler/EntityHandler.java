package net.shirojr.boatism.util.handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismEntities;
import net.shirojr.boatism.util.nbt.BoatEngineDataHelper;
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
                boatEngine -> boatEngine.getUuid().equals(uuid)
        );
        possibleEntities.addAll(
                world.getEntitiesByType(BoatismEntities.AIR_BOAT_ENGINE,
                        Box.of(pos, searchSize, searchSize, searchSize),
                        boatEngine -> boatEngine.getUuid().equals(uuid))
        );
        if (possibleEntities.isEmpty()) return Optional.empty();
        return Optional.ofNullable(possibleEntities.get(0));
    }

    public static void removePossibleBoatEngineEntry(Entity entity) {
        if (!(entity instanceof BoatEntity boatEntity)) return;
        UUID boatEngineUuid = ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid();
        Optional<BoatEngineEntity> boatEngineEntity = EntityHandler.getBoatEngineEntityFromUuid(boatEngineUuid, boatEntity.getWorld(), boatEntity.getPos(), 10);
        boatEngineEntity.ifPresent(entry -> {
            if (!(entry.getWorld() instanceof ServerWorld)) return;
            dropMountedInventory(entry, true, false);
            entry.removeBoatEngine(boatEntity);
        });
    }

    public static void dropItemStackFromMountedInventory(ItemStack itemStack, BoatEngineEntity boatEngineEntity) {
        boatEngineEntity.dropStack(itemStack);
    }

    public static void dropMountedInventory(BoatEngineEntity boatEngineEntity, boolean dropEngine, boolean dropEquipment) {
        List<ItemStack> allEngineStacks = new ArrayList<>();
        if (dropEngine) {
            allEngineStacks.add(BoatEngineDataHelper.getItemStackFromBoatEngineEntity(boatEngineEntity));
        }
        if (dropEquipment) {
            allEngineStacks.addAll(BoatEngineDataHelper.getMountedItemsFromBoatEngineEntity(boatEngineEntity));
        }
        for (ItemStack entry : allEngineStacks) {
            boatEngineEntity.dropStack(entry);
        }
        for (int i = 0; i < boatEngineEntity.getMountedInventory().size(); i++) {
            if (boatEngineEntity.getMountedInventory().getStack(i).isEmpty()) continue;
            boatEngineEntity.getMountedInventory().setStack(i, ItemStack.EMPTY);
        }
        boatEngineEntity.updateArmorModifier();
    }

    public static void engineLinkCleanUp(BoatEntity boatEntity) {
        if (!(boatEntity instanceof BoatEngineCoupler boatLink)) return;
        if (boatLink.boatism$getBoatEngineEntityUuid() == null) return;
        if (boatEntity.getPassengerList().stream().noneMatch(entity -> entity instanceof BoatEngineEntity)) {
            boatLink.boatism$setBoatEngineEntity(null);
        }
    }
}
