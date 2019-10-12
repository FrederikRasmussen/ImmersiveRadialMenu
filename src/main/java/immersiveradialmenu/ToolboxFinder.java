package immersiveradialmenu;

import java.util.List;

import blusunrize.immersiveengineering.common.gui.ContainerToolbox;
import blusunrize.immersiveengineering.common.items.ItemToolbox;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ToolboxFinder {
  public static ContainerToolbox findToolbox() {
    Minecraft mc = Minecraft.getMinecraft();
    EntityPlayer player = mc.player;
    EntityEquipmentSlot
    List<Slot> slots = player.inventoryContainer.inventorySlots;
    for (Slot slot : slots) {
      if (slot != null && slot.getHasStack()) {
        ItemStack stack = slot.getStack();
        if (stack.getItem() instanceof ItemToolbox) {
          return new ContainerToolbox(
            player.inventory,
            mc.world,
            slot,
            stack
          );
        }
      }
    }
    return null;
  }
}
