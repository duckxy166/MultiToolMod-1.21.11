package com.duckxy.multitoolmod;

import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;
import java.util.function.Predicate;

public class MultiToolItem extends BowItem {

    public MultiToolItem(Settings settings) {
        super(settings);
    }

    public float getMiningSpeed(ItemStack stack, BlockState state) {
        if (state.isIn(BlockTags.PICKAXE_MINEABLE) ||
                state.isIn(BlockTags.AXE_MINEABLE) ||
                state.isIn(BlockTags.SHOVEL_MINEABLE) ||
                state.isIn(BlockTags.HOE_MINEABLE)) {
            return 9.0F;
        }
        return 1.0F;
    }

    public boolean isSuitableFor(BlockState state) {
        int i = 3; // diamond level
        if (i < 3 && state.isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return false;
        } else if (i < 2 && state.isIn(BlockTags.NEEDS_IRON_TOOL)) {
            return false;
        } else if (i < 1 && state.isIn(BlockTags.NEEDS_STONE_TOOL)) {
            return false;
        }
        return state.isIn(BlockTags.PICKAXE_MINEABLE) ||
                state.isIn(BlockTags.AXE_MINEABLE) ||
                state.isIn(BlockTags.SHOVEL_MINEABLE) ||
                state.isIn(BlockTags.HOE_MINEABLE);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();

        // Flint and Steel functionality - light fire on Obsidian, Netherrack, or any solid block
        BlockPos firePos = pos.offset(context.getSide());
        if (canPlaceFire(world, state, firePos)) {
            world.playSound(player, firePos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            if (!world.isClient()) {
                BlockState fireState = AbstractFireBlock.getState(world, firePos);
                world.setBlockState(firePos, fireState, Block.NOTIFY_ALL);
                if (player instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
                    context.getStack().damage(1, serverWorld, serverPlayer, (item) -> {});
                }
                world.emitGameEvent(player, GameEvent.BLOCK_PLACE, firePos);
            }
            return ActionResult.SUCCESS;
        }

        // Hoe functionality - till dirt/grass
        if (state.isIn(BlockTags.DIRT) && world.getBlockState(pos.up()).isAir()) {
            BlockState farmland = Blocks.FARMLAND.getDefaultState();
            world.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!world.isClient()) {
                world.setBlockState(pos, farmland);
                if (player instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
                    context.getStack().damage(1, serverWorld, serverPlayer, (item) -> {});
                }
            }
            return ActionResult.SUCCESS;
        }

        // Shovel functionality - create path
        if ((state.isOf(Blocks.GRASS_BLOCK) || state.isOf(Blocks.DIRT)) && world.getBlockState(pos.up()).isAir()) {
            world.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!world.isClient()) {
                world.setBlockState(pos, Blocks.DIRT_PATH.getDefaultState());
                if (player instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
                    context.getStack().damage(1, serverWorld, serverPlayer, (item) -> {});
                }
            }
            return ActionResult.SUCCESS;
        }

        // Axe functionality - strip logs
        Block strippedBlock = getStrippedBlock(state.getBlock());
        if (strippedBlock != null) {
            world.playSound(player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!world.isClient()) {
                BlockState strippedState = strippedBlock.getDefaultState();
                if (state.contains(PillarBlock.AXIS)) {
                    strippedState = strippedState.with(PillarBlock.AXIS, state.get(PillarBlock.AXIS));
                }
                world.setBlockState(pos, strippedState);
                if (player instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
                    context.getStack().damage(1, serverWorld, serverPlayer, (item) -> {});
                }
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private Block getStrippedBlock(Block block) {
        if (block == Blocks.OAK_LOG) return Blocks.STRIPPED_OAK_LOG;
        if (block == Blocks.SPRUCE_LOG) return Blocks.STRIPPED_SPRUCE_LOG;
        if (block == Blocks.BIRCH_LOG) return Blocks.STRIPPED_BIRCH_LOG;
        if (block == Blocks.JUNGLE_LOG) return Blocks.STRIPPED_JUNGLE_LOG;
        if (block == Blocks.ACACIA_LOG) return Blocks.STRIPPED_ACACIA_LOG;
        if (block == Blocks.DARK_OAK_LOG) return Blocks.STRIPPED_DARK_OAK_LOG;
        if (block == Blocks.MANGROVE_LOG) return Blocks.STRIPPED_MANGROVE_LOG;
        if (block == Blocks.CHERRY_LOG) return Blocks.STRIPPED_CHERRY_LOG;
        if (block == Blocks.CRIMSON_STEM) return Blocks.STRIPPED_CRIMSON_STEM;
        if (block == Blocks.WARPED_STEM) return Blocks.STRIPPED_WARPED_STEM;
        if (block == Blocks.OAK_WOOD) return Blocks.STRIPPED_OAK_WOOD;
        if (block == Blocks.SPRUCE_WOOD) return Blocks.STRIPPED_SPRUCE_WOOD;
        if (block == Blocks.BIRCH_WOOD) return Blocks.STRIPPED_BIRCH_WOOD;
        if (block == Blocks.JUNGLE_WOOD) return Blocks.STRIPPED_JUNGLE_WOOD;
        if (block == Blocks.ACACIA_WOOD) return Blocks.STRIPPED_ACACIA_WOOD;
        if (block == Blocks.DARK_OAK_WOOD) return Blocks.STRIPPED_OAK_WOOD;
        if (block == Blocks.MANGROVE_WOOD) return Blocks.STRIPPED_MANGROVE_WOOD;
        if (block == Blocks.CHERRY_WOOD) return Blocks.STRIPPED_CHERRY_WOOD;
        if (block == Blocks.CRIMSON_HYPHAE) return Blocks.STRIPPED_CRIMSON_HYPHAE;
        if (block == Blocks.WARPED_HYPHAE) return Blocks.STRIPPED_WARPED_HYPHAE;
        return null;
    }

    private boolean canPlaceFire(World world, BlockState clickedState, BlockPos firePos) {
        // Only light fire on Obsidian or Netherrack (and variations)
        Block block = clickedState.getBlock();
        boolean isFireableBlock = block == Blocks.OBSIDIAN ||
                block == Blocks.CRYING_OBSIDIAN ||
                block == Blocks.NETHERRACK ||
                block == Blocks.SOUL_SAND ||
                block == Blocks.SOUL_SOIL;
        
        if (!isFireableBlock) {
            return false;
        }
        
        // Check if we can place fire at the target position
        BlockState fireState = world.getBlockState(firePos);
        return fireState.isAir() || fireState.isReplaceable();
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof Shearable shearable) {
            if (shearable.isShearable()) {
                World world = entity.getEntityWorld();
                if (user instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
                    shearable.sheared(serverWorld, SoundCategory.PLAYERS, stack);
                    stack.damage(1, serverWorld, serverPlayer, (item) -> {});
                    user.emitGameEvent(GameEvent.SHEAR, entity);
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient() && state.getHardness(world, pos) != 0.0F) {
            if (miner instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
                stack.damage(1, serverWorld, serverPlayer, (item) -> {});
            }
        }
        return true;
    }

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        World world = attacker.getEntityWorld();
        if (attacker instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
            stack.damage(1, serverWorld, serverPlayer, (item) -> {});
        }
    }
}