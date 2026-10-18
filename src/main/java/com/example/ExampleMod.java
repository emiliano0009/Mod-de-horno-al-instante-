package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class ExampleMod implements ModInitializer {

    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            ItemStack heldItem = player.getStackInHand(hand);

            if (heldItem.isOf(Items.COAL) && player.isSneaking()) {
                BlockPos pos = hitResult.getBlockPos();
                
                if (world.getBlockState(pos).isOf(Blocks.FURNACE)) {
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    
                    if (blockEntity instanceof AbstractFurnaceBlockEntity furnace) {
                        ItemStack fuelSlot = furnace.getStack(1);

                        if (fuelSlot.isEmpty() || (fuelSlot.isOf(Items.COAL) && fuelSlot.getCount() < 64)) {
                            int spaceAvailable = fuelSlot.isEmpty() ? 64 : 64 - fuelSlot.getCount();
                            int coalInHand = heldItem.getCount();

                            int toTransfer = Math.min(spaceAvailable, coalInHand);

                            if (toTransfer > 0) {
                                if (fuelSlot.isEmpty()) {
                                    furnace.setStack(1, new ItemStack(Items.COAL, toTransfer));
                                } else {
                                    fuelSlot.increment(toTransfer);
                                }
                                heldItem.decrement(toTransfer);
                                furnace.markDirty();
                                return ActionResult.SUCCESS;
                            }
                        }
                    }
                }
            }
            return ActionResult.PASS;
        });
    }
}
