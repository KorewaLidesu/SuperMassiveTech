package korewali.supermassivetech.common.item;

import net.minecraft.item.Item;
import korewali.supermassivetech.ModProps;
import korewali.supermassivetech.SuperMassiveTech;

public class ItemSMT extends Item
{
    public ItemSMT(String unlocName, String textureName)
    {
        super();

        setCreativeTab(SuperMassiveTech.tabSMT);
        setUnlocalizedName(unlocName);
        setTextureName(ModProps.MODID + ":" + textureName);
    }
}
