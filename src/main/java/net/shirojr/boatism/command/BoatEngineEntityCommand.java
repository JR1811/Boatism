package net.shirojr.boatism.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.init.BoatismEntities;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;

public class BoatEngineEntityCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal(Boatism.MODID).then(CommandManager.literal("sound")
                        .then(CommandManager.literal("stop").executes(BoatEngineEntityCommand::stopAllSoundInstances)))
                .then(CommandManager.literal("entities")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                        .then(CommandManager.literal("remove").executes(BoatEngineEntityCommand::removeBoatEngineEntities))));
    }

    private static int stopAllSoundInstances(CommandContext<ServerCommandSource> context) {
        PacketByteBuf buf = PacketByteBufs.create();
        ServerPlayerEntity serverPlayerEntity = context.getSource().getPlayer();
        if (serverPlayerEntity == null) return 0;
        ServerPlayNetworking.send(serverPlayerEntity, BoatismNetworkIdentifiers.SOUND_END_ALL.getIdentifier(), buf);
        return 1;
    }

    private static int removeBoatEngineEntities(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Iterable<ServerWorld> serverWorlds = context.getSource().getServer().getWorlds();
        serverWorlds.forEach(serverWorld -> serverWorld.getEntitiesByType(BoatismEntities.BOAT_ENGINE, boatEngine -> true)
                .forEach(boatEngine -> {
                    context.getSource().sendFeedback(() -> Text.literal(context.getSource().getName() +
                            " removed " + boatEngine.toString()), true);
                    boatEngine.getHookedBoatEntity().ifPresent(boatEntity ->
                            ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(null));
                    boatEngine.remove(Entity.RemovalReason.DISCARDED);
                }));
        return stopAllSoundInstances(context);
    }
}
