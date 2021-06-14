package korewali.supermassivetech.client.config;

import net.minecraft.client.gui.GuiScreen;
import korewali.supermassivetech.ModProps;
import korewali.supermassivetech.common.config.ConfigHandler;
import korewali.supermassivetech.common.util.Utils;

import com.enderio.core.api.common.config.IConfigHandler;
import com.enderio.core.client.config.BaseConfigGui;

public class ConfigGuiSMT extends BaseConfigGui
{
    public ConfigGuiSMT(GuiScreen parentScreen)
    {
        super(parentScreen);
    }   
    
    @Override
    protected String getTitle()
    {
        return Utils.lang.localize("config.title");
    }
    
    @Override
    protected String getLangPrefix()
    {
        return ModProps.LOCALIZING + ".config.";
    }
    
    @Override
    protected IConfigHandler getConfigHandler()
    {
        return ConfigHandler.INSTANCE;
    }
}
