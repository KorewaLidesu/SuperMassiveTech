package korewali.supermassivetech.common.item.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import korewali.supermassivetech.api.common.item.IAdvancedTooltip;
import korewali.supermassivetech.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockHopper extends ItemBlockSMT implements IAdvancedTooltip
{
    public ItemBlockHopper(Block block)
    {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getHiddenLines(ItemStack stack)
    {
        return Utils.lang.localize("tooltip.blackHoleHopper");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getStaticLines(ItemStack stack)
    {
        if (stack.stackTagCompound == null)
            return null;

        ItemStack cfg = ItemStack.loadItemStackFromNBT(stack.stackTagCompound.getCompoundTag("inventory1"));
        ItemStack stored = ItemStack.loadItemStackFromNBT(stack.stackTagCompound.getCompoundTag("inventory0"));
        List<String> strs = new ArrayList<String>();

        if (stored != null)
            strs.add(String.format("%s: %d %s", Utils.lang.localize("tooltip.stored"), stored.stackSize, stored.getDisplayName()));
        if (cfg != null)
            strs.add(String.format("%s: %s", Utils.lang.localize("tooltip.configuration"), cfg.getDisplayName()));

        if (strs.isEmpty())
        {
            return null;
        }
        else
        {
            return Utils.makeTooltipString(strs);
        }
    }

}
