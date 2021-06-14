package korewali.supermassivetech.common.registry;

import korewali.supermassivetech.SuperMassiveTech;
import korewali.supermassivetech.common.entity.EntityDyingBlock;
import korewali.supermassivetech.common.entity.EntityFormingStar;
import korewali.supermassivetech.common.entity.item.EntityItemDepletedNetherStar;
import korewali.supermassivetech.common.entity.item.EntityItemIndestructible;
import korewali.supermassivetech.common.entity.item.EntityItemStar;
import korewali.supermassivetech.common.entity.item.EntityItemStarHeart;
import cpw.mods.fml.common.registry.EntityRegistry;

public class ModEntities
{
    public static final ModEntities instance = new ModEntities();

    private ModEntities()
    {}

    public void init()
    {
        int id = 0;

        EntityRegistry.registerModEntity(EntityItemDepletedNetherStar.class, "korewali.smt.entityDepletedNetherStar", id++, SuperMassiveTech.instance, 80, 80, true);
        EntityRegistry.registerModEntity(EntityItemStar.class, "korewali.smt.entitySpecialStar", id++, SuperMassiveTech.instance, 80, 80, true);
        EntityRegistry.registerModEntity(EntityItemStarHeart.class, "korewali.smt.entityStarHeart", id++, SuperMassiveTech.instance, 80, 80, true);
        EntityRegistry.registerModEntity(EntityItemIndestructible.class, "korewali.smt.entityItemIndestructible", id++, SuperMassiveTech.instance, 80, 80, true);
        EntityRegistry.registerModEntity(EntityFormingStar.class, "korewali.smt.entityFormingStar", id++, SuperMassiveTech.instance, 80, 3, false);
        EntityRegistry.registerModEntity(EntityDyingBlock.class, "korewali.smt.entityDyingBlock", id++, SuperMassiveTech.instance, 80, 3, true);
    }
}
