package immersiveradialmenu;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class Toolbox {
  ItemStack toolboxStack;
  IItemHandler toolboxHandler;
  boolean inOffHand;
  int inventorySlot;

  private Toolbox(
    ItemStack toolboxStack,
    IItemHandler toolboxHandler,
    int inventorySlot,
    boolean inOffHand
  ) {
    this.toolboxStack = toolboxStack;
    this.toolboxHandler = toolboxHandler;
    this.inOffHand = inOffHand;
    this.inventorySlot = inventorySlot;
  }

  public static Toolbox toolboxFromOffHand(
    ItemStack toolboxStack
  ) {
    return new Toolbox(
      toolboxStack,
      toolboxStack.getCapability(
        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
        null
      ),
      -1,
      true
    );
  }

  public static Toolbox toolboxFromInventory(
    ItemStack toolboxStack,
    int inventorySlot
  ) {
    return new Toolbox(
      toolboxStack,
      toolboxStack.getCapability(
        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
        null
      ),
      inventorySlot,
      false
    );
  }

  public IItemHandler handler() {
    return toolboxHandler;
  }

  public ItemStack stack(EntityPlayer player) {
    return toolboxStack;
  }

  public int inventorySlot() {
    return inventorySlot;
  }

  public boolean inOffHand() {
    return inOffHand;
  }
}
