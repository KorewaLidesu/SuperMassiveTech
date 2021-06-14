package korewali.supermassivetech.common.entity.item;

import java.util.LinkedList;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import korewali.supermassivetech.common.network.PacketHandler;
import korewali.supermassivetech.common.network.message.MessageStarHeartParticle;
import korewali.supermassivetech.common.registry.Achievements;
import korewali.supermassivetech.common.util.Utils;

import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.util.ItemUtil;
import com.enderio.core.common.util.blockiterators.CubicBlockIterator;

import cpw.mods.fml.common.ObfuscationReflectionHelper;

import static korewali.supermassivetech.SuperMassiveTech.*;

public class EntityItemStarHeart extends EntityItemIndestructible
{
    public EntityItemStarHeart(World world)
    {
        super(world);
    }

    public EntityItemStarHeart(World world, double posX, double posY, double posZ, ItemStack itemstack, double motionX, double motionY, double motionZ, int delay)
    {
        super(world, posX, posY, posZ, itemstack, motionX, motionY, motionZ, delay);
    }

    private boolean ready;
    private int explodeTimer = -1, particlesLeft = 0, powerLevel, postTimer = 20;
    private final int TIMER_MAX = 60, RADIUS = 10;
    private BlockCoord toRemove = null;

    private LinkedList<BlockCoord> fire = new LinkedList<BlockCoord>();

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (worldObj.isRemote)
        {
            return;
        }

        if (explodeTimer < 0)
        {
            if (this.isBurning() && ready)
            {
                explodeTimer = TIMER_MAX;
            }
            else
                ready = !this.isBurning();
        }
        else if (explodeTimer == TIMER_MAX)
        {
            getFire();
            delayBeforeCanPickup = 1000000;
            explodeTimer--;
            powerLevel = fire.size();
        }
        else if (explodeTimer == 0)
        {
            if (!fire.isEmpty())
                explodeTimer++;
            else if (postTimer > 0)
                postTimer--;
            else
                changeToStar();
        }

        if (explodeTimer > 0 && explodeTimer < TIMER_MAX)
        {
            if (fire.size() > 0)
            {
                if (particlesLeft <= 0)
                {
                    toRemove = fire.remove(new Random().nextInt(fire.size()));

                    particlesLeft = extinguish(toRemove) ? 4 + new Random().nextInt(2) - 1 : 0;
                    explodeTimer--;
                }
                else
                {
                    sendParticlePacket(toRemove.x, toRemove.y, toRemove.z);
                    particlesLeft--;
                }
            }
            else
            {
                explodeTimer = 0;
            }
        }
    }

    private void sendParticlePacket(int x, int y, int z)
    {
        PacketHandler.INSTANCE.sendToAll(new MessageStarHeartParticle((int) posX, (int) posY, (int) posZ, x, y, z));
    }

    private void changeToStar()
    {
        ItemStack star = new ItemStack(itemRegistry.star, this.getEntityItem().stackSize);

        // Sets the type of the star to a random type
        Utils.setType(star, starRegistry.getRandomStarFromType(starRegistry.getWeightedCreationTier(powerLevel)));

        worldObj.newExplosion(this, posX, posY, posZ, 3.0f + (this.getEntityItem().stackSize), true, true);

        EntityItemIndestructible starEntity = new EntityItemIndestructible(worldObj, posX, posY, posZ, star, 0, 0, 0, 0);
        EntityItemIndestructible depletedEntity = new EntityItemDepletedNetherStar(worldObj, posX, posY, posZ, new ItemStack(itemRegistry.depletedNetherStar, star.stackSize),
                0, 0, 0, 0);

        starEntity.func_145799_b(this.func_145800_j());
        depletedEntity.func_145799_b(this.func_145800_j());

        ItemUtil.spawnItemInWorldWithRandomMotion(starEntity);
        ItemUtil.spawnItemInWorldWithRandomMotion(depletedEntity);

        Achievements.unlock(Achievements.getValidItemStack(star), (EntityPlayerMP) worldObj.getPlayerEntityByName(this.func_145800_j()));

        this.setDead();
    }

    @Override
    public boolean isBurning()
    {
        boolean flag = this.worldObj != null && this.worldObj.isRemote;
        // TODO PR forge or AT
        Integer fire = ObfuscationReflectionHelper.getPrivateValue(Entity.class, this, "fire", "field_70151_c");
        return (fire > 0 || flag && this.getFlag(0));
    }

    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
    {
        if (par1DamageSource.isFireDamage())
            return false;
        else
            return super.attackEntityFrom(par1DamageSource, par2);
    }

    private void getFire()
    {
        CubicBlockIterator iter = new CubicBlockIterator(new BlockCoord(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)), RADIUS);
        while (iter.hasNext())
        {
            BlockCoord coord = iter.next();
            if (coord.getBlock(worldObj) == Blocks.fire)
            {
                fire.add(coord);
            }
        }
    }

    private boolean extinguish(BlockCoord coord)
    {
        if (worldObj.getBlock(toRemove.x, toRemove.y, toRemove.z) == Blocks.fire)
        {
            worldObj.setBlockToAir(toRemove.x, toRemove.y, toRemove.z);
            return true;
        }
        return false;
    }
}
