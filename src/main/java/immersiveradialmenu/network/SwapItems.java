package immersiveradialmenu.network;

import blusunrize.immersiveengineering.api.tool.ToolboxHandler;
import blusunrize.immersiveengineering.common.items.ItemToolbox;
import immersiveradialmenu.Category;
import immersiveradialmenu.ToolboxFinder;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class SwapItems implements IMessage {
  public int swapWith;

  public SwapItems() {}

  public SwapItems(int swapWith) {
    this.swapWith = swapWith;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    swapWith = buf.readInt();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(swapWith);
  }

  public static void swapItem(int toolboxSlot, EntityPlayer player) {
    ItemStack toolboxStack = player.getHeldItemOffhand();
    if (!(toolboxStack.getItem() instanceof ItemToolbox))
      return;
    IItemHandler toolbox = toolboxStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

    if (toolbox == null)
      return;
    if (toolboxSlot < 0)
      return;

    ItemStack inHand = player.getHeldItemMainhand();
    if (!Category.itemStackValidInSlot(inHand, toolboxSlot))
      return;

    ItemStack inSlot = toolbox.getStackInSlot(toolboxSlot);
    player.setHeldItem(EnumHand.MAIN_HAND, inSlot);
    ((IItemHandlerModifiable)toolbox).setStackInSlot(toolboxSlot, inHand);
    player.setHeldItem(EnumHand.OFF_HAND, toolboxStack);
  }

  public static class Handler implements IMessageHandler<SwapItems, IMessage> {
    @Override
    public IMessage onMessage(final SwapItems message, MessageContext ctx) {
      final EntityPlayerMP player = ctx.getServerHandler().player;
      final WorldServer world = (WorldServer) player.world;

      world.addScheduledTask(() -> swapItem(message.swapWith, player));

      return null; // no response in this case
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
