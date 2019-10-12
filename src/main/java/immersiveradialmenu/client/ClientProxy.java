package immersiveradialmenu.client;

import java.util.function.Predicate;

import org.lwjgl.input.Keyboard;

import blusunrize.immersiveengineering.api.tool.ToolboxHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import immersiveradialmenu.CommonProxy;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
  public static KeyBinding keyOpenToolMenu;
  public static KeyBinding keyOpenFoodMenu;
  public static KeyBinding keyOpenWiringMenu;

  @Override
  public void init() {
    keyOpenToolMenu =
        new KeyBinding(
          "key.immersiveradialmenu.opentool",
          Keyboard.KEY_R,
          "key.immersiveradialmenu.category"
        );
    ClientRegistry.registerKeyBinding(keyOpenToolMenu);

    keyOpenFoodMenu =
        new KeyBinding(
          "key.immersiveradialmenu.openfood",
          Keyboard.KEY_F,
          "key.immersiveradialmenu.category"
        );
    ClientRegistry.registerKeyBinding(keyOpenFoodMenu);
    
    keyOpenWiringMenu =
        new KeyBinding(
          "key.immersiveradialmenu.openwiring",
          Keyboard.KEY_G,
          "key.immersiveradialmenu.category"
        );
    ClientRegistry.registerKeyBinding(keyOpenWiringMenu);
  }

  @SubscribeEvent
  public static void handleKeys(InputEvent ev) {
    handleKeybind(
      keyOpenToolMenu,
      itemStack -> ToolboxHandler.isTool(itemStack)
    );
    handleKeybind(
      keyOpenFoodMenu,
      itemStack -> ToolboxHandler.isFood(itemStack)
    );
    final World world = Minecraft.getMinecraft().world;
    handleKeybind(
      keyOpenToolMenu,
      itemStack -> ToolboxHandler.isWiring(itemStack, world)
    );
  }

  public static void wipeOpen()
  {
      while (
        keyOpenToolMenu.isPressed()
        || keyOpenFoodMenu.isPressed()
        || keyOpenWiringMenu.isPressed()
      ) {
        
      }
  }

  private static void handleKeybind(
    KeyBinding keybind,
    Predicate<ItemStack> predicate
  ) {
    Minecraft mc = Minecraft.getMinecraft();
    while (keybind.isPressed())
      if (mc.currentScreen == null) {
        ItemStack inHand = mc.player.getHeldItemMainhand();
        if (predicate.test(inHand))
          mc.displayGuiScreen(new GuiRadialMenu(predicate));
      }
  }
}

/*
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
