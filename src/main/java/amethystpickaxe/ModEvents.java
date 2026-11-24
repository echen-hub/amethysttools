package amethystpickaxe;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.BlockTags;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.level.block.LeavesBlock;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.level.block.state.BlockState;


public class ModEvents {

    public static void initialize() {

        PlayerBlockBreakEvents.BEFORE.register(
                (level, player, pos, state, blockEntity) -> {

                    if (!(level instanceof ServerLevel serverLevel)) return true;
                    if (!(player instanceof ServerPlayer serverPlayer)) return true;

                    if (!player.isShiftKeyDown()) return true;

                    var tool = player.getMainHandItem();

                    if (tool.is(ModItems.AMETHYST_AXE)) {
                        if (!state.is(BlockTags.LOGS)) return true;

                        breakConnectedLogs(serverLevel, serverPlayer, pos, tool);
                        return false;
                    }

                    if (!tool.is(ModItems.AMETHYST_PICKAXE)) return true;

                    Vec3 look = player.getLookAngle();

                    double x = Math.abs(look.x);
                    double y = Math.abs(look.y);
                    double z = Math.abs(look.z);

                    int extraBlocksBroken = 0;

                    for (int a = -1; a <= 1; a++) {
                        for (int b = -1; b <= 1; b++) {

                            int dx = 0;
                            int dy = 0;
                            int dz = 0;

                            if (y > x && y > z) {
                                dx = a;
                                dz = b;
                            } else if (x > z) {
                                dy = a;
                                dz = b;
                            } else {
                                dx = a;
                                dy = b;
                            }

                            if (dx == 0 && dy == 0 && dz == 0) {
                                continue;
                            }

                            BlockPos target = pos.offset(dx, dy, dz);

                            if (!serverLevel.isEmptyBlock(target)
                                    && serverLevel.getBlockState(target).getDestroySpeed(serverLevel, target) >= 0.0F) {

                                var targetState = serverLevel.getBlockState(target);
                                var targetBlockEntity = serverLevel.getBlockEntity(target);

                                Block.dropResources(
                                        targetState,
                                        serverLevel,
                                        target,
                                        targetBlockEntity,
                                        serverPlayer,
                                        tool
                                );

                                if (serverLevel.destroyBlock(target, false, serverPlayer)) {
                                    extraBlocksBroken++;
                                }
                            }
                        }
                    }

                    // Original block already costs 1 durability.
                    int totalBlocksBroken = extraBlocksBroken + 1;

                    int totalCost = (totalBlocksBroken + 2) / 3;
                    int extraCost = totalCost - 1;

                    if (extraCost > 0) {
                        tool.hurtAndBreak(
                                extraCost,
                                serverLevel,
                                serverPlayer,
                                item -> serverPlayer.onEquippedItemBroken(
                                        item,
                                        EquipmentSlot.MAINHAND
                                )
                        );
                    }

                    return true;
                }
        );
    }
    private static void breakConnectedLogs(
            ServerLevel serverLevel,
            ServerPlayer player,
            BlockPos startPos,
            net.minecraft.world.item.ItemStack tool
    ) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> checked = new HashSet<>();
        Set<BlockPos> logPositions = new HashSet<>();
        Set<BlockPos> leafPositions = new HashSet<>();
        Map<BlockPos, BlockState> originalLeafStates = new HashMap<>();

        queue.add(startPos);
        logPositions.add(startPos);
        final int MAX_LOGS = 250;

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {

                        if (x == 0 && y == 0 && z == 0) continue;

                        BlockPos target = current.offset(x, y, z);

                        if (!checked.add(target)) continue;

                        var state = serverLevel.getBlockState(target);

                        if (!state.is(BlockTags.LOGS)) continue;
                        if (logPositions.size() >= MAX_LOGS) continue;

                        queue.add(target);
                        logPositions.add(target);
                    }
                }
            }
        }
        for (BlockPos logPos : logPositions) {
            for (int x = -7; x <= 7; x++) {
                for (int y = -7; y <= 7; y++) {
                    for (int z = -7; z <= 7; z++) {

                        BlockPos target = logPos.offset(x, y, z);

                        if (originalLeafStates.containsKey(target)) continue;

                        BlockState leafState = serverLevel.getBlockState(target);

                        if (!leafState.is(BlockTags.LEAVES)) continue;
                        if (logPositions.size() + leafPositions.size() >= MAX_LOGS) continue;

                        originalLeafStates.put(target, leafState);
                        leafPositions.add(target);
                    }
                }
            }
        }
        for (BlockPos logPos : logPositions) {
            BlockState logState = serverLevel.getBlockState(logPos);

            Block.dropResources(
                    logState,
                    serverLevel,
                    logPos,
                    serverLevel.getBlockEntity(logPos),
                    player,
                    tool
            );

            if (serverLevel.destroyBlock(logPos, false, player)) {
                tool.hurtAndBreak(
                        1,
                        serverLevel,
                        player,
                        item -> player.onEquippedItemBroken(
                                item,
                                EquipmentSlot.MAINHAND
                        )
                );
            }
        }
        for (BlockPos leafPos : leafPositions) {
            BlockState leafState = originalLeafStates.get(leafPos);

            if (leafState.getValue(LeavesBlock.PERSISTENT)) continue;

            int distance = leafState.getValue(LeavesBlock.DISTANCE);
            boolean touchesOtherLog = false;
            if(distance >= 7) continue;

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        BlockPos neighbor = leafPos.offset(x, y, z);

                        if (serverLevel.getBlockState(neighbor).is(BlockTags.LOGS)
                                && !logPositions.contains(neighbor)) {
                            touchesOtherLog = true;
                        }
                    }
                }
            }

            if (touchesOtherLog) continue;
            if (!serverLevel.getBlockState(leafPos).is(BlockTags.LEAVES)) continue;

            Block.dropResources(
                    leafState,
                    serverLevel,
                    leafPos,
                    serverLevel.getBlockEntity(leafPos),
                    player,
                    tool
            );

            serverLevel.destroyBlock(leafPos, false, player);
        }
    }
}