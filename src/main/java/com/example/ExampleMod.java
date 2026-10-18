package com.ejemplo.hornos;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HornosAutoMod implements ModInitializer {

    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // Verificar si la acción es en el servidor/cliente principal y con la mano principal
            if (world.isClient() || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            ItemStack heldItem = player.getStackInHand(hand);

            // 1. Verificar si el jugador tiene Carbón en la mano y está agachado (Sneaking / Shift/Ctrl)
            if (heldItem.isOf(Items.COAL) && player.isSneaking()) {
                BlockPos pos = hitResult.getBlockPos();
                
                // 2. Verificar si el bloque al que da clic es un Horno
                if (world.getBlockState(pos).isOf(Blocks.FURNACE)) {
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    
                    if (blockEntity instanceof AbstractFurnaceBlockEntity furnace) {
                        ItemStack fuelSlot = furnace.getStack(1); // Ranura 1 = Combustible en el horno

                        // 3. Rellenar si la ranura está vacía o si ya tiene carbón pero no está llena (máx 64)
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
                                furnace.markDirty(); // Guardar el estado del horno
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
