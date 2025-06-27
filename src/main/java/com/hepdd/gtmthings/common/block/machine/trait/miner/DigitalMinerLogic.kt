package com.hepdd.gtmthings.common.block.machine.trait.miner

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.capability.recipe.*
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter
import com.gregtechceu.gtceu.api.item.tool.GTToolType
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.misc.IgnoreEnergyRecipeHandler
import com.gregtechceu.gtceu.api.misc.ItemRecipeHandler
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.RecipeHelper
import com.gregtechceu.gtceu.api.transfer.item.NotifiableAccountedInvWrapper
import com.gregtechceu.gtceu.common.data.GTMaterialItems
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.config.ConfigHolder
import com.gregtechceu.gtceu.utils.GTTransferUtils
import com.gregtechceu.gtceu.utils.GTUtil
import com.hepdd.gtmthings.api.capability.IDigitalMiner
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.commands.arguments.blocks.BlockStateParser
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.Tags
import net.minecraftforge.items.IItemHandlerModifiable
import java.util.*
import kotlin.math.pow

class DigitalMinerLogic(machine: IRecipeLogicMachine?, maximumRadius:Int, minHeight:Int, maxHeight:Int, silk:Int, itemFilter: ItemFilter?, speed:Int): RecipeLogic(machine), IRecipeCapabilityHolder {

    companion object {
        @JvmStatic
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            DigitalMinerLogic::class.java,
            RecipeLogic.MANAGED_FIELD_HOLDER
        )
        @JvmStatic
        val MAX_SPEED: Short = Short.MAX_VALUE
        @JvmStatic
        val POWER: Byte = 5
        @JvmStatic
        val TICK_TOLERANCE: Byte = 20
        @JvmStatic
        val DIVIDEND: Double = MAX_SPEED * TICK_TOLERANCE.toDouble().pow(POWER.toDouble())
    }

    private var miner: IDigitalMiner? = null
    private var cachedItemTransfer: NotifiableAccountedInvWrapper? = null

    private var silk = 0

    private var speed = 0

    var maximumRadius = 0

    private var pickaxeTool: ItemStack? = null
    private val blocksToMine = LinkedList<BlockPos>()

    @Persisted
    private var x: Int = Int.MAX_VALUE

    @Persisted
    private var y: Int = Int.MAX_VALUE

    @Persisted
    private var z: Int = Int.MAX_VALUE

    @Persisted
    private var startX: Int = Int.MAX_VALUE

    @Persisted
    private var startZ: Int = Int.MAX_VALUE

    @Persisted
    private var startY: Int = Int.MAX_VALUE

    @Persisted
    private var mineX: Int = Int.MAX_VALUE

    @Persisted
    private var mineZ: Int = Int.MAX_VALUE

    @Persisted
    var mineY: Int = Int.MAX_VALUE

    private var minBuildHeight = Int.MAX_VALUE

    private var minHeight = 0

    private var maxHeight = 0

    @Persisted
    var currentRadius = 0

    @Persisted
    var isDone = false

    var isInventoryFull = false

    private var capabilitiesProxy: Map<IO, List<RecipeHandlerList>>? = null

    private var capabilitiesFlat: Map<IO, Map<RecipeCapability<*>, List<IRecipeHandler<*>>>>? = null
    private var inputItemHandler: ItemRecipeHandler? = null
    private var outputItemHandler: ItemRecipeHandler? = null
    private var inputEnergyHandler: IgnoreEnergyRecipeHandler? = null

    var oreAmount = 0

    private var itemFilter: ItemFilter? = null

    init {
        this.miner = machine as IDigitalMiner
        this.silk = silk
        this.speed = speed
        this.currentRadius = maximumRadius
        this.maximumRadius = maximumRadius
        this.isDone = false
        this.minHeight = minHeight
        this.maxHeight = maxHeight
        this.pickaxeTool = GTMaterialItems.TOOL_ITEMS[GTMaterials.Neutronium, GTToolType.PICKAXE]!!.get().get()
        (this.pickaxeTool as ItemStack).enchant(Enchantments.BLOCK_FORTUNE,1)
        this.itemFilter = itemFilter
        this.capabilitiesProxy = EnumMap(IO::class.java)
        this.capabilitiesFlat = EnumMap(
            IO::class.java
        )
        this.inputItemHandler = ItemRecipeHandler(
            IO.IN,
            machine.getRecipeType().getMaxInputs(ItemRecipeCapability.CAP)
        )
        this.outputItemHandler = ItemRecipeHandler(
            IO.OUT,
            machine.getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP)
        )
        this.inputEnergyHandler = IgnoreEnergyRecipeHandler()
        addHandlerList(RecipeHandlerList.of(IO.IN, inputItemHandler, inputEnergyHandler))
        addHandlerList(RecipeHandlerList.of(IO.OUT, outputItemHandler))
    }

    override fun resetRecipeLogic() {
        super.resetRecipeLogic()
        resetArea(false)
        this.cachedItemTransfer = null
        this.isWorkingEnabled = false
    }

    fun resetRecipeLogic(maximumRadius: Int, minHeight: Int, maxHeight: Int, silk: Int, itemFilter: ItemFilter?) {
        this.silk = silk
        this.currentRadius = maximumRadius
        this.maximumRadius = maximumRadius
        this.minHeight = minHeight
        this.maxHeight = maxHeight
        this.itemFilter = itemFilter
        blocksToMine.clear()
        this.oreAmount = 0
        this.resetRecipeLogic()
    }

    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }

    override fun inValid() {
        super.inValid()
        this.cachedItemTransfer = null
    }

    private fun findMiningReplacementBlock(level: Level): BlockState {
        try {
            return BlockStateParser.parseForBlock(
                level.holderLookup(Registries.BLOCK),
                ConfigHolder.INSTANCE.machines.replaceMinedBlocksWith, false
            ).blockState()
        } catch (ignored: CommandSyntaxException) {
            GTCEu.LOGGER.error(
                "failed to parse replaceMinedBlocksWith, invalid BlockState: {}",
                ConfigHolder.INSTANCE.machines.replaceMinedBlocksWith
            )
            return Blocks.COBBLESTONE.defaultBlockState()
        }
    }

    /**
     * Performs the actual mining in world
     * Call this method every tick in update
     */
    override fun serverTick() {
        if (!isSuspend && getMachine().level is ServerLevel && checkCanMine()) {
            val serverLevel: ServerLevel = getMachine().level as ServerLevel
            // if the inventory is not full, drain energy etc. from the miner
            // the storages have already been checked earlier
            if (!isInventoryFull) {
                // always drain storages when working, even if blocksToMine ends up being empty
                miner!!.drainInput(false)
                // since energy is being consumed the miner is now active
                status = Status.WORKING
            } else {
                // the miner cannot drain, therefore it is inactive
                if (this.isWorking) {
                    setWaiting(
                        Component.translatable("gtceu.recipe_logic.insufficient_out").append(": ")
                            .append(ItemRecipeCapability.CAP.getName())
                    )
                }
            }

            // check if the miner needs new blocks to mine and get them if needed
            checkBlocksToMine()

            // if there are blocks to mine and the correct amount of time has passed, do the mining
            if (getMachine().offsetTimer % this.speed == 0L && !blocksToMine.isEmpty()) {
                val blockDrops = NonNullList.create<ItemStack>()
                var blockState: BlockState = serverLevel.getBlockState(blocksToMine.first())

                // check to make sure the ore is still there,
                while (!blockState.`is`(Tags.Blocks.ORES)) {
                    blocksToMine.removeFirst()
                    this.oreAmount = blocksToMine.size
                    if (blocksToMine.isEmpty()) break
                    blockState = serverLevel.getBlockState(blocksToMine.first())
                }
                // When we are here we have an ore to mine! I'm glad we aren't threaded
                if (!blocksToMine.isEmpty() and blockState.`is`(Tags.Blocks.ORES)) {
                    val builder = LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.BLOCK_STATE, blockState)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atLowerCornerOf(blocksToMine.first()))
                        .withParameter(LootContextParams.TOOL, pickaxeTool!!)

                    // get the block's drops.
                    if (isSilkTouchMode()) {
                        getSilkTouchDrops(blockDrops, blockState)
                    } else {
                        getRegularBlockDrops(blockDrops, blockState, builder)
                    }
                    // handle recipe type
                    if (hasPostProcessing()) {
                        doPostProcessing(blockDrops)
                    }
                    // try to insert them
                    mineAndInsertItems(blockDrops, serverLevel)
                    this.oreAmount = blocksToMine.size
                }
            }

            if (blocksToMine.isEmpty()) {
                // there were no blocks to mine, so the current position is the previous position
                x = mineX
                y = mineY
                z = mineZ

                // attempt to get more blocks to mine, if there are none, the miner is done mining
                blocksToMine.addAll(getBlocksToMine())
                if (blocksToMine.isEmpty()) {
                    this.isDone = true
                    this.status = Status.IDLE
                }
                this.oreAmount = blocksToMine.size
            }
        } else {
            // machine isn't working enabled
            this.status = Status.IDLE
            if (subscription != null) {
                subscription.unsubscribe()
                subscription = null
            }
        }
    }

    /**
     * @return true if the miner is able to mine, else false
     */
    private fun checkCanMine(): Boolean {
        // if the miner is finished, the target coordinates are invalid, or it cannot drain storages, stop
        // if the miner is not finished and has invalid coordinates, get new and valid starting coordinates
        if (!isDone && checkCoordinatesInvalid()) {
            initPos(getMiningPos(), currentRadius)
        }
        return !isDone && miner!!.drainInput(true)
    }

    /**
     * Called after each block is mined, used to perform additional actions after wards
     */
    private fun onMineOperation() {}

    /**
     * Should we apply additional processing according to the recipe type?
     */
    private fun hasPostProcessing(): Boolean {
        return false
    }

    private fun isSilkTouchMode(): Boolean {
        return silk == 1
    }

    /**
     * called to handle mining regular ores and blocks
     *
     * @param blockDrops the List of items to fill after the operation
     * @param blockState the [BlockState] of the block being mined
     */
    private fun getRegularBlockDrops(
        blockDrops: NonNullList<ItemStack>, blockState: BlockState,
        builder: LootParams.Builder
    ) {
        blockDrops.addAll(blockState.getDrops(builder))
    }

    private fun getVoltageTier(): Int {
        return 0
    }

    private fun doPostProcessing(
        blockDrops: NonNullList<ItemStack>
    ): Boolean {
        val oreDrop = blockDrops[0]

        // create dummy recipe handler
        inputItemHandler!!.storage.setStackInSlot(0, oreDrop)
        inputItemHandler!!.storage.onContentsChanged(0)
        for (i in 0..<outputItemHandler!!.storage.slots) {
            outputItemHandler!!.storage.setStackInSlot(i, ItemStack.EMPTY)
        }
        outputItemHandler!!.storage.onContentsChanged(0)

        val matches = machine.recipeType.searchRecipe(
            this
        ) { r: GTRecipe? -> RecipeHelper.matchContents(this, r).isSuccess }

        while (matches.hasNext()) {
            val match = matches.next() ?: continue

            val eut = RecipeHelper.getInputEUt(match)
            if (GTUtil.getTierByVoltage(eut) <= getVoltageTier()) {
                if (RecipeHelper.handleRecipeIO(this, match, IO.OUT, this.chanceCaches).isSuccess) {
                    blockDrops.clear()
                    val result = ArrayList<ItemStack>()
                    for (i in 0..<outputItemHandler!!.storage.slots) {
                        val stack = outputItemHandler!!.storage.getStackInSlot(i)
                        if (stack.isEmpty) continue
                        result.add(stack)
                    }
                    dropPostProcessing(blockDrops, result)
                    return true
                }
            }
        }
        return false
    }

    private fun dropPostProcessing(
        blockDrops: NonNullList<ItemStack>, outputs: List<ItemStack>
    ) {
        blockDrops.addAll(outputs)
    }

    /**
     * called to handle mining regular ores and blocks with silk touch
     *
     * @param blockDrops the List of items to fill after the operation
     * @param blockState the [BlockState] of the block being mined
     */
    private fun getSilkTouchDrops(
        blockDrops: NonNullList<ItemStack>, blockState: BlockState
    ) {
        blockDrops.add(ItemStack(blockState.block))
    }

    private fun getCachedItemTransfer(): NotifiableAccountedInvWrapper {
        if (cachedItemTransfer == null) {
            cachedItemTransfer = NotifiableAccountedInvWrapper(
                *machine.getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP)
                    .map { it as IItemHandlerModifiable }.toTypedArray())
        }
        return cachedItemTransfer as NotifiableAccountedInvWrapper
    }

    /**
     * called in order to insert the mined items into the inventory and actually remove the block in world
     * marks the inventory as full if the items cannot fit, and not full if it previously was full and items could fit
     *
     * @param blockDrops the List of items to insert
     * @param world      the [ServerLevel] the miner is in
     */
    private fun mineAndInsertItems(blockDrops: NonNullList<ItemStack>, world: ServerLevel) {
        // If the block's drops can fit in the inventory, move the previously mined position to the block
        // replace the ore block with cobblestone instead of breaking it to prevent mob spawning
        // remove the ore block's position from the mining queue
        val transfer = getCachedItemTransfer()
        if (GTTransferUtils.addItemsToItemHandler(transfer, true, blockDrops)) {
            GTTransferUtils.addItemsToItemHandler(transfer, false, blockDrops)
            world.setBlock(blocksToMine.first(), findMiningReplacementBlock(world), 3)
            mineX = blocksToMine.first().x
            mineZ = blocksToMine.first().z
            mineY = blocksToMine.first().y
            blocksToMine.removeFirst()
            onMineOperation()

            // if the inventory was previously considered full, mark it as not since an item was able to fit
            isInventoryFull = false
        } else {
            // the ore block was not able to fit, so the inventory is considered full
            isInventoryFull = true
        }
    }

    /**
     * This method designates the starting position for mining blocks
     *
     * @param pos           the [BlockPos] of the miner itself
     * @param currentRadius the currently set mining radius
     */
    private fun initPos(pos: BlockPos, currentRadius: Int) {
        x = pos.x - currentRadius
        z = pos.z - currentRadius
        y = maxHeight
        startX = pos.x - currentRadius
        startZ = pos.z - currentRadius
        startY = maxHeight
        mineX = pos.x - currentRadius
        mineZ = pos.z - currentRadius
        mineY = maxHeight
    }

    /**
     * Checks if the current coordinates are invalid
     *
     * @return `true` if the coordinates are invalid, else false
     */
    private fun checkCoordinatesInvalid(): Boolean {
        return x == Int.MAX_VALUE && y == Int.MAX_VALUE && z == Int.MAX_VALUE
    }

    /**
     * Checks whether there are any more blocks to mine, if there are currently none queued
     */
    private fun checkBlocksToMine() {
        if (blocksToMine.isEmpty()) blocksToMine.addAll(getBlocksToMine())
    }

    /**
     * Recalculates the mining area, refills the block list and restarts the miner, if it was done
     */
    fun resetArea(checkToMine: Boolean) {
        initPos(getMiningPos(), currentRadius)
        if (this.isDone) this.isWorkingEnabled = false
        this.isDone = false
        if (checkToMine) {
            blocksToMine.clear()
            checkBlocksToMine()
        }
    }

    /**
     * Gets the blocks to mine
     *
     * @return a [LinkedList] of [BlockPos] for each ore to mine
     */
    private fun getBlocksToMine(): LinkedList<BlockPos> {
        val blocks = LinkedList<BlockPos>()

        // determine how many blocks to retrieve this time
        getQuotient(getMeanTickTime(getMachine().level!!))
        // int calcAmount = quotient < 1 ? 1 : (int) (Math.min(quotient, Short.MAX_VALUE));
        val calcAmount = Short.MAX_VALUE.toInt()
        var calculated = 0

        if (this.minBuildHeight == Int.MAX_VALUE) this.minBuildHeight =
            getMachine().level!!.minBuildHeight

        // keep getting blocks until the target amount is reached
        while (calculated < calcAmount) {
            // moving down the y-axis
            if (y > this.minHeight) {
                // moving across the z-axis
                if (z <= startZ + currentRadius * 2) {
                    // check every block along the x-axis
                    if (x <= startX + currentRadius * 2) {
                        val blockPos = BlockPos(x, y, z)
                        val state = getMachine().level!!.getBlockState(blockPos)
                        if (state.block.defaultDestroyTime() >= 0 && getMachine().level!!.getBlockEntity(blockPos) == null &&
                            state.`is`(Tags.Blocks.ORES)
                        ) {
                            if (itemFilter == null) {
                                blocks.addLast(blockPos)
                            } else if (itemFilter!!.test(state.block.asItem().defaultInstance)) {
                                blocks.addLast(blockPos)
                            }
                        }
                        // move to the next x position
                        ++x
                    } else {
                        // reset x and move to the next z layer
                        x = startX
                        ++z
                    }
                } else {
                    // reset z and move to the next y layer
                    z = startZ
                    --y
                }
            } else return blocks

            // only count iterations where blocks were found
            if (!blocks.isEmpty()) calculated = blocks.size
        }
        return blocks
    }

    /**
     * @param values to find the mean of
     * @return the mean value
     */
    private fun mean(values: LongArray): Long {
        if (values.size.toLong() == 0L) return 0L

        var sum = 0L
        for (v in values) sum += v
        return sum / values.size
    }

    /**
     * @param world the [Level] to get the average tick time of
     * @return the mean tick time
     */
    private fun getMeanTickTime(world: Level?): Double {
        return mean(world!!.server!!.tickTimes) * 1.0E-6
    }

    /**
     * gets the quotient for determining the amount of blocks to mine
     *
     * @param base is a value used for calculation, intended to be the mean tick time of the world the miner is in
     * @return the quotient
     */
    private fun getQuotient(base: Double): Double {
        return DIVIDEND / base.pow(POWER.toDouble())
    }

    /**
     * @return the position to start mining from
     */
    private fun getMiningPos(): BlockPos {
        return getMachine().pos
    }

    override fun getCapabilitiesProxy(): MutableMap<IO, MutableList<RecipeHandlerList>> {
        TODO("Not yet implemented")
    }

    override fun getCapabilitiesFlat(): MutableMap<IO, MutableMap<RecipeCapability<*>, MutableList<IRecipeHandler<*>>>> {
        TODO("Not yet implemented")
    }
}