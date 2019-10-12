package immersiveradialmenu.client;

import java.util.function.Predicate;

import blusunrize.immersiveengineering.common.gui.ContainerToolbox;
import immersiveradialmenu.ToolboxFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class GuiRadialMenu extends GuiScreen {
  private KeyBinding keybinding;
  private Predicate<ItemStack> predicate;
  private ContainerToolbox toolbox;

  private boolean closing;
  private boolean doneClosing;
  private double startAnimation;

  private int selectedItem;
  private boolean keyCycleBeforeL;
  private boolean keyCycleBeforeR;

  GuiRadialMenu(
    KeyBinding keybinding,
    Predicate<ItemStack> predicate
  ) {
    this.keybinding = keybinding;
    this.predicate = predicate;
    this.toolbox = ToolboxFinder.findToolbox();

    this.closing = false;
    this.doneClosing = false;
    Minecraft mc = Minecraft.getMinecraft();
    this.startAnimation =mc.world.getTotalWorldTime()
        + (double) mc.getRenderPartialTicks();

    selectedItem = -1;
    keyCycleBeforeL = false;
    keyCycleBeforeR = false;
  }

  @SubscribeEvent
  public static void overlayEvent(
    RenderGameOverlayEvent.Pre event
  ) {
    ElementType type = event.getType();
    if (type != RenderGameOverlayEvent.ElementType.CROSSHAIRS)
      return;
    Minecraft mc = Minecraft.getMinecraft();
    if (mc.currentScreen instanceof GuiRadialMenu)
      event.setCanceled(true);
  }

  @Override
  public void updateScreen() {
    super.updateScreen();

    if (closing) {
      if (doneClosing || toolbox == null) {
        mc.displayGuiScreen(null);
        ClientProxy.wipeOpen();
      }
    }

    ItemStack inhand = mc.player.getHeldItemMainhand();
    if (!predicate.test(inhand))
      toolbox = null;
    if (toolbox == null)
      mc.displayGuiScreen(null);
    else if (!GameSettings.isKeyDown(keybinding))
      processClick(false);
  }

  @Override
  protected void mouseReleased(int mouseX, int mouseY, int state)
  {
      super.mouseReleased(mouseX, mouseY, state);
      processClick(true);
  }

  protected void processClick(boolean triggeredByMouse) {
    if (closing)
      return;
    if (toolbox == null)
      return;
  }
}
