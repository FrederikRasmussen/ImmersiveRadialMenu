package immersiveradialmenu;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import immersiveradialmenu.network.SwapItems;

@Mod(
  modid = ImmersiveRadialMenu.MODID,
  name = ImmersiveRadialMenu.NAME,
  version = ImmersiveRadialMenu.VERSION
)
public class ImmersiveRadialMenu {
  public static final String MODID = "immersiveradialmenu";
  public static final String NAME = "Immersive Radial Menu";
  public static final String VERSION = "@VERSION@";

  @Mod.Instance(MODID)
  public static ImmersiveRadialMenu instance;

  public static SimpleNetworkWrapper channel;

  @SidedProxy(
    clientSide = "immersiveradialmenu.client.ClientProxy",
    serverSide = "immersiveradialmenu.CommonProxy"
  )
  public static CommonProxy proxy;

  public ImmersiveRadialMenu() {
  }

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    channel = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
    int messageNumber = 0;
    channel.registerMessage(
      SwapItems.Handler.class,
      SwapItems.class,
      messageNumber++,
      Side.SERVER
    );
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    proxy.init();
  }
}
