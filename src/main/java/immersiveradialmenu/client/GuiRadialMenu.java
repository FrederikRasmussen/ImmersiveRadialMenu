package immersiveradialmenu.client;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import org.lwjgl.opengl.GL11;

import immersiveradialmenu.ImmersiveRadialMenu;
import immersiveradialmenu.network.SwapItems;
import immersiveradialmenu.ToolboxFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber(Side.CLIENT)
public class GuiRadialMenu extends GuiScreen {
  private KeyBinding keybinding;
  private Predicate<ItemStack> predicate;
  private int minSlot;
  private int maxSlot;
  private IItemHandler toolbox;

  private List<ItemStack> cachedStacks;

  private boolean closing;
  private boolean doneClosing;
  private double startAnimation;

  private int selectedItem;

  GuiRadialMenu(
    KeyBinding keybinding,
    Predicate<ItemStack> predicate,
    int minSlot,
    int maxSlot
  ) {
    this.keybinding = keybinding;
    this.predicate = predicate;
    this.minSlot = minSlot;
    this.maxSlot = maxSlot;

    Minecraft mc = Minecraft.getMinecraft();
    this.toolbox = ToolboxFinder.findToolbox(mc.player);

    this.cachedStacks = null;

    this.closing = false;
    this.doneClosing = false;
    this.startAnimation = mc.world.getTotalWorldTime()
        + (double) mc.getRenderPartialTicks();

    selectedItem = -1;
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

    if (closing)
      if (doneClosing || toolbox == null) {
        mc.displayGuiScreen(null);
        ClientProxy.wipeOpen();
      }

    ItemStack inHand = mc.player.getHeldItemMainhand();
    if (!predicate.test(inHand))
      toolbox = null;
    if (toolbox == null)
      mc.displayGuiScreen(null);
    else if (!GameSettings.isKeyDown(keybinding))
      processClick(false);
  }

  @Override
  protected void mouseReleased(int mouseX, int mouseY, int state) {
      super.mouseReleased(mouseX, mouseY, state);
      processClick(true);
  }

  protected void processClick(boolean triggeredByMouse) {
    if (closing)
      return;
    if (toolbox == null)
      return;
    ItemStack inHand = mc.player.getHeldItemMainhand();
    if (!predicate.test(inHand))
      return;
    
    List<Integer> items = Lists.newArrayList();
    for (int i = minSlot; i < maxSlot; i++)
      items.add(i);

    int numItems = items.size();
    if (numItems <= 0)
      return;
    
    if (selectedItem >= 0) {
      int swapWith = items.get(selectedItem);
      int inHandCount = inHand.getCount();
      int toolboxSlotCount = toolbox.getStackInSlot(swapWith).getCount();
      if (inHandCount <= 0 && toolboxSlotCount <= 0 && triggeredByMouse)
        return;
      else {
        SwapItems.swapItem(swapWith, mc.player);
        ImmersiveRadialMenu.channel.sendToServer(new SwapItems(swapWith));
      }
    }

    animateClose();
  }

  private void animateClose() {
      closing = true;
      doneClosing = false;
      startAnimation =
          Minecraft.getMinecraft().world.getTotalWorldTime()
          + (double) Minecraft.getMinecraft().getRenderPartialTicks();
  }


  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);

    if (toolbox == null)
      return;
    
    List<ItemStack> items = cachedStacks;
    if (items == null) {
      items = Lists.newArrayList();
      for (int i = minSlot; i < maxSlot; i++) {
        ItemStack inSlot = toolbox.getStackInSlot(i);
        items.add(inSlot);
      }
      cachedStacks = items;
    }

    ItemStack inHand = mc.player.getHeldItemMainhand();
    if (!predicate.test(inHand))
      return;
    
    int numItems = items.size();
    if (numItems <= 0) {
      drawCenteredString(
        fontRenderer,
        I18n.format("text.immersiveradialmenu.empty"),
        width / 2,
        (height - fontRenderer.FONT_HEIGHT) / 2,
        0xFFFFFFFF
      );
      if (closing)
        doneClosing = true;
      return;
    }
    
    final float OPEN_ANIMATION_LENGTH = 2.5f;
    long worldTime = Minecraft.getMinecraft().world.getTotalWorldTime();
    float animationTime = (float) (worldTime + partialTicks - startAnimation);
    float openAnimation =
        closing
        ? 1.0f - animationTime / OPEN_ANIMATION_LENGTH
        : animationTime / OPEN_ANIMATION_LENGTH;
    
    if (closing && openAnimation <= 0.0f)
      doneClosing = true;
    
    float animProgress = MathHelper.clamp(openAnimation, 0, 1);
    float radiusIn = Math.max(0.1f, 30 * animProgress);
    float radiusOut = radiusIn * 2;
    float itemRadius = (radiusIn + radiusOut) * 0.5f;
    float animTop = (1 - animProgress) * height / 2.0f;

    int x = width / 2;
    int y = height / 2;

    double a = Math.toDegrees(Math.atan2(mouseY - y, mouseX - x));
    double d = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
    float s0 = (((0 - 0.5f) / (float) numItems) + 0.25f) * 360;
    if (a < s0) a += 360;

    GlStateManager.pushMatrix();
    GlStateManager.disableAlpha();
    GlStateManager.enableBlend();
    GlStateManager.disableTexture2D();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

    GlStateManager.translate(0, animTop, 0);

    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();
    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
    boolean hasMouseOver = false;
    ItemStack itemMouseOver = ItemStack.EMPTY;

    if (!closing) {
      selectedItem = -1;
      for (int i = 0; i < numItems; i++) {
        float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
        float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
        if (a >= s && a < e && d >= radiusIn && d < radiusOut) {
          selectedItem = i;
          break;
        }
      }
    }

    for (int i = 0; i < numItems; i++) {
      float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
      float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
      if (selectedItem == i) {
        drawPieArc(
          buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 255, 255, 255, 64
        );
        hasMouseOver = true;
        ItemStack inSlot = ItemStack.EMPTY;
        inSlot = items.get(i);
        itemMouseOver = inSlot;
      }
      else
        drawPieArc(
          buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64
        );
    }

    tessellator.draw();
    GlStateManager.enableTexture2D();

    boolean hasItemInHand = inHand.getCount() > 0;
    if (hasMouseOver)
      if (hasItemInHand)
        if (itemMouseOver.getCount() > 0)
          drawCenteredString(
            fontRenderer,
            I18n.format("text.immersiveradialmenu.swap"),
            width / 2, (height - fontRenderer.FONT_HEIGHT) / 2,
            0xFFFFFFFF
          );
        else
          drawCenteredString(
            fontRenderer,
            I18n.format("text.immersiveradialmenu.insert"),
            width / 2, (height - fontRenderer.FONT_HEIGHT) / 2,
            0xFFFFFFFF
          );
      else if (itemMouseOver.getCount() > 0)
        drawCenteredString(
          fontRenderer,
          I18n.format("text.immersiveradialmenu.extract"),
          width / 2,
          (height - fontRenderer.FONT_HEIGHT) / 2,
          0xFFFFFFFF
        );
    
    for (int i = minSlot; i < maxSlot; i++) {
        ItemStack inSlot = toolbox.getStackInSlot(i);
        if (inSlot.getCount() <= 0) {
            float angle1 =
                ((i / (float) numItems) + 0.25f) * 2 * (float) Math.PI;
            float posX = x + itemRadius * (float) Math.cos(angle1);
            float posY = y + itemRadius * (float) Math.sin(angle1);
            drawCenteredString(
              fontRenderer,
              I18n.format("text.immersiveradialmenu.empty"),
              (int)posX,
              (int)posY - fontRenderer.FONT_HEIGHT / 2,
              0x7FFFFFFF
            );
        }
    }
    
    RenderHelper.enableGUIStandardItemLighting();
    for (int i = 0; i < numItems; i++) {
      float angle1 = ((i / (float) numItems) + 0.25f) * 2 * (float) Math.PI;
      float posX = x - 8 + itemRadius * (float) Math.cos(angle1);
      float posY = y - 8 + itemRadius * (float) Math.sin(angle1);
      ItemStack inSlot = items.get(i);
      if (inSlot.getCount() > 0) {
          this.itemRender.renderItemAndEffectIntoGUI(
            inSlot,
            (int) posX,
            (int) posY
          );
          this.itemRender.renderItemOverlayIntoGUI(
            this.fontRenderer,
            inSlot,
            (int) posX,
            (int) posY,
            ""
          );
      }
    }
    RenderHelper.disableStandardItemLighting();

    GlStateManager.popMatrix();

    if (itemMouseOver.getCount() > 0)
        renderToolTip(itemMouseOver, mouseX, mouseY);
  }

  private static final float PRECISION = 5;

  private void drawPieArc(
    BufferBuilder buffer,
    float x,
    float y,
    float z,
    float radiusIn,
    float radiusOut,
    float startAngle,
    float endAngle,
    int r,
    int g,
    int b,
    int a
  ) {
      float angle = endAngle - startAngle;
      int sections = Math.max(1, MathHelper.ceil(angle / PRECISION));

      startAngle = (float) Math.toRadians(startAngle);
      endAngle = (float) Math.toRadians(endAngle);
      angle = endAngle - startAngle;

      for (int i = 0; i < sections; i++)
      {
          float angle1 = startAngle + (i / (float) sections) * angle;
          float angle2 = startAngle + ((i + 1) / (float) sections) * angle;

          float pos1InX = x + radiusIn * (float) Math.cos(angle1);
          float pos1InY = y + radiusIn * (float) Math.sin(angle1);
          float pos1OutX = x + radiusOut * (float) Math.cos(angle1);
          float pos1OutY = y + radiusOut * (float) Math.sin(angle1);
          float pos2OutX = x + radiusOut * (float) Math.cos(angle2);
          float pos2OutY = y + radiusOut * (float) Math.sin(angle2);
          float pos2InX = x + radiusIn * (float) Math.cos(angle2);
          float pos2InY = y + radiusIn * (float) Math.sin(angle2);

          buffer.pos(pos1OutX, pos1OutY, z).color(r, g, b, a).endVertex();
          buffer.pos(pos1InX, pos1InY, z).color(r, g, b, a).endVertex();
          buffer.pos(pos2InX, pos2InY, z).color(r, g, b, a).endVertex();
          buffer.pos(pos2OutX, pos2OutY, z).color(r, g, b, a).endVertex();
      }
  }

  @Override
  public boolean doesGuiPauseGame() {
      return false;
  }
}

/*
Note: This code has been modified from David Quintana's solution.
Below is the required copyright notice.

Copyright (c) 2015, David Quintana <gigaherz@gmail.com>
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the author nor the
      names of the contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
