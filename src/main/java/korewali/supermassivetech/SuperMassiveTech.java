package korewali.supermassivetech;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import korewali.supermassivetech.client.gui.GuiHandler;
import korewali.supermassivetech.common.CommonProxy;
import korewali.supermassivetech.common.compat.RarityAdjuster;
import korewali.supermassivetech.common.config.ConfigHandler;
import korewali.supermassivetech.common.network.PacketHandler;
import korewali.supermassivetech.common.registry.Achievements;
import korewali.supermassivetech.common.registry.ModBlocks;
import korewali.supermassivetech.common.registry.ModEnchants;
import korewali.supermassivetech.common.registry.ModEntities;
import korewali.supermassivetech.common.registry.ModItems;
import korewali.supermassivetech.common.registry.Stars;

import com.enderio.core.IEnderMod;
import com.enderio.core.common.compat.CompatRegistry;
import com.enderio.core.common.util.CreativeTabsCustom;
import com.enderio.core.common.util.RegisterTime;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

/**
 * @author Garrett Spicer-Davis
 */
@Mod(modid = ModProps.MODID, name = ModProps.MOD_NAME, version = ModProps.VERSION, dependencies = ModProps.DEPENDENCIES, guiFactory = ModProps.GUI_FACTORY_CLASS)
public class SuperMassiveTech implements IEnderMod
{
    @Instance
    public static SuperMassiveTech instance;

    @SidedProxy(clientSide = ModProps.CLIENT_PROXY_CLASS, serverSide = ModProps.SERVER_PROXY_CLASS)
    public static CommonProxy proxy;

    public static final Logger logger = LogManager.getLogger("SuperMassiveTech");

    public static final ModItems itemRegistry = ModItems.instance;
    public static final ModEnchants enchantRegistry = ModEnchants.instance;
    public static final ModBlocks blockRegistry = ModBlocks.instance;
    public static final ModEntities entityRegistry = ModEntities.instance;
    public static final Stars starRegistry = Stars.instance;

    public static CreativeTabsCustom tabSMT = new CreativeTabsCustom(ModProps.MODID);

    public static int renderIDStorage, renderIDHopper, renderIDStarHarvester, renderIDWaypoint, renderIDBlackHole, renderIDCharger;

    public SuperMassiveTech()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event)
    {
        ConfigHandler.INSTANCE.initialize(event.getSuggestedConfigurationFile());

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        proxy.preInit();

        itemRegistry.register();
        blockRegistry.register();
        entityRegistry.init();

        proxy.registerRenderers();

        enchantRegistry.init();

        Achievements.initAchievements();

        starRegistry.registerDefaultStars();

        FMLInterModComms.sendMessage("Waila", "register", ModProps.MAIN_PACKAGE + ".common.compat.waila.WailaCompat.load");

        CompatRegistry.INSTANCE.registerCompat(RegisterTime.INIT, ModProps.MAIN_PACKAGE + ".common.compat.enderio.EnderIOCompat", "EnderIO");
    }

    @EventHandler
    public static void init(FMLInitializationEvent event)
    {
        PacketHandler.init();

        itemRegistry.addRecipes();
        blockRegistry.addRecipes();
    }

    @EventHandler
    public static void postInit(FMLPostInitializationEvent event)
    {
        RarityAdjuster.fix();
    }

    @Override
    public String modid()
    {
        return ModProps.MODID;
    }

    @Override
    public String name()
    {
        return ModProps.MOD_NAME;
    }

    @Override
    public String version()
    {
        return ModProps.VERSION;
    }
}
