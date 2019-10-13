package immersiveradialmenu.client;

import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import immersiveradialmenu.Category;
import immersiveradialmenu.CommonProxy;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
  public static KeyBinding keyOpenToolboxMenu;

  @Override
  public void init() {
    keyOpenToolboxMenu =
        new KeyBinding(
          "key.immersiveradialmenu.opentoolbox",
          Keyboard.KEY_R,
          "key.immersiveradialmenu.category"
        );
    ClientRegistry.registerKeyBinding(keyOpenToolboxMenu);
  }

  @SubscribeEvent
  public static void handleKeys(InputEvent ev) {
    while (keyOpenToolboxMenu.isPressed()) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.currentScreen == null) {
        ItemStack inHand = mc.player.getHeldItemMainhand();
        List<Category> categories = Category.categoriesFor(inHand);
        if (!categories.isEmpty()) {
          mc.displayGuiScreen(
            new GuiRadialMenu(keyOpenToolboxMenu)
          );
        }
      }
    }
  }

  public static void wipeOpen()
  {
      while (keyOpenToolboxMenu.isPressed()) {
        
      }
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
