package korewali.supermassivetech.common.config;

import net.minecraft.client.gui.GuiScreen;
import korewali.supermassivetech.client.config.ConfigGuiSMT;

import com.enderio.core.common.config.BaseConfigFactory;

public class ConfigFactorySMT extends BaseConfigFactory
{
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass()
    {
        return ConfigGuiSMT.class;
    }
}
