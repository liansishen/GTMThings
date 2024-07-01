package com.hepdd.gtmthings.common.block.machine.electric;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.IgnoreEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.misc.ItemRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.OreDictExprFilter;
import com.hepdd.gtmthings.api.misc.UnlimitedItemStackTransfer;
import com.hepdd.gtmthings.common.block.machine.trait.miner.DigitalMinerLogic;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.misc.ItemTransferList;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DigitalMiner extends WorkableTieredMachine
        implements IControllable, IFancyUIMachine, IDataInfoProvider {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(DigitalMiner.class,
            WorkableTieredMachine.MANAGED_FIELD_HOLDER);

    private final int INVENTORY_SIZE = 27;
    private static final Pattern DOUBLE_WILDCARD = Pattern.compile("\\*{2,}");
    private static final Pattern DOUBLE_AND = Pattern.compile("&{2,}");
    private static final Pattern DOUBLE_OR = Pattern.compile("\\|{2,}");
    private static final Pattern DOUBLE_NOT = Pattern.compile("!{2,}");
    private static final Pattern DOUBLE_XOR = Pattern.compile("\\^{2,}");
    private static final Pattern DOUBLE_SPACE = Pattern.compile(" {2,}");

    private final long energyPerTick;
    @Nullable
    protected TickableSubscription autoOutputSubs;
    @Nullable
    protected ISubscription exportItemSubs, energySubs;
    @Persisted
    protected NotifiableItemStackHandler inventory;

    @Nullable
    private ItemTransferList cachedItemTransfer = null;
    @Getter
    private final int fortune;
    @Getter
    private final int speed;
    @Getter
    private final int maximumRadius;
    @Getter
    public ItemStack pickaxeTool;
    private final LinkedList<BlockPos> blocksToMine = new LinkedList<>();
    @Getter
    @Persisted
    protected int x = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int y = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int z = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int startX = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int startZ = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int startY = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int mineX = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int mineZ = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int mineY = Integer.MAX_VALUE;
    @Getter
    private int minBuildHeight = Integer.MAX_VALUE;
    @Getter
    @Setter
    @Persisted
    private int currentRadius;
    @Getter
    @Persisted
    private boolean isDone;
    @Getter
    private boolean isInventoryFull;
    @Getter
    private final Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilitiesProxy;
    private final ItemRecipeHandler inputItemHandler, outputItemHandler;
    private final IgnoreEnergyRecipeHandler inputEnergyHandler;
    @Getter
    private int oreAmount;
    private TickableSubscription mineSubs;


    public DigitalMiner(IMachineBlockEntity holder, int tier, int speed, int maximumRadius, int fortune,
                        Object... args) {
        super(holder, tier, GTMachines.defaultTankSizeFunction, args, (tier + 1) * (tier + 1), fortune, speed,
                maximumRadius);
        this.energyPerTick = GTValues.V[tier - 1];
        this.setWorkingEnabled(false);
        this.inventory = createInventory();
        this.fortune = fortune;
        this.speed = speed;
        this.currentRadius = maximumRadius;
        this.maximumRadius = maximumRadius;
        this.isDone = false;
        this.pickaxeTool = GTItems.TOOL_ITEMS.get(GTMaterials.Neutronium, GTToolType.PICKAXE).get().get();
        this.pickaxeTool.enchant(Enchantments.BLOCK_FORTUNE, fortune);
        this.capabilitiesProxy = Tables.newCustomTable(new EnumMap<>(IO.class), IdentityHashMap::new);
        this.inputItemHandler = new ItemRecipeHandler(IO.IN, INVENTORY_SIZE);
        this.outputItemHandler = new ItemRecipeHandler(IO.OUT,INVENTORY_SIZE);
        this.inputEnergyHandler = new IgnoreEnergyRecipeHandler();
        this.capabilitiesProxy.put(IO.IN, inputItemHandler.getCapability(), List.of(inputItemHandler));
        this.capabilitiesProxy.put(IO.IN, inputEnergyHandler.getCapability(), List.of(inputEnergyHandler));
        this.capabilitiesProxy.put(IO.OUT, outputItemHandler.getCapability(), List.of(outputItemHandler));
        if(this.oreDictFilterExpression==null){
            this.oreDictFilterExpression="";
        }
        if (!this.oreDictFilterExpression.isEmpty()) {
            SetOreFilter(this.oreDictFilterExpression);
        }
    }

    protected NotifiableItemStackHandler createInventory() {
        return new NotifiableItemStackHandler(this, INVENTORY_SIZE, IO.OUT, IO.OUT, UnlimitedItemStackTransfer::new) {

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
                var extracted = super.extractItem(slot, amount, simulate, notifyChanges).copy();
                if (!extracted.isEmpty()) {
                    extracted.setCount(Integer.MAX_VALUE);
                }
                return extracted;
            }
        };
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    protected NotifiableItemStackHandler createImportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, 0, IO.NONE);
    }

    @Override
    protected NotifiableItemStackHandler createExportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, INVENTORY_SIZE, IO.OUT, IO.BOTH);
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, exportItems.storage);
    }


    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            if (getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
            }
            mineSubs = subscribeServerTick(mineSubs, this::doMineSubscription);
            exportItemSubs = exportItems.addChangedListener(this::updateAutoOutputSubscription);
            //updateTickSubscription();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (mineSubs != null) {
            mineSubs.unsubscribe();
            mineSubs = null;
        }

        if (exportItemSubs != null) {
            exportItemSubs.unsubscribe();
            exportItemSubs = null;
        }

        if (energySubs != null) {
            energySubs.unsubscribe();
            energySubs = null;
        }
    }
    public void updateTickSubscription() {
        if (getRecipeLogic().isSuspend() || !isRecipeLogicAvailable()) {
            if (mineSubs != null) {
                mineSubs.unsubscribe();
                mineSubs = null;
            }
        } else {
            mineSubs = subscribeServerTick(mineSubs, this::doMineSubscription);
        }
    }

    //////////////////////////////////////
    // ********** LOGIC **********//
    //////////////////////////////////////
    protected void updateAutoOutputSubscription() {
        var outputFacingItems = getFrontFacing();
        if (!exportItems.isEmpty() && ItemTransferHelper.getItemTransfer(getLevel(),
                getPos().relative(outputFacingItems), outputFacingItems.getOpposite()) != null) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::autoOutput);
        } else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    protected void autoOutput() {
        if (getOffsetTimer() % 5 == 0) {
            exportItems.exportToNearby(getFrontFacing());
        }
        updateAutoOutputSubscription();
    }


    protected void doMineSubscription() {
        if (!isWorkingEnabled()) return;
        if (!getRecipeLogic().isSuspend() && getLevel() instanceof ServerLevel serverLevel && checkCanMine()) {
            if (!isInventoryFull()) {
                // always drain storages when working, even if blocksToMine ends up being empty
                drainInput(false);
                // since energy is being consumed the miner is now active
                getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
            } else {
                // the miner cannot drain, therefore it is inactive
                if (getRecipeLogic().isWorking()) {
                    getRecipeLogic().setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_out").append(": ")
                            .append(ItemRecipeCapability.CAP.getName()));
                }
            }

            if (blocksToMine.isEmpty()) {
                checkBlocksToMine();
                this.oreAmount = blocksToMine.size();
            }

            if (getOffsetTimer() % this.speed == 0 && !blocksToMine.isEmpty()) {
                NonNullList<ItemStack> blockDrops = NonNullList.create();
                BlockState blockState = serverLevel.getBlockState(blocksToMine.getFirst());

                // check to make sure the ore is still there,
                while (!blockState.is(CustomTags.ORE_BLOCKS)) {
                    blocksToMine.removeFirst();
                    this.oreAmount = blocksToMine.size();
                    if (blocksToMine.isEmpty()) break;
                    blockState = serverLevel.getBlockState(blocksToMine.getFirst());
                }
                // When we are here we have an ore to mine! I'm glad we aren't threaded
                if (!blocksToMine.isEmpty() & blockState.is(CustomTags.ORE_BLOCKS)) {
                    LootParams.Builder builder = new LootParams.Builder(serverLevel)
                            .withParameter(LootContextParams.BLOCK_STATE, blockState)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atLowerCornerOf(blocksToMine.getFirst()))
                            .withParameter(LootContextParams.TOOL, getPickaxeTool());

                    // get the block's drops.
                    getRegularBlockDrops(blockDrops, blockState, builder);

                    // try to insert them
                    mineAndInsertItems(blockDrops, serverLevel);
                }
            }

            if (blocksToMine.isEmpty()) {
                // there were no blocks to mine, so the current position is the previous position
                x = mineX;
                y = mineY;
                z = mineZ;

                // attempt to get more blocks to mine, if there are none, the miner is done mining
                blocksToMine.addAll(getBlocksToMine());
                if (blocksToMine.isEmpty()) {
                    this.isDone = true;
                    getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
                }
            }
        } else {
            // machine isn't working enabled
            getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
//            if (mineSubs != null) {
//                mineSubs.unsubscribe();
//                mineSubs = null;
//            }
        }
    }
    protected ItemTransferList getCachedItemTransfer() {
        if (cachedItemTransfer == null) {
//            cachedItemTransfer = new ItemTransferList(getCapabilitiesProxy()
//                    .get(IO.OUT, ItemRecipeCapability.CAP).stream().map(IItemTransfer.class::cast).toList());
            cachedItemTransfer = new ItemTransferList(exportItems);
        }

        return cachedItemTransfer;
    }

    private static BlockState findMiningReplacementBlock(Level level) {
        try {
            return BlockStateParser.parseForBlock(level.holderLookup(Registries.BLOCK),
                    ConfigHolder.INSTANCE.machines.replaceMinedBlocksWith, false).blockState();
        } catch (CommandSyntaxException ignored) {
            GTCEu.LOGGER.error("failed to parse replaceMinedBlocksWith, invalid BlockState: {}",
                    ConfigHolder.INSTANCE.machines.replaceMinedBlocksWith);
            return Blocks.COBBLESTONE.defaultBlockState();
        }
    }

    private void mineAndInsertItems(NonNullList<ItemStack> blockDrops, ServerLevel world) {
        // If the block's drops can fit in the inventory, move the previously mined position to the block
        // replace the ore block with cobblestone instead of breaking it to prevent mob spawning
        // remove the ore block's position from the mining queue
        var transfer = getCachedItemTransfer();
        if (transfer != null) {
            if (GTTransferUtils.addItemsToItemHandler(transfer, true, blockDrops)) {
                GTTransferUtils.addItemsToItemHandler(transfer, false, blockDrops);
                world.setBlock(blocksToMine.getFirst(), findMiningReplacementBlock(world), 3);
                mineX = blocksToMine.getFirst().getX();
                mineZ = blocksToMine.getFirst().getZ();
                mineY = blocksToMine.getFirst().getY();
                blocksToMine.removeFirst();
                this.oreAmount = blocksToMine.size();

                // if the inventory was previously considered full, mark it as not since an item was able to fit
                isInventoryFull = false;
            } else {
                // the ore block was not able to fit, so the inventory is considered full
                isInventoryFull = true;
            }
        }
    }

    protected void getRegularBlockDrops(NonNullList<ItemStack> blockDrops, BlockState blockState,
                                        LootParams.Builder builder) {
        blockDrops.addAll(blockState.getDrops(builder));
    }

    public void checkBlocksToMine() {
        if (blocksToMine.isEmpty())
            blocksToMine.addAll(getBlocksToMine());
    }

    private LinkedList<BlockPos> getBlocksToMine() {
        LinkedList<BlockPos> blocks = new LinkedList<>();

        // determine how many blocks to retrieve this time
        //double quotient = getQuotient(getMeanTickTime(getMachine().getLevel()));
        //int calcAmount = quotient < 1 ? 1 : (int) (Math.min(quotient, Short.MAX_VALUE));
        int calcAmount = Short.MAX_VALUE;
        int calculated = 0;

        if (this.minBuildHeight == Integer.MAX_VALUE)
            this.minBuildHeight = getLevel().getMinBuildHeight();

        // keep getting blocks until the target amount is reached
        while (calculated < calcAmount) {
            // moving down the y-axis
            if (y > minBuildHeight) {
                // moving across the z-axis
                if (z <= startZ + currentRadius * 2) {
                    // check every block along the x-axis
                    if (x <= startX + currentRadius * 2) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        BlockState state = getLevel().getBlockState(blockPos);
                        if (state.getBlock().defaultDestroyTime() >= 0 &&
                                getLevel().getBlockEntity(blockPos) == null) {
                            if (this.oreDictFilterExpression.isEmpty() && state.is(CustomTags.ORE_BLOCKS)) {
                                blocks.addLast(blockPos);
                            } else if (!this.oreDictFilterExpression.isEmpty() && test(state.getBlock().asItem().getDefaultInstance())) {
                                blocks.addLast(blockPos);
                            }
                        }
                        // move to the next x position
                        ++x;
                    } else {
                        // reset x and move to the next z layer
                        x = startX;
                        ++z;
                    }
                } else {
                    // reset z and move to the next y layer
                    z = startZ;
                    --y;
                }
            } else
                return blocks;

            // only count iterations where blocks were found
            if (!blocks.isEmpty())
                calculated++;
        }
        return blocks;
    }
    protected boolean checkCanMine() {
        // if the miner is finished, the target coordinates are invalid, or it cannot drain storages, stop
        // if the miner is not finished and has invalid coordinates, get new and valid starting coordinates
        if (!isDone && checkCoordinatesInvalid()) {
            initPos(getMiningPos(), currentRadius);
        }
        return !isDone && drainInput(true);
    }

    private boolean checkCoordinatesInvalid() {
        return x == Integer.MAX_VALUE && y == Integer.MAX_VALUE && z == Integer.MAX_VALUE;
    }

    public void initPos(@NotNull BlockPos pos, int currentRadius) {
        x = pos.getX() - currentRadius;
        z = pos.getZ() - currentRadius;
        y = pos.getY() - 1;
        startX = pos.getX() - currentRadius;
        startZ = pos.getZ() - currentRadius;
        startY = pos.getY();
        mineX = pos.getX() - currentRadius;
        mineZ = pos.getZ() - currentRadius;
        mineY = pos.getY() - 1;
        onRemove();
    }

    public void onRemove() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            var pos = getMiningPos().relative(Direction.DOWN);
            while (serverLevel.getBlockState(pos).is(GTBlocks.MINER_PIPE.get())) {
                serverLevel.removeBlock(pos, false);
                pos = pos.relative(Direction.DOWN);
            }
        }
    }

    public BlockPos getMiningPos() {
        return getPos();
    }
    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////
    private static final int BORDER_WIDTH = 3;

    @Override
    public Widget createUIWidget() {
        int rowSize = 3;
        int colSize = 9;
        int width = rowSize * 18 + 120 + 50;
        //int height = Math.max(rowSize * 18 + 20, 80);
        int height = rowSize * 18 + 80 + 8;
        int index = 0;

        WidgetGroup group = new WidgetGroup(0, 0, width, height);
        WidgetGroup slots = new WidgetGroup(8, 80 + 4 / 2, colSize * 18, rowSize * 18);
        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < colSize; x++) {
                var slot = new SlotWidget(exportItems,index++,x*18,y*18,true,false)
                        .setBackground(GuiTextures.SLOT);

                slots.addWidget(slot);
            }
        }

        var componentPanel = new ComponentPanelWidget(4,5,this::addDisplayText).setMaxWidthLimit(110);

        var container = new WidgetGroup(8, 0, 117, 80);
        container.addWidget(new DraggableScrollableWidgetGroup(4, 4, container.getSize().width - 8,
                container.getSize().height - 8)
                .setBackground(GuiTextures.DISPLAY)
                .addWidget(componentPanel));
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);

        var label = new LabelWidget(125,0,"过滤标签");
        var filter = new TextFieldWidget(125,10,80,16,this::getOreDictFilterExpression,this::SetOreFilter)
                .setMaxStringLength(64)
                .setValidator(input -> {
                    // remove all operators that are double
                    input = DOUBLE_WILDCARD.matcher(input).replaceAll("*");
                    input = DOUBLE_AND.matcher(input).replaceAll("&");
                    input = DOUBLE_OR.matcher(input).replaceAll("|");
                    input = DOUBLE_NOT.matcher(input).replaceAll("!");
                    input = DOUBLE_XOR.matcher(input).replaceAll("^");
                    input = DOUBLE_SPACE.matcher(input).replaceAll(" ");
                    // move ( and ) so it doesn't create invalid expressions f.e. xxx (& yyy) => xxx & (yyy)
                    // append or prepend ( and ) if the amount is not equal
                    StringBuilder builder = new StringBuilder();
                    int unclosed = 0;
                    char last = ' ';
                    for (int i = 0; i < input.length(); i++) {
                        char c = input.charAt(i);
                        if (c == ' ') {
                            if (last != '(')
                                builder.append(" ");
                            continue;
                        }
                        if (c == '(')
                            unclosed++;
                        else if (c == ')') {
                            unclosed--;
                            if (last == '&' || last == '|' || last == '^') {
                                int l = builder.lastIndexOf(" " + last);
                                int l2 = builder.lastIndexOf(String.valueOf(last));
                                builder.insert(l == l2 - 1 ? l : l2, ")");
                                continue;
                            }
                            if (i > 0 && builder.charAt(builder.length() - 1) == ' ') {
                                builder.deleteCharAt(builder.length() - 1);
                            }
                        } else if ((c == '&' || c == '|' || c == '^') && last == '(') {
                            builder.deleteCharAt(builder.lastIndexOf("("));
                            builder.append(c).append(" (");
                            continue;
                        }

                        builder.append(c);
                        last = c;
                    }
                    if (unclosed > 0) {
                        builder.append(")".repeat(unclosed));
                    } else if (unclosed < 0) {
                        unclosed = -unclosed;
                        for (int i = 0; i < unclosed; i++) {
                            builder.insert(0, "(");
                        }
                    }
                    input = builder.toString();
                    input = input.replaceAll(" {2,}", " ");
                    return input;
                });


        //var btn = new ButtonWidget(125,50,20,20,this::reset);
        var btn = new ButtonWidget(125, 40+BORDER_WIDTH, 18, 16 - BORDER_WIDTH,
                new TextTexture("重置").setDropShadow(false).setColor(ChatFormatting.BLACK.getColor()), this::reset)
                .setHoverTooltips(Component.literal("重置"));

        group.addWidget(btn);
        group.addWidget(label);
        group.addWidget(filter);
        group.addWidget(container);
        group.addWidget(slots);

        return group;
    }

    private void reset(ClickData clickData) {
        this.isDone = false;
        this.x = Integer.MAX_VALUE;
        this.y = Integer.MAX_VALUE;
        this.z = Integer.MAX_VALUE;
        blocksToMine.clear();
    }


    @Getter
    @Persisted
    protected String oreDictFilterExpression;
    //protected Consumer<ItemFilter> itemWriter = filter -> {};
    //protected Consumer<ItemFilter> onUpdated = filter -> itemWriter.accept(filter);

    @Persisted
    protected final List<OreDictExprFilter.MatchRule> matchRules = new ArrayList<>();

    private final Object2BooleanMap<Item> cache = new Object2BooleanOpenHashMap<>();

    private void SetOreFilter(String oreDict) {
        cache.clear();
        matchRules.clear();
        this.oreDictFilterExpression = oreDict;
        OreDictExprFilter.parseExpression(matchRules, oreDictFilterExpression);
        //onUpdated.accept(this);
    }

//    @Override
//    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
//        tag.putString("oreDict",this.oreDictFilterExpression);
//        super.saveCustomPersistedData(tag, forDrop);
//    }
//
//    @Override
//    public void loadCustomPersistedData(CompoundTag tag) {
//        this.oreDictFilterExpression = tag.getString("oreDict");
//        super.loadCustomPersistedData(tag);
//    }

    public boolean test(ItemStack itemStack) {
        if (oreDictFilterExpression.isEmpty()) return true;
        if (cache.containsKey(itemStack.getItem())) return cache.getOrDefault(itemStack.getItem(), false);
        if (OreDictExprFilter.matchesOreDict(matchRules, itemStack)) {
            cache.put(itemStack.getItem(), true);
            return true;
        }
        cache.put(itemStack.getItem(), false);
        return false;
    }

    private void addDisplayText(@NotNull List<Component> textList) {
//        int workingArea = IMiner.getWorkingArea(getCurrentRadius());
//        textList.add(Component.translatable("gtceu.machine.miner.startx", getX()).append(" ")
//                .append(Component.translatable("gtceu.machine.miner.minex", getMineX())));
//        textList.add(Component.translatable("gtceu.machine.miner.starty", getY()).append(" ")
//                .append(Component.translatable("gtceu.machine.miner.miney", getMineY())));
//        textList.add(Component.translatable("gtceu.machine.miner.startz", getZ()).append(" ")
//                .append(Component.translatable("gtceu.machine.miner.minez", getMineZ())));
//        textList.add(Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        textList.add(Component.literal("Ore Amount: ").append(String.valueOf(getOreAmount())));
        if (isDone())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.done")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
//        else if (isWorking())
//            textList.add(Component.translatable("gtceu.multiblock.large_miner.working")
//                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
        else if (!this.isWorkingEnabled())
            textList.add(Component.translatable("gtceu.multiblock.work_paused"));
        if (isInventoryFull())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.invfull")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        if (!drainInput(true))
            textList.add(Component.translatable("gtceu.multiblock.large_miner.needspower")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
    }

    public boolean drainInput(boolean simulate) {
        long resultEnergy = energyContainer.getEnergyStored() - energyPerTick;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(energyPerTick);
            return true;
        }
        return false;
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////
    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        if (isRemote()) return InteractionResult.SUCCESS;

        if (!this.isActive()) {
            int currentRadius = getCurrentRadius();
            if (currentRadius == 1)
                setCurrentRadius(getMaximumRadius());
            else if (playerIn.isShiftKeyDown())
                setCurrentRadius(Math.max(1, Math.round(currentRadius / 2.0f)));
            else
                setCurrentRadius(Math.max(1, currentRadius - 1));

            //resetArea(true);

            int workingArea = IMiner.getWorkingArea(getCurrentRadius());
            playerIn.sendSystemMessage(
                    Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        } else {
            playerIn.sendSystemMessage(Component.translatable("gtceu.multiblock.large_miner.errorradius"));
        }
        return InteractionResult.SUCCESS;
    }

    @NotNull
    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_MACHINE_INFO) {
            int workingArea = IMiner.getWorkingArea(getCurrentRadius());
            return Collections.singletonList(
                    Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        }
        return new ArrayList<>();
    }
}
