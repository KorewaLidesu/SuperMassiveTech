package korewali.supermassivetech.common.tile.abstracts;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import korewali.supermassivetech.client.util.ClientUtils;
import korewali.supermassivetech.common.config.ConfigHandler;
import korewali.supermassivetech.common.util.Utils;

public abstract class TileSMT extends TileEntity
{
    private final float RANGE;
    private final float STRENGTH;
    private final float MAX_GRAV_XZ, MAX_GRAV_Y, MIN_GRAV;
    private int ticksSinceLastParticle = 0;

    /**
     * Sets the tile with all default constant values
     */
    public TileSMT()
    {
        this(1, 1);
    }

    /**
     * Sets the tile with the passed range muliplier and strength multiplier
     */
    public TileSMT(float rangeMult, float strengthMult)
    {
        this(rangeMult, strengthMult, ConfigHandler.maxGravityXZ, ConfigHandler.maxGravityY, ConfigHandler.minGravity);
    }

    /**
     * Fully customizeable constructor, takes in all gravity parameters
     * 
     * @param rangeMult - mulitplier for range, base is set in config
     * @param strengthMult - multiplier for strength, base is set in config
     * @param maxGravXZ - max gravity that can be applied in the X and Z directions
     * @param maxGravY - max gravity that can be applied in the Y direction
     * @param minGrav - minimum gravity to be applied, prevents "bouncing"
     */
    public TileSMT(float rangeMult, float strengthMult, float maxGravXZ, float maxGravY, float minGrav)
    {
        RANGE = ConfigHandler.range * rangeMult;
        STRENGTH = ConfigHandler.strength * strengthMult;
        MAX_GRAV_XZ = maxGravXZ;
        MAX_GRAV_Y = maxGravY;
        MIN_GRAV = minGrav;
    }

    @Override
    public void updateEntity()
    {
        if (isGravityWell() && ConfigHandler.doGravityWell)
        {
            float range = getRange() * getRangeMultiplier();
            for (Object o : worldObj.getEntitiesWithinAABB(
                    Entity.class,
                    AxisAlignedBB.getBoundingBox(xCoord + 0.5 - range, yCoord + 0.5 - range, zCoord + 0.5 - range, xCoord + 0.5 + range, yCoord + 0.5 + range, zCoord + 0.5
                            + range)))
            {
                Utils.applyGravity(getStrength() * getStrengthMultiplier(), getMaxGravXZ(), getMaxGravY(), getMinGrav(), range, (Entity) o, this, showParticles());
            }

            if (worldObj != null && worldObj.isRemote && ticksSinceLastParticle >= 4 && showParticles())
            {
                double x = getRand(), y = getRand(), z = getRand();

                ClientUtils.spawnGravityParticle(xCoord, yCoord, zCoord, x, y, z);
                ticksSinceLastParticle = 0;
            }
            else if (ticksSinceLastParticle < 4)
                ticksSinceLastParticle++;
            else
                ticksSinceLastParticle = 0;
        }
    }

    private double getRand()
    {
        double num = (worldObj.rand.nextFloat() * (getRange() * 2) - getRange());
        if (num < 0 && num > -1.0)
            num = -1.0f;
        if (num > 0 && num < 1.0)
            num = 1.0f;
        return num;
    }

    /**
     * The multiplier to be applied to the strength of the gravity, used for gravity dependant on stored amount of items, configuration, etc.
     * 
     * @return a float to multiply the strength with, defaults to 1.
     */
    protected float getStrengthMultiplier()
    {
        return 1;
    }

    /**
     * The multiplier to be applied to the range of the gravity, used for gravity dependant on stored amount of items, configuration, etc.
     * 
     * @return a float to multiply the range with, defaults to 1.
     */
    protected float getRangeMultiplier()
    {
        return 1;
    }

    /**
     * Whether this tile is a gravity well, that is, whether to apply gravity to surrounding entities.
     */
    public abstract boolean isGravityWell();

    public abstract boolean showParticles();

    public float getRange()
    {
        return RANGE;
    }

    public float getStrength()
    {
        return STRENGTH;
    }

    public float getMaxGravXZ()
    {
        return MAX_GRAV_XZ;
    }

    public float getMaxGravY()
    {
        return MAX_GRAV_Y;
    }

    public float getMinGrav()
    {
        return MIN_GRAV;
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
}
