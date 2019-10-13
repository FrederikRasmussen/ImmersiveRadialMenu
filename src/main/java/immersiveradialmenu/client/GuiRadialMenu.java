package immersiveradialmenu.client;

import java.util.List;

import org.lwjgl.opengl.GL11;

import immersiveradialmenu.Category;
import immersiveradialmenu.ImmersiveRadialMenu;
import immersiveradialmenu.ToolboxFinder;
import immersiveradialmenu.network.SwapItems;
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
  private static final float PRECISION = 5.0f;

  private KeyBinding keybinding;

  private boolean closing;
  private boolean doneClosing;

  private double startAnimation;

  private Category selectedCategory;
  private int selectedItem;

  GuiRadialMenu(
    KeyBinding keybinding
  ) {
    this.keybinding = keybinding;

    this.closing = false;
    this.doneClosing = false;

    Minecraft mc = Minecraft.getMinecraft();
    this.startAnimation =
        mc.world.getTotalWorldTime() + (double) mc.getRenderPartialTicks();
    
    this.selectedCategory = null;
    this.selectedItem = -1;
  }

  @SubscribeEvent
  public static void overlayEvent(
    RenderGameOverlayEvent.Pre event
  ) {
    if (Minecraft.getMinecraft().currentScreen instanceof GuiRadialMenu) {
      if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
        event.setCanceled(true);
      }
    }
  }

  @Override
  protected void mouseReleased(
    int mouseX,
    int mouseY,
    int state
  ) {
      super.mouseReleased(mouseX, mouseY, state);
      List<Category> categories = mainHandCategories();
      processClick(true, categories);
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
    List<Category> categories = mainHandCategories();
    if (selectedCategory == null && categories.size() <= 2) {
      selectedCategory = categories.get(0);
    }
    if (!hasToolbox() || categories.isEmpty() || closing && doneClosing) {
      mc.displayGuiScreen(null);
      ClientProxy.wipeOpen();
    } else if (!closing && !GameSettings.isKeyDown(keybinding)) {
      processClick(false, categories);
      if (!closing) {
        animateClose();
      }
    }
  }

  private void processClick(
    boolean triggeredByMouse,
    List<Category> categories
  ) {
    if (!hasToolbox() || !(closing || categories.isEmpty())) {
      if (selectedItem >= 0) {
        if (selectedCategory == null) {
          processCategoryClick(triggeredByMouse, categories);
        } else {
          processSlotClick(triggeredByMouse);
          animateClose();
        }
      }
    }
  }

  private void processCategoryClick(
    boolean triggeredByMouse,
    List<Category> categories
  ) {
    if (triggeredByMouse) {
      selectedCategory = categories.get(selectedItem);
    } else {
      animateClose();
    }
  }

  private void processSlotClick(
    boolean triggeredByMouse
  ) {
    int slotIndex = selectedCategory.slotIndices()[selectedItem];
    SwapItems.swapItem(slotIndex, mc.player);
    ImmersiveRadialMenu.channel.sendToServer(new SwapItems(slotIndex));
  }

  private void animateClose() {
    closing = true;
    doneClosing = false;
    startAnimation = Minecraft.getMinecraft().world.getTotalWorldTime()
        + (double) Minecraft.getMinecraft().getRenderPartialTicks();
  }

  @Override
  public void drawScreen(
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    super.drawScreen(mouseX, mouseY, partialTicks);
    List<Category> categories = mainHandCategories();
    if (hasToolbox() && !categories.isEmpty()) {
      final float OPEN_ANIMATION_LENGTH = 2.5f;
      long worldTime = Minecraft.getMinecraft().world.getTotalWorldTime();
      float animationTime = (float) (worldTime + partialTicks - startAnimation);
      float openAnimation =
          closing
          ? 1.0f - animationTime / OPEN_ANIMATION_LENGTH
          : animationTime / OPEN_ANIMATION_LENGTH;
      if (closing && openAnimation <= 0.0f) {
        doneClosing = true;
      }
      
      float animProgress = MathHelper.clamp(openAnimation, 0, 1);
      float radiusIn = Math.max(0.1f, 30 * animProgress);
      float radiusOut = radiusIn * 2;
      float itemRadius = (radiusIn + radiusOut) * 0.5f;
      float animTop = (1 - animProgress) * height / 2.0f;
      int x = width / 2;
      int y = height / 2;

      int numberOfSlices = selectedCategory == null ? categories.size() : selectedCategory.numberOfIndices();

      double a = Math.toDegrees(Math.atan2(mouseY - y, mouseX - x));
      double d = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
      float s0 = (((0 - 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
      if (a < s0) {
        a += 360;
      }

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
      ItemStack mousedOverItem = null;
      Category mousedOverCategory = null;

      if (!closing) {
        selectedItem = -1;
        for (int i = 0; i < numberOfSlices; i++) {
          float s = (((i - 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
          float e = (((i + 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
          if (a >= s && a < e && d >= radiusIn && d < radiusOut) {
            selectedItem = i;
            break;
          }
        }
      }

      IItemHandler toolbox = toolbox();
      for (int i = 0; i < numberOfSlices; i++) {
        float s = (((i - 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
        float e = (((i + 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
        if (selectedItem == i) {
          drawSlice(
            buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 255, 255, 255, 64
          );
          hasMouseOver = true;
          if (selectedCategory != null) {
            mousedOverItem = toolbox.getStackInSlot(selectedCategory.slotIndices()[i]);
          } else {
            mousedOverCategory = categories.get(i);
          }
        }
        else
          drawSlice(
            buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64
          );
      }

      tessellator.draw();
      GlStateManager.enableTexture2D();

      if (hasMouseOver && mousedOverItem != null) {
        if (!mc.player.getHeldItemMainhand().isEmpty()) {
          if (!mousedOverItem.isEmpty()) {
            drawCenteredString(
              fontRenderer,
              I18n.format("text.immersiveradialmenu.swap"),
              width / 2, (height - fontRenderer.FONT_HEIGHT) / 2,
              0xFFFFFFFF
            );
          } else {
            drawCenteredString(
              fontRenderer,
              I18n.format("text.immersiveradialmenu.insert"),
              width / 2, (height - fontRenderer.FONT_HEIGHT) / 2,
              0xFFFFFFFF
            );
          }
        } else {
          drawCenteredString(
            fontRenderer,
            I18n.format("text.immersiveradialmenu.extract"),
            width / 2, (height - fontRenderer.FONT_HEIGHT) / 2,
            0xFFFFFFFF
          );
        }
      } else if (hasMouseOver && mousedOverCategory != null) {
        drawCenteredString(
          fontRenderer,
          I18n.format(mousedOverCategory.unlocalisedName()),
          width / 2, (height - fontRenderer.FONT_HEIGHT) / 2,
          0xFFFFFFFF
        );
      }

      if (selectedCategory != null) {
        int[] slotIndices = selectedCategory.slotIndices();
        for (int i = 0; i < slotIndices.length; i++) {
          ItemStack inSlot = toolbox.getStackInSlot(slotIndices[i]);
          if (inSlot.getCount() <= 0) {
            float angle1 =
                ((i / (float) selectedCategory.numberOfIndices()) + 0.25f) * 2 * (float) Math.PI;
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
        for (int i = 0; i < slotIndices.length; i++) {
          float angle1 = ((i / (float) selectedCategory.numberOfIndices()) + 0.25f) * 2 * (float) Math.PI;
          float posX = x - 8 + itemRadius * (float) Math.cos(angle1);
          float posY = y - 8 + itemRadius * (float) Math.sin(angle1);
          ItemStack inSlot = toolbox.getStackInSlot(slotIndices[i]);
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
          } else {
            posX = x + itemRadius * (float) Math.cos(angle1);
            posY = y + itemRadius * (float) Math.sin(angle1);
            drawCenteredString(
              fontRenderer,
              I18n.format("text.immersiveradialmenu.empty"),
              (int)posX,
              (int)posY - fontRenderer.FONT_HEIGHT / 2,
              0x7FFFFFFF
            );
          }
        }
        RenderHelper.disableStandardItemLighting();
      } else {
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < categories.size(); i++) {
          float angle1 = ((i / (float) categories.size()) + 0.25f) * 2 * (float) Math.PI;
          float posX = x - 8 + itemRadius * (float) Math.cos(angle1);
          float posY = y - 8 + itemRadius * (float) Math.sin(angle1);
          ItemStack icon = categories.get(i).icon();
          if (icon != null) {
            this.itemRender.renderItemAndEffectIntoGUI(
              icon,
              (int) posX,
              (int) posY
            );
            this.itemRender.renderItemOverlayIntoGUI(
              this.fontRenderer,
              icon,
              (int) posX,
              (int) posY,
              ""
            );
          }
        }
        RenderHelper.disableStandardItemLighting();
      }

      GlStateManager.popMatrix();

      if (mousedOverItem != null && mousedOverItem.getCount() > 0) {
        renderToolTip(mousedOverItem, mouseX, mouseY);
      }
    }
  }

  private void drawSlice(
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

  private boolean hasToolbox() {
    return ToolboxFinder.findToolbox(mc.player) != null;
  }

  private IItemHandler toolbox() {
    return ToolboxFinder.findToolbox(mc.player).handler();
  }

  private List<Category> mainHandCategories() {
    return Category.categoriesFor(mc.player.getHeldItemMainhand());
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
