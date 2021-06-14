package korewali.supermassivetech.client;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.obj.WavefrontObject;
import korewali.supermassivetech.ModProps;
import korewali.supermassivetech.client.model.ModelBlackHoleStorage;
import korewali.supermassivetech.client.render.BlackHoleSpecialRenderer;
import korewali.supermassivetech.client.render.ChargerSpecialRenderer;
import korewali.supermassivetech.client.render.RenderStarHarvester;
import korewali.supermassivetech.client.render.WaypointSpecialRenderer;
import korewali.supermassivetech.client.render.entity.RenderDyingBlock;
import korewali.supermassivetech.client.render.entity.RenderFormingStar;
import korewali.supermassivetech.common.CommonProxy;
import korewali.supermassivetech.common.entity.EntityDyingBlock;
import korewali.supermassivetech.common.entity.EntityFormingStar;
import korewali.supermassivetech.common.tile.TileBlackHole;
import korewali.supermassivetech.common.tile.TileBlackHoleHopper;
import korewali.supermassivetech.common.tile.TileBlackHoleStorage;
import korewali.supermassivetech.common.tile.TileWaypoint;
import korewali.supermassivetech.common.tile.energy.TileCharger;
import korewali.supermassivetech.common.tile.energy.TileStarHarvester;

import com.enderio.core.client.render.DirectionalModelRenderer;
import com.enderio.core.client.render.SimpleModelRenderer;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

import static korewali.supermassivetech.SuperMassiveTech.*;

public class ClientProxy extends CommonProxy
{
    public static DirectionalModelRenderer<TileBlackHoleStorage> storage;

    public static DirectionalModelRenderer<TileBlackHoleHopper> hopper;

    public static RenderStarHarvester starHarvester;

    public static WaypointSpecialRenderer beacon;
    public static SimpleModelRenderer waypoint;

    public static SimpleModelRenderer charger;
    public static ChargerSpecialRenderer chargerSpecial;

    public static BlackHoleSpecialRenderer blackHole;

    @Override
    public void preInit()
    {
        renderIDStorage = RenderingRegistry.getNextAvailableRenderId();
        renderIDHopper = RenderingRegistry.getNextAvailableRenderId();
        renderIDStarHarvester = RenderingRegistry.getNextAvailableRenderId();
        renderIDWaypoint = RenderingRegistry.getNextAvailableRenderId();
        renderIDBlackHole = RenderingRegistry.getNextAvailableRenderId();
        renderIDCharger = RenderingRegistry.getNextAvailableRenderId();
    }

    @Override
    public void registerRenderers()
    {
        storage = new DirectionalModelRenderer<TileBlackHoleStorage>(new ModelBlackHoleStorage(),
                new ResourceLocation(ModProps.MOD_TEXTUREPATH, "textures/models/storage.png"));
        hopper = new DirectionalModelRenderer<TileBlackHoleHopper>(new ResourceLocation(ModProps.MOD_TEXTUREPATH, "models/newHopper.obj"), new ResourceLocation(
                ModProps.MOD_TEXTUREPATH, "textures/models/hopper.png"));
        starHarvester = new RenderStarHarvester(new ResourceLocation(ModProps.MOD_TEXTUREPATH, "models/starHarvesterMain.obj"), new ResourceLocation(ModProps.MOD_TEXTUREPATH,
                "models/starHarvesterSphere.obj"), new ResourceLocation(ModProps.MOD_TEXTUREPATH, "models/ring.obj"));
        waypoint = new SimpleModelRenderer(new WavefrontObject(new ResourceLocation(ModProps.MOD_TEXTUREPATH, "models/waypoint.obj")), renderIDWaypoint);
        beacon = new WaypointSpecialRenderer();
        charger = new SimpleModelRenderer(new WavefrontObject(new ResourceLocation(ModProps.MOD_TEXTUREPATH, "models/charger.obj")), renderIDCharger);
        chargerSpecial = new ChargerSpecialRenderer();
        blackHole = new BlackHoleSpecialRenderer();

        // BHS
        ClientRegistry.bindTileEntitySpecialRenderer(TileBlackHoleStorage.class, storage);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(blockRegistry.blackHoleStorage), storage);

        // BHH
        ClientRegistry.bindTileEntitySpecialRenderer(TileBlackHoleHopper.class, hopper);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(blockRegistry.blackHoleHopper), hopper);

        // Star Harvester
        ClientRegistry.bindTileEntitySpecialRenderer(TileStarHarvester.class, starHarvester);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(blockRegistry.starHarvester), starHarvester);

        // Charger
        ClientRegistry.bindTileEntitySpecialRenderer(TileCharger.class, chargerSpecial);
        RenderingRegistry.registerBlockHandler(charger);

        // Waypoint
        ClientRegistry.bindTileEntitySpecialRenderer(TileWaypoint.class, beacon);
        RenderingRegistry.registerBlockHandler(waypoint);

        // Black Hole
        ClientRegistry.bindTileEntitySpecialRenderer(TileBlackHole.class, blackHole);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(blockRegistry.starHarvester), starHarvester);

        // Entities
        RenderingRegistry.registerEntityRenderingHandler(EntityFormingStar.class, new RenderFormingStar());
        RenderingRegistry.registerEntityRenderingHandler(EntityDyingBlock.class, new RenderDyingBlock());
    }
}
