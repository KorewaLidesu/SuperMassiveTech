package korewali.supermassivetech.common.tile;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import korewali.supermassivetech.api.common.compat.IWailaAdditionalInfo;
import korewali.supermassivetech.common.util.Utils;
import korewali.supermassivetech.common.util.Waypoint;

public class TileWaypoint extends TileEntity implements IWailaAdditionalInfo
{
    public Waypoint waypoint;

    public TileWaypoint()
    {
        waypoint = new Waypoint();
    }

    public void init(EntityPlayer... players)
    {
        waypoint = new Waypoint("Unnamed", this.xCoord, this.yCoord, this.zCoord, players);
    }

    @Override
    public void updateEntity()
    {
        if (worldObj.isRemote)
        {
            if (waypoint == null || waypoint.isNull() || Waypoint.waypoints.contains(waypoint))
                return;

            Waypoint.waypoints.add(waypoint);
        }
    }

    public void addPlayer(EntityPlayer player)
    {
        Waypoint.waypoints.remove(waypoint);

        waypoint.addPlayer(player);

        Waypoint.waypoints.add(waypoint);

        this.markDirty();
    }

    @Override
    public boolean canUpdate()
    {
        return waypoint == null || waypoint.isNull() || !Waypoint.waypoints.contains(this.waypoint);
    }

    @Override
    public void invalidate()
    {
        Waypoint.waypoints.remove(waypoint);
        super.invalidate();
    }

    @Override
    public double getMaxRenderDistanceSquared()
    {
        return 65536.0D;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    public int[] getColorArr()
    {
        return waypoint.isNull() ? new int[] { 255, 255, 255 } : new int[] { waypoint.getColor().getRed(), waypoint.getColor().getGreen(), waypoint.getColor().getBlue() };
    }

    public void setInternalWaypointName(String newName)
    {
        if (!waypoint.isNull())
            waypoint.setName(newName);
    }

    @Override
    public boolean shouldRenderInPass(int pass)
    {
        return pass > 0;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        waypoint.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        waypoint = waypoint.readFromNBT(tag);
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        this.readFromNBT(pkt.func_148857_g());
    }

    @Override
    public void getWailaInfo(List<String> tooltip, int x, int y, int z, World world)
    {
        if (!waypoint.isNull())
        {
            tooltip.add("");

            tooltip.add(EnumChatFormatting.WHITE + Utils.lang.localize("tooltip.name") + ": " + waypoint.getName());
            tooltip.add(EnumChatFormatting.RED + Utils.lang.localize("tooltip.red") + ": " + waypoint.getColor().getRed());
            tooltip.add(EnumChatFormatting.GREEN + Utils.lang.localize("tooltip.green") + ": " + waypoint.getColor().getGreen());
            tooltip.add(EnumChatFormatting.BLUE + Utils.lang.localize("tooltip.blue") + ": " + waypoint.getColor().getBlue());

            tooltip.add("");
        }
    }
}
