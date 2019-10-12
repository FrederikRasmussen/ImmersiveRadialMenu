package immersiveradialmenu;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
  modid = ImmersiveRadialMenu.MODID,
  name = ImmersiveRadialMenu.NAME,
  version = ImmersiveRadialMenu.VERSION
)
public class ImmersiveRadialMenu {
  public static final String MODID = "immersiveradialmenu";
  public static final String NAME = "Immersive Radial Menu";
  public static final String VERSION = "@VERSION@";

  @SidedProxy(
    clientSide = "immersiveradialmenu.client.ClientProxy",
    serverSide = "immersiveradialmenu.CommonProxy"
  )
  public static CommonProxy proxy;

  public ImmersiveRadialMenu() {
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    proxy.init();
  }
}
