package korewali.supermassivetech.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import korewali.supermassivetech.api.common.item.IAdvancedTooltip;
import korewali.supermassivetech.common.entity.EntityDyingBlock;
import korewali.supermassivetech.common.entity.item.EntityItemStarHeart;
import korewali.supermassivetech.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemStarHeart extends ItemSMT implements IAdvancedTooltip
{
    public ItemStarHeart(String unlocName)
    {
        super(unlocName, "starHeart");
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack)
    {
        EntityItemStarHeart entity = new EntityItemStarHeart(world, location.posX, location.posY, location.posZ, itemstack, location.motionX, location.motionY,
                location.motionZ, ((EntityItem) location).delayBeforeCanPickup);
        entity.func_145799_b(world.getClosestPlayerToEntity(location, -1).getCommandSenderName());
        return entity;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack)
    {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getHiddenLines(ItemStack stack)
    {
        return Utils.lang.localize("tooltip.starHeart");
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return stack.getItemDamage() != 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getStaticLines(ItemStack stack)
    {
        return null;
    }

    @Override
    public boolean onItemUse(ItemStack p_77648_1_, EntityPlayer p_77648_2_, World world, int x, int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_,
            float p_77648_10_)
    {
        if (!world.isRemote)
        {
            EntityDyingBlock e = new EntityDyingBlock(world, world.getBlock(x, y, z), world.getBlockMetadata(x, y, z), x, y + 1, z);
            world.spawnEntityInWorld(e);
        }
        return true;
    }
}
