package korewali.supermassivetech.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import korewali.supermassivetech.client.gui.GuiWaypoint;
import korewali.supermassivetech.common.tile.TileWaypoint;

public class GuiHelper
{
    public static void openWaypointGui(World world, int x, int y, int z)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiWaypoint((TileWaypoint) world.getTileEntity(x, y, z)));
    }
}
