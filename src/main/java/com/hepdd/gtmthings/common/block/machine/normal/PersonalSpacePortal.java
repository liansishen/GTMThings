package com.hepdd.gtmthings.common.block.machine.normal;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.hepdd.gtmthings.api.capability.IBindable;
import com.hepdd.gtmthings.data.CustomMachines;
import com.hepdd.gtmthings.worldgen.DimensionHelper;
import com.hepdd.gtmthings.worldgen.ModTeleporter;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PersonalSpacePortal extends MetaMachine
        implements IInteractedMachine, IBindable, IMachineLife, IUIMachine, IDropSaveMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER =
            new ManagedFieldHolder(PersonalSpacePortal.class, MetaMachine.MANAGED_FIELD_HOLDER);

//    public final List<Block> VALID_BLOCK = Arrays.asList(Blocks.AIR,Blocks.STONE,Blocks.BEDROCK,Blocks.DIRT,Blocks.GRASS_BLOCK,Blocks.COBBLESTONE,Blocks.STONE_BRICKS) ;
    public final Map<String,Block> VALID_BLOCKS = new HashMap<String,Block>() {{
        put("minecraft:air",Blocks.AIR);
        put("minecraft:stone",Blocks.STONE);
        put("minecraft:bedrock",Blocks.BEDROCK);
        put("minecraft:dirt",Blocks.DIRT);
        put("minecraft:grass_block",Blocks.GRASS_BLOCK);
        put("minecraft:cobblestone",Blocks.COBBLESTONE);
        put("minecraft:stone_bricks",Blocks.STONE_BRICKS);
    }};

    public final int MAX_FLOOR = 446;

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Persisted
    private UUID owner;
    @Persisted
    @DescSynced
    @Setter @Getter
    private int worldId;
    @Setter @Getter
    @Persisted
    private BlockPos teleportPos;

    public PersonalSpacePortal(IMachineBlockEntity holder) {
        super(holder);
        this.layerItem = new ItemStackTransfer(10);
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        tag.putInt("worldId",this.worldId);
        if (this.teleportPos != null) {
            var posTag = new CompoundTag();
            posTag.putInt("x",this.teleportPos.getX());
            posTag.putInt("y",this.teleportPos.getY());
            posTag.putInt("z",this.teleportPos.getZ());
            tag.put("blockPos",posTag);
        }
        IDropSaveMachine.super.saveToItem(tag);
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        IDropSaveMachine.super.loadFromItem(tag);
        if (tag.contains("worldId")) {
            this.worldId = tag.getInt("worldId");
        }
        if (tag.contains("blockPos")) {
            var posTag = tag.getCompound("blockPos");
            this.teleportPos = new BlockPos(posTag.getInt("x"),posTag.getInt("y"),posTag.getInt("z"));
        }
    }

    @Override
    public UUID getUUID() {
        return owner;
    }

    @Override
    public void setUUID(UUID uuid) {
        this.owner = uuid;
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (world instanceof ServerLevel serverLevel && player.canChangeDimensions() && !player.isPassenger() && !player.isVehicle()) {
            if(world.dimension() == Level.OVERWORLD) {
                if (worldId == 0) return InteractionResult.PASS;
                var dim = DimensionHelper.getOrCreateWorld(world.getServer(),worldId,layerMap);
                player.changeDimension(dim,new ModTeleporter(this.getTeleportPos(), true));
            } else {
                player.changeDimension(serverLevel.getServer().overworld(),new ModTeleporter(this.getTeleportPos(), true));
            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (owner == null) {
            owner = player.getUUID();
            return true;
        }
        return player.getUUID().equals(owner);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player != null) {
            this.owner = player.getUUID();
        }
        IMachineLife.super.onMachinePlaced(player, stack);
    }


    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .widget(getCreateUI().setBackground(GuiTextures.BACKGROUND.copy()
                .setColor(Long.decode(ConfigHolder.INSTANCE.client.defaultUIColor).intValue() | 0xFF000000)));
    }



    @Persisted
    @Setter @Getter
    @DescSynced
    private String worldGernater;
    private Map<Block,Integer> layerMap = new HashMap<>();
    private ButtonWidget btnCreate;
    private TextFieldWidget layerInput;
    @Persisted
    private ItemStackTransfer layerItem;

    private Widget getCreateUI() {
        int height = 117;
        int width = 178;
        var group = new WidgetGroup(0, 0, width + 8, height + 4);

        group.addWidget(new LabelWidget(4,4,String.format("worldId: %s",worldId)));

        layerInput = new TextFieldWidget(4, 16, 80, 12, this::getWorldGernater,this::setWorldGernater)
                .setMaxStringLength(64)
                .setValidator(this::validateInput);
        group.addWidget(layerInput);

        //var layerSLot = new SlotWidget(layerItem,0,90,16,false,false);
        group.addWidget(new SlotWidget(layerItem,0,90,16,false,false));
        group.addWidget(new SlotWidget(layerItem,1,90,34,false,false));
        group.addWidget(new SlotWidget(layerItem,2,90,52,false,false));

        btnCreate = new ButtonWidget(4, 32, 30, 12,
                new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("create"))
                , this::createDim);
        btnCreate.setVisible(worldId == 0);
        group.addWidget(btnCreate);
        return group;
    }

    private String validateInput(String input) {
        String[] blocks = input.split(";");
        boolean errFlag = false;
        int totalFloor = 0;
        for (String block: blocks) {
            String blockName;
            int amount;
            var str = block.split("\\*");
            if (str.length>1) {
                blockName = str[0];
                amount = getAmount(str[1]);
            } else {
                blockName = block;
                amount = 1;
            }
            if (VALID_BLOCKS.containsKey(blockName) && totalFloor + amount < MAX_FLOOR) {
                layerMap.put(VALID_BLOCKS.get(blockName),amount);
                totalFloor = totalFloor + amount;
            } else {
                errFlag = true;
                break;
            }
        }
        if (errFlag) {
            layerMap.clear();
            btnCreate.setActive(false);
            layerInput.setTextColor(0xffff0000);
            layerInput.setHoverTooltips("无效的设置");
        } else {
            btnCreate.setActive(true);
            layerInput.setTextColor(-1);
            layerInput.getTooltipTexts().clear();
            if (!layerMap.isEmpty()) {
                int index = 0;
                for (Block layer: layerMap.keySet()) {
                    ItemStack is = layer.asItem().getDefaultInstance();
                    is.setCount(layerMap.get(layer));
                    layerItem.setStackInSlot(index++,is);
                }

            }
        }
        return input;
    }

    private int getAmount(String input) {
        String numberRegex = "^\\d+$";
        if (!input.matches(numberRegex)) {
            return 0;
        }
        var number = Integer.parseInt(input);
        if (number > 0 && number < MAX_FLOOR) {
            return number;
        } else {
            return 0;
        }
    }

    private void createDim(ClickData clickData) {

        if (getLevel() instanceof ServerLevel serverLevel) {
            var player = getLevel().getPlayerByUUID(owner);
            if (player != null && player.canChangeDimensions() && !player.isPassenger() && !player.isVehicle()) {
                worldId = DimensionHelper.getWorldCount(serverLevel.getServer()) + 1;
                int y = -64;
                for (Block key: layerMap.keySet()) {
                    y = y+ layerMap.get(key);
                }
                var pos = new BlockPos(0,y + 2,0);
                var dim = DimensionHelper.getOrCreateWorld(serverLevel.getServer(),worldId,layerMap);
                createSpawnSpace(dim,pos);
                player.changeDimension(dim,new ModTeleporter(this.teleportPos, true));
            }
        }
    }

    private void createSpawnSpace(ServerLevel dim,BlockPos pos) {
        dim.setBlock(pos, CustomMachines.PERSONAL_SPACE_PORTAL.defaultBlockState(), 3);
        var portalBlock = MetaMachine.getMachine(dim,pos);
        if (portalBlock instanceof PersonalSpacePortal personalSpacePortal) {
            personalSpacePortal.setTeleportPos(this.getPos().offset(2,0,0));
            personalSpacePortal.setWorldId(-1);
        }
        int radius = 2;
        for (int x = -radius;x <= radius;x++) {
            for (int z = -radius; z <= radius; z++) {
                var targetPos = portalBlock.getPos().offset(x,-1,z);
                dim.setBlock(targetPos, Blocks.SMOOTH_STONE.defaultBlockState(),3);
            }
        }
        this.teleportPos = portalBlock.getPos().offset(2,0,0);
    }
}
