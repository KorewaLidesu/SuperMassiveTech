package korewali.supermassivetech.common.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import korewali.supermassivetech.common.network.PacketHandler;
import korewali.supermassivetech.common.network.message.tile.MessageHopperParticle;
import korewali.supermassivetech.common.tile.abstracts.TileSMTInventory;
import korewali.supermassivetech.common.util.Utils;

import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.util.ItemUtil;
import com.enderio.core.common.util.blockiterators.CubicBlockIterator;

public class TileBlackHoleHopper extends TileSMTInventory implements ISidedInventory
{
    private ForgeDirection connectionDir;
    ArrayList<InventoryConnection> inventories = new ArrayList<InventoryConnection>();

    private InventoryConnection connection;

    private final int searchRange = 5, hiddenSlot = 0, cfgSlot = 1;

    private class InventoryConnection
    {
        public BlockCoord pos;
        public IInventory inv;

        public InventoryConnection(IInventory inv, BlockCoord pos)
        {
            this.inv = inv;
            this.pos = pos;
        }

        public boolean isStillValid()
        {
            return pos.getTileEntity(worldObj) == this.inv;
        }
    }

    public TileBlackHoleHopper()
    {
        super(1, 0.8f);
        inventory = new ItemStack[2];
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (worldObj.isRemote)
            return;

        if (connectionDir == null)
        {
            connectionDir = ForgeDirection.getOrientation(getBlockMetadata());
        }

        checkConnection();

        processNearbyItems();

        if (onTime())
        {
            searchForInventories();
        }

        for (int i = 0; i < inventories.size(); i++)
            processInventory(inventories.get(i));

        if (connection == null)
        {
            return;
        }

        for (int i = 0; i < inventories.size(); i++)
            processConnection();
    }

    private void checkConnection()
    {
        if (connection == null)
        {
            TileEntity te = worldObj.getTileEntity(connectionDir.offsetX + xCoord, connectionDir.offsetY + yCoord, connectionDir.offsetZ + zCoord);
            if (te != null && te instanceof IInventory)
                connection = new InventoryConnection((IInventory) te, new BlockCoord(te));
        }
        else
        {
            TileEntity te = connection.pos.getTileEntity(worldObj);
            if (te instanceof IInventory)
            {
                connection = new InventoryConnection((IInventory) te, new BlockCoord(te));
            }
            else
            {
                connection = null;
            }
        }
    }

    private void processConnection()
    {
        if (inventory[hiddenSlot] == null || connection == null)
            return;

        if (connection.inv instanceof ISidedInventory)
        {
            ISidedInventory inv = (ISidedInventory) connection.inv;

            for (Integer i : inv.getAccessibleSlotsFromSide(connectionDir.ordinal()))
            {
                if (inv.canInsertItem(i, inventory[hiddenSlot], connectionDir.ordinal()))
                {
                    insertOneItem(inv, i);
                    return;
                }
            }
        }
        else
        {
            for (int idx = 0; idx < connection.inv.getSizeInventory(); idx++)
            {
                ItemStack stack = connection.inv.getStackInSlot(idx);

                if (connection.inv.isItemValidForSlot(idx, inventory[hiddenSlot])
                        && (stack == null || (itemStackEquals(stack, inventory[hiddenSlot]) && stack.stackSize < stack.getMaxStackSize())))
                {
                    insertOneItem(connection.inv, idx);
                    return;
                }
            }
        }
    }

    private void insertOneItem(IInventory inv, int idx)
    {
        ItemStack inSlot = inv.getStackInSlot(idx);

        if (inSlot == null && inv.isItemValidForSlot(idx, inventory[hiddenSlot]))
        {
            ItemStack newStack = inventory[hiddenSlot].copy();
            newStack.stackSize = 1;
            connection.inv.setInventorySlotContents(idx, newStack);
            inventory[hiddenSlot].stackSize--;
        }
        else if (inSlot != null && inSlot.stackSize < inv.getInventoryStackLimit())
        {
            inSlot.stackSize++;
            inventory[hiddenSlot].stackSize--;
        }

        if (inventory[hiddenSlot].stackSize <= 0)
            inventory[hiddenSlot] = null;
    }

    /**
     * Pulls from an inventory, adds to the hiddenSlot
     * 
     * @param i
     *            - InventoryConnection object to pull from
     */
    private void processInventory(InventoryConnection i)
    {
        if (i.isStillValid() && (inventory[hiddenSlot] == null || inventory[hiddenSlot].stackSize < inventory[hiddenSlot].getMaxStackSize()))
        {
            loop: for (int idx = 0; idx < i.inv.getSizeInventory(); idx++)
            {
                ItemStack stack = i.inv.getStackInSlot(idx);

                if (stack != null && inventory[cfgSlot] != null && stack.getItem() == inventory[cfgSlot].getItem() && stack.stackSize > 0
                        && (inventory[hiddenSlot] == null || inventory[hiddenSlot].stackSize < inventory[hiddenSlot].getMaxStackSize()))
                {
                    ItemStack single = stack.copy();
                    single.stackSize = 1;

                    i.inv.decrStackSize(idx, 1);

                    if (inventory[hiddenSlot] == null)
                        inventory[hiddenSlot] = single;
                    else
                        inventory[hiddenSlot].stackSize++;

                    spawnParticle(i.pos.x, i.pos.y, i.pos.z);
                    break loop;
                }
            }
        }
        else
        {
            inventories.remove(i);
        }
    }

    private void spawnParticle(int fromX, int fromY, int fromZ)
    {
        PacketHandler.INSTANCE.sendToAll(new MessageHopperParticle(xCoord, yCoord, zCoord, fromX, fromY, fromZ));
    }

    @SuppressWarnings("unchecked")
    private void processNearbyItems()
    {
        List<EntityItem> touchingEntityItems = worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(xCoord + 0.5 - 1,
                yCoord + 0.5 - 1, zCoord + 0.5 - 1, xCoord + 0.5 + 1, yCoord + 0.5 + 1, zCoord + 0.5 + 1));

        for (EntityItem item : touchingEntityItems)
        {
            if ((inventory[hiddenSlot] == null || itemStackEquals(item.getEntityItem(), inventory[hiddenSlot])
                    && (inventory[hiddenSlot] == null || inventory[hiddenSlot].stackSize < inventory[hiddenSlot].getMaxStackSize())))
            {
                if (inventory[hiddenSlot] == null)
                {
                    ItemStack newStack = item.getEntityItem().copy();
                    newStack.stackSize = 1;
                    inventory[hiddenSlot] = newStack;
                    item.getEntityItem().stackSize--;
                }
                else
                {
                    item.getEntityItem().stackSize--;
                    inventory[hiddenSlot].stackSize++;
                }
                processConnection();
            }
        }
    }

    @Override
    public boolean isGravityWell()
    {
        return true;
    }

    @Override
    public boolean showParticles()
    {
        return true;
    }

    private boolean onTime()
    {
        return worldObj.getTotalWorldTime() % 20 == 0;
    }

    private void searchForInventories()
    {
        for (BlockCoord bc : new CubicBlockIterator(new BlockCoord(this), searchRange))
        {
            if (isValidTileEntity(bc))
            {
                TileEntity te = bc.getTileEntity(worldObj);
                inventories.add(new InventoryConnection((IInventory) te, new BlockCoord(te)));
            }
        }
    }

    private boolean isValidTileEntity(BlockCoord bc)
    {
        Block block = bc.getBlock(worldObj);
        if (block != null && block.hasTileEntity(bc.getMetadata(worldObj)))
        {
            TileEntity te = bc.getTileEntity(worldObj);
            return te != null && !(te instanceof TileBlackHoleHopper) && teNotEquals(te, connection) && te instanceof IInventory
                    && notAlreadyFound(te);
        }

        return false;
    }

    private boolean notAlreadyFound(TileEntity te)
    {
        for (InventoryConnection i : inventories)
        {
            if (i.pos.equals(new BlockCoord(te)))
            {
                return false;
            }
        }

        return true;
    }

    private boolean teNotEquals(TileEntity inv, InventoryConnection connection)
    {
        if (connection == null)
            return true;
        else
            return !connection.pos.equals(new BlockCoord(inv));
    }

    public void setConfig(ItemStack stack, EntityPlayer player)
    {
        player.addChatMessage(new ChatComponentText(Utils.lang.localize("tooltip.setConfig") + ": "
                + StatCollector.translateToLocal(stack.getUnlocalizedName() + ".name")));
        inventory[cfgSlot] = stack.copy();
    }

    public String getConfig()
    {
        return inventory[cfgSlot] == null ? "nothing" : StatCollector.translateToLocal(inventory[cfgSlot].getUnlocalizedName() + ".name");
    }

    public void clearConfig(EntityPlayer player)
    {
        if (inventory[cfgSlot] == null)
        {
            player.addChatMessage(new ChatComponentText(Utils.lang.localize("tooltip.noConfigSet")));
            return;
        }

        player.addChatMessage(new ChatComponentText(Utils.lang.localize("tooltip.clearedConfig") + ": "
                + StatCollector.translateToLocal(inventory[cfgSlot].getUnlocalizedName() + ".name")));
        inventory[cfgSlot] = null;
    }

    public static boolean itemStackEquals(ItemStack s1, ItemStack s2)
    {
        if (s1 == null && s2 == null)
            return true;
        if (s1 == null || s2 == null)
            return false;
        if (!s1.isItemEqual(s2))
            return false;
        if (s1.getTagCompound() == null && s2.getTagCompound() == null)
            return true;
        if (s1.getTagCompound() == null || s2.getTagCompound() == null)
            return false;

        return s1.getTagCompound().equals(s2.getTagCompound());
    }

    @Override
    public String getInventoryName()
    {
        return "korewali.inventory.blackHoleHopper";
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int var1)
    {
        ForgeDirection dir = getRawDir();
        return new int[] { dir.ordinal(), dir.getOpposite().ordinal() };
    }

    @Override
    public boolean canInsertItem(int var1, ItemStack var2, int var3)
    {
        ForgeDirection dir = getRawDir();
        return var1 == hiddenSlot && var3 == dir.getOpposite().ordinal() && ItemUtil.stacksEqual(inventory[cfgSlot], var2);
    }

    @Override
    public boolean canExtractItem(int var1, ItemStack var2, int var3)
    {
        ForgeDirection dir = getRawDir();
        return var1 == hiddenSlot && var3 == dir.ordinal();
    }

    private ForgeDirection getRawDir()
    {
        return ForgeDirection.getOrientation(getBlockMetadata());
    }
}
