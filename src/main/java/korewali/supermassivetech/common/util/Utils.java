package korewali.supermassivetech.common.util;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;

import korewali.supermassivetech.ModProps;
import korewali.supermassivetech.SuperMassiveTech;
import korewali.supermassivetech.api.common.item.IAdvancedTooltip;
import korewali.supermassivetech.api.common.item.IStarItem;
import korewali.supermassivetech.api.common.registry.IStar;
import korewali.supermassivetech.client.util.ClientUtils;
import korewali.supermassivetech.common.config.ConfigHandler;
import korewali.supermassivetech.common.handlers.GravityArmorHandler;
import korewali.supermassivetech.common.item.ItemGravityArmor;
import korewali.supermassivetech.common.item.ItemStar;
import korewali.supermassivetech.common.network.message.MessageUpdateGravityArmor.PowerUps;
import korewali.supermassivetech.common.registry.Stars;
import korewali.supermassivetech.common.tile.TileBlackHoleStorage;
import cofh.api.energy.IEnergyContainerItem;

import com.enderio.core.common.Lang;
import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.util.EnderStringUtils;
import com.enderio.core.common.util.EntityUtil;

import static korewali.supermassivetech.SuperMassiveTech.itemRegistry;

public class Utils
{
    private static Stars stars = Stars.instance;
    private static Material[] pickMats = { Material.rock, Material.iron, Material.anvil };
    private static Material[] shovelMats = { Material.clay, Material.snow, Material.ground };

    public static final Random rand = new Random();

    public static final Lang lang = new Lang(ModProps.LOCALIZING);

    /**
     * Formats a string and number for use in GUIs and tooltips
     * 
     * @param prefix
     *            - The string to put before the formatted number
     * @param suffix
     *            - The string to put after the formatted number
     * @param amnt
     *            - The number to be formatted
     * @param useDecimals
     *            - Whether or not to use decimals in the representation
     * @param formatK
     *            - Whether or not to format the thousands
     * @return
     */
    public static String formatStringForBHS(String prefix, String suffix, long amnt, boolean useDecimals, boolean formatK)
    {
        if (amnt == TileBlackHoleStorage.max)
        {
            prefix += "2^40" + suffix;
            return prefix;
        }

        return EnderStringUtils.formatString(prefix, suffix, amnt, useDecimals, formatK);
    }

    /**
     * Applies gravity to an entity with the passed configurations
     * 
     * @param gravStrength
     *            - Strength of the gravity, usually a number < 3
     * @param maxGravXZ
     *            - Max gravity that can be applied in the X and Z directions
     * @param maxGravY
     *            - Max gravity that can be applied in the Y direction
     * @param minGrav
     *            - Minimum gravity that can be applied (prevents "wobbling" if
     *            such a thing ever exists)
     * @param range
     *            - The range of the gravitational effects
     * @param entity
     *            - Entity to effect
     * @param xCoord
     *            - X coord of the center of gravity
     * @param yCoord
     *            - Y coord of the center of gravity
     * @param zCoord
     *            - Z coord of the center of gravity
     */
    public static void applyGravity(float gravStrength, float maxGravXZ, float maxGravY, float minGrav, float range, Entity entity, int xCoord,
            int yCoord, int zCoord, boolean showParticles)
    {
        // distance forumla
        double dist = Math.sqrt(Math.pow(xCoord + 0.5 - entity.posX, 2) + Math.pow(zCoord + 0.5 - entity.posZ, 2)
                + Math.pow(yCoord + 0.5 - entity.posY, 2));

        if (dist > range)
            return;

        double xDisplacment = entity.posX - (xCoord + 0.5);
        double yDisplacment = entity.posY - (yCoord + 0.5);
        double zDisplacment = entity.posZ - (zCoord + 0.5);

        // http://en.wikipedia.org/wiki/Spherical_coordinate_system#Coordinate_system_conversions
        double theta = Math.acos(zDisplacment / dist);
        double phi = Math.atan2(yDisplacment, xDisplacment);

        // Gravity decreases linearly
        double gravForce = gravStrength * (1 - dist / range);

        if (entity instanceof EntityPlayer)
        {
            if (((EntityPlayer) entity).capabilities.isCreativeMode)
                return;

            // instant half gravity
            gravForce *= 0.5;

            double armorMult = 1.0;
            for (ItemStack s : ((EntityPlayer) entity).inventory.armorInventory)
            {
                // handles gravity armor
                if (s != null && itemRegistry.armors.contains(s.getItem()))
                {
                    IEnergyContainerItem item = (IEnergyContainerItem) s.getItem();
                    if (item.getEnergyStored(s) > 0)
                    {
                        item.extractEnergy(s, (int) (ConfigHandler.gravArmorDrain * gravForce) / 20, false);
                        armorMult -= 0.23;
                    }
                }
                // handles enchant
                else if (s != null && EnchantmentHelper.getEnchantmentLevel(ConfigHandler.gravEnchantID, s) != 0)
                {
                    armorMult -= 0.23 / 2;
                    s.damageItem(new Random().nextInt(100) < 2 && !entity.worldObj.isRemote ? 1 : 0, (EntityLivingBase) entity);
                }
            }

            gravForce *= armorMult;

        }
        else
        {
            gravForce *= 2;
        }

        double vecX = -gravForce * Math.sin(theta) * Math.cos(phi);
        double vecY = -gravForce * Math.sin(theta) * Math.sin(phi);
        double vecZ = -gravForce * Math.cos(theta);

        // trims gravity above max
        if (Math.abs(vecX) > maxGravXZ)
            vecX *= maxGravXZ / Math.abs(vecX);
        if (Math.abs(vecY) > maxGravY)
            vecY *= maxGravY / Math.abs(vecY);
        if (Math.abs(vecZ) > maxGravXZ)
            vecZ *= maxGravXZ / Math.abs(vecZ);

        // trims gravity below min
        if (Math.abs(vecX) < minGrav)
            vecX = 0;
        if (Math.abs(vecY) < minGrav)
            vecY = 0;
        if (Math.abs(vecZ) < minGrav)
            vecZ = 0;

        EntityUtil.setEntityVelocity(entity, entity.motionX + vecX, entity.motionY + vecY, entity.motionZ + vecZ);

        showParticles &= dist > 1;

        // shows smoke particles

        if (showParticles && entity.worldObj.isRemote)
        {
            ClientUtils.spawnGravityEffectParticles(xCoord, yCoord, zCoord, entity, (float) Math.min(2, dist));
        }
    }

    /**
     * Applies gravity to an entity with the passed configurations, this method
     * calls the other with the TE's xyz coords
     * 
     * @param gravStrength
     *            - Strength of the gravity, usually a number < 3
     * @param maxGravXZ
     *            - Max gravity that can be applied in the X and Z directions
     * @param maxGravY
     *            - Max gravity that can be applied in the Y direction
     * @param minGrav
     *            - Minimum gravity that can be applied (prevents "wobbling" if
     *            such a thing ever exists)
     * @param range
     *            - The range of the gravitational effects
     * @param entity
     *            - Entity to effect
     * @param te
     *            - {@link TileEntity} to use as the center of gravity
     */
    public static void applyGravity(float gravStrength, float maxGravXZ, float maxGravY, float minGrav, float range, Entity entity, TileEntity te,
            boolean showParticles)
    {
        applyGravity(gravStrength, maxGravXZ, maxGravY, minGrav, range, entity, te.xCoord, te.yCoord, te.zCoord, showParticles);
    }

    /**
     * Applies gravity to the passed entity, with a center at the passed TE,
     * calls the other method with default configuration values
     * 
     * @param entity
     *            - Entity to affect
     * @param te
     *            - {@link TileEntity} to use as the center of gravity
     */
    public static void applyGravity(Entity entity, TileEntity te, boolean showParticles)
    {
        applyGravity(entity, te.xCoord, te.yCoord, te.zCoord, showParticles);
    }

    /**
     * Applies gravity to the passed entity, with a center at the passed
     * coordinates, calls the other method with default configuration values
     * 
     * @param entity
     *            - Entity to affect
     * @param x
     *            - x coord
     * @param y
     *            - y coord
     * @param z
     *            - z coord
     */
    public static void applyGravity(Entity entity, int x, int y, int z, boolean showParticles)
    {
        applyGravity(ConfigHandler.strength, ConfigHandler.maxGravityXZ, ConfigHandler.maxGravityY, ConfigHandler.minGravity, ConfigHandler.range,
                entity, x, y, z, showParticles);
    }

    /**
     * Gets the star type of a star item, can handle items that are not
     * instances of {@link ItemStar}
     * 
     * @param stack
     *            - Stack to get the type from
     * @return {@link StarType} of the item
     */
    public static IStar getType(ItemStack stack)
    {
        if (stack != null && stack.getItem() instanceof IStarItem && stack.stackTagCompound != null)
            return stars.getTypeByName(stack.stackTagCompound.getString("type"));
        else
            return null;
    }

    public static int getStarPowerRemaining(ItemStack star)
    {
        if (star != null && star.getItem() instanceof IStarItem && star.stackTagCompound != null)
            return star.stackTagCompound.getInteger("energy");
        else
            return 0;
    }

    public static int getStarFuseRemaining(ItemStack star)
    {
        if (star != null && star.getItem() instanceof IStarItem && star.stackTagCompound != null)
        {
            if (!star.getTagCompound().hasKey("fuse"))
            {
                star.getTagCompound().setInteger("fuse", getType(star).getFuse());
            }

            return star.getTagCompound().getInteger("fuse");
        }
        else
        {
            return 0;
        }
    }

    public static void setStarFuseRemaining(ItemStack star, int fuse)
    {
        if (star != null && star.getItem() instanceof IStarItem && star.stackTagCompound != null)
            star.stackTagCompound.setInteger("fuse", fuse);
    }

    /**
     * Sets the type of a star itemstack, can handle items that are not
     * instances of {@link ItemStar}
     * 
     * @param stack
     *            - Stack to set the type on
     * @param type
     *            - Type to use
     * @return The itemstack affected
     */
    public static ItemStack setType(ItemStack stack, IStar type)
    {
        if (stack != null && stack.getItem() instanceof IStarItem)
        {
            if (stack.stackTagCompound == null)
                stack.stackTagCompound = new NBTTagCompound();

            stack.stackTagCompound.setString("type", type.getName());
            stack.stackTagCompound.setInteger("energy", type.getMaxEnergyStored(stack));
            stack.stackTagCompound.setInteger("fuse", type.getFuse());
        }
        else if (stack != null)
        {
            SuperMassiveTech.logger.error(String.format("A mod tried to set the type of an item that was not a star, item was %s",
                    stack.getUnlocalizedName()));
        }
        else
        {
            SuperMassiveTech.logger.error("A mod tried to set the type of a null itemstack");
        }

        return stack;
    }

    /**
     * Finds the proper tool for this material, returns "none" if there isn't
     * one
     */
    public static String getToolClassFromMaterial(Material mat)
    {
        if (ArrayUtils.contains(pickMats, mat))
            return "pickaxe";
        if (ArrayUtils.contains(shovelMats, mat))
            return "shovel";
        if (mat == Material.wood)
            return "axe";
        else
            return "none";
    }

    /**
     * Gets the tool level for this material
     */
    public static int getToolLevelFromMaterial(Material mat)
    {
        if (ArrayUtils.contains(pickMats, mat))
        {
            if (mat == Material.rock)
                return 0;
            if (mat == Material.iron)
                return 1;
            if (mat == Material.anvil)
                return 1;
        }
        return 0;
    }

    private static boolean green = true;

    /**
     * In-place adds to a list, forming an advanced tooltip from the passed item
     */
    public static void formAdvancedTooltip(List<String> lines, ItemStack stack, IAdvancedTooltip tooltip)
    {
        formAdvancedTooltip(lines, stack, tooltip, Keyboard.getKeyIndex(ConfigHandler.tooltipKey1), Keyboard.getKeyIndex(ConfigHandler.tooltipKey2));
    }

    public static void formAdvancedTooltip(List<String> lines, ItemStack stack, IAdvancedTooltip tooltip, int key)
    {
        formAdvancedTooltip(lines, stack, tooltip, key, key);
    }

    public static void formAdvancedTooltip(List<String> lines, ItemStack stack, IAdvancedTooltip tooltip, int key, int alternateKey)
    {
        if (tooltip.getHiddenLines(stack) != null)
        {
            if (Keyboard.isKeyDown(key) || Keyboard.isKeyDown(alternateKey))
            {
                for (String s : lang.splitList(tooltip.getHiddenLines(stack)))
                {
                    String[] ss = s.split("~");
                    for (String line : ss)
                    {
                        lines.add(green ? EnumChatFormatting.GREEN.toString() + line : EnumChatFormatting.WHITE + line);
                    }
                    green = green ? false : true;
                }
                green = true;
            }
            else
            {
                lines.add(String.format("%s -%s- %s", EnumChatFormatting.RED + lang.localize("tooltip.hold") + EnumChatFormatting.YELLOW,
                        getNameForKey(key), EnumChatFormatting.RED + lang.localize("tooltip.moreInfo")));
            }
        }

        if (tooltip.getStaticLines(stack) != null)
        {
            if (tooltip.getHiddenLines(stack) != null)
                lines.add("");

            for (String s : lang.splitList(tooltip.getStaticLines(stack), "~"))
                lines.add(EnumChatFormatting.WHITE + s);
        }
    }

    private static String getNameForKey(int key)
    {
        switch (key)
        {
        case Keyboard.KEY_LSHIFT:
        case Keyboard.KEY_RSHIFT:
            return lang.localize("tooltip.shift");
        case Keyboard.KEY_LCONTROL:
        case Keyboard.KEY_RCONTROL:
            return lang.localize("tooltip.control");
        case Keyboard.KEY_LMENU:
        case Keyboard.KEY_RMENU:
            return lang.localize("tooltip.alt");
        }

        return Keyboard.getKeyName(key);
    }

    /**
     * Applies the potion effects associated with gravity to the player at
     * effect level <code> level </code>
     */
    public static void applyGravPotionEffects(EntityPlayer player, int level)
    {
        if (!player.capabilities.isCreativeMode)
        {
            player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 1, level, true));
            player.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 1, level, true));
        }
    }

    public static String makeTooltipString(List<String> strs)
    {
        String toReturn = "";
        for (String s : strs)
        {
            toReturn += s;
            if (strs.indexOf(s) != strs.size() - 1)
                toReturn += "~";
        }
        return toReturn;
    }

    public static void writeUUIDsToNBT(UUID[] uuids, NBTTagCompound tag, String toName)
    {
        int[] uuidnums = new int[uuids.length * 4];

        for (int i = 0; i < uuids.length; i++)
        {
            long msd = uuids[i].getMostSignificantBits();
            long lsd = uuids[i].getLeastSignificantBits();

            uuidnums[i * 4] = (int) (msd >> 32);
            uuidnums[i * 4 + 1] = (int) msd;
            uuidnums[i * 4 + 2] = (int) (lsd >> 32);
            uuidnums[i * 4 + 3] = (int) lsd;
        }

        tag.setIntArray(toName, uuidnums);
    }

    public static UUID[] readUUIDsFromNBT(String name, NBTTagCompound tag)
    {
        int[] uuidnums = tag.getIntArray(name);
        UUID[] uuids = new UUID[uuidnums.length / 4];

        for (int i = 0; i < uuidnums.length; i += 4)
        {
            long msd = ((long) uuidnums[i]) << 32;
            msd += uuidnums[i + 1];
            long lsd = ((long) uuidnums[i + 2]) << 32;
            lsd += uuidnums[i + 3];

            uuids[i / 4] = new UUID(msd, lsd);
        }

        return uuids;
    }

    public static boolean doStatesMatch(EntityPlayer e, PowerUps power, int slot, String state)
    {
        ItemStack armor = e.inventory.armorInventory[slot];
        return armorIsGravityArmor(armor) && armor.stackTagCompound.getString(power.toString()).equals(state);
    }

    public static boolean armorIsGravityArmor(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof ItemGravityArmor;
    }

    public static double getGravResist(EntityPlayer player, double mult)
    {
        double percent = 0.0;
        for (int i = 0; i < 4; i++)
        {
            if (doStatesMatch(player, PowerUps.GRAV_RESIST, i, GravityArmorHandler.ON))
            {
                percent += 0.25;
            }
            else
            {
                int level = EnchantmentHelper.getEnchantmentLevel(ConfigHandler.gravEnchantID, player.inventory.armorInventory[i]);
                if (level > 0)
                {
                    percent += SuperMassiveTech.enchantRegistry.gravity.getReduction(1 / 5, level);
                }
            }
        }
        return percent * mult;
    }

    public static double getGravResist(EntityPlayer player)
    {
        return getGravResist(player, 1.0);
    }

    public static boolean shouldSpawnBlackHole(World worldObj) // TODO:
                                                               // implement
                                                               // something
                                                               // better for
                                                               // this
    {
        return worldObj.rand.nextBoolean();
    }

    /**
     * Make sure block isn't spawn protected or unbreakable
     */
    public static boolean canBreakBlock(EntityPlayer player, World world, BlockCoord blockCoord)
    {
        return world.canMineBlock(player, blockCoord.x, blockCoord.y, blockCoord.z)
                && world.getBlock(blockCoord.x, blockCoord.y, blockCoord.z).canEntityDestroy(world, blockCoord.x, blockCoord.y, blockCoord.z, player);
    }
}
