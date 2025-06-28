package com.hepdd.gtmthings.api.pattern

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.NonNullList
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.ItemStackHandler

import appeng.api.config.Actionable
import appeng.api.networking.IGrid
import appeng.api.stacks.AEItemKey
import appeng.items.tools.powered.WirelessTerminalItem
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController
import com.gregtechceu.gtceu.api.pattern.BlockPattern
import com.gregtechceu.gtceu.api.pattern.MultiblockState
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection
import com.gregtechceu.gtceu.common.block.CoilBlock
import com.hepdd.gtmthings.common.item.AdvancedTerminalBehavior.AutoBuildSetting
import com.lowdragmc.lowdraglib.utils.BlockInfo
import com.mojang.datafixers.util.Pair
import it.unimi.dsi.fastutil.ints.IntObjectPair
import org.apache.commons.lang3.ArrayUtils
import oshi.util.tuples.Triplet

import java.util.function.BiPredicate
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

open class AdvancedBlockPattern(predicatesIn: Array<Array<Array<TraceabilityPredicate>?>?>, structureDir: Array<RelativeDirection?>, aisleRepetitions: Array<IntArray?>, centerOffset: IntArray) : BlockPattern(predicatesIn, structureDir, aisleRepetitions, centerOffset) {

    companion object {
        @JvmStatic
        var FACINGS: Array<Direction?> = arrayOf<Direction?>(
            Direction.SOUTH,
            Direction.NORTH,
            Direction.WEST,
            Direction.EAST,
            Direction.UP,
            Direction.DOWN,
        )

        @JvmStatic
        var FACINGS_H: Array<Direction?> =
            arrayOf<Direction?>(Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST)

        @JvmStatic
        fun getAdvancedBlockPattern(blockPattern: BlockPattern?): AdvancedBlockPattern? {
            try {
                val clazz: Class<*> = BlockPattern::class.java
                // blockMatches
                val blockMatchesField = clazz.getDeclaredField("blockMatches")
                blockMatchesField.setAccessible(true)
                val blockMatches = blockMatchesField.get(blockPattern) as Array<Array<Array<TraceabilityPredicate>?>?>
                // structureDir
                val structureDirField = clazz.getDeclaredField("structureDir")
                structureDirField.setAccessible(true)
                val structureDir = structureDirField.get(blockPattern) as Array<RelativeDirection?>
                // aisleRepetitions
                val aisleRepetitionsField = clazz.getDeclaredField("aisleRepetitions")
                aisleRepetitionsField.setAccessible(true)
                val aisleRepetitions = aisleRepetitionsField.get(blockPattern) as Array<IntArray?>
                // centerOffset
                val centerOffsetField = clazz.getDeclaredField("centerOffset")
                centerOffsetField.setAccessible(true)
                val centerOffset = centerOffsetField.get(blockPattern) as IntArray

                return AdvancedBlockPattern(blockMatches, structureDir, aisleRepetitions, centerOffset)
            } catch (_: Exception) {
            }
            return null
        }
    }

    val advaisleRepetitions: Array<IntArray?>
    val advstructureDir: Array<RelativeDirection?>
    private val advblockMatches: Array<Array<Array<TraceabilityPredicate>?>?> = predicatesIn // [z][y][x]
    private var advfingerLength: Int = 0 // z size
    private var advthumbLength: Int = 0 // y size
    private var advpalmLength: Int = 0 // x size
    private var advcenterOffset: IntArray // x, y, z, minZ, maxZ

    init {
        this.advfingerLength = predicatesIn.size
        this.advstructureDir = structureDir
        this.advaisleRepetitions = aisleRepetitions

        if (this.advfingerLength > 0) {
            this.advthumbLength = predicatesIn[0]!!.size

            if (this.advthumbLength > 0) {
                this.advpalmLength = predicatesIn[0]!![0]!!.size
            } else {
                this.advpalmLength = 0
            }
        } else {
            this.advthumbLength = 0
            this.advpalmLength = 0
        }

        this.advcenterOffset = centerOffset
    }

    fun autoBuild(player: Player, worldState: MultiblockState, autoBuildSetting: AutoBuildSetting) {
        val world = player.level()
        var minZ = -advcenterOffset[4]
        clearWorldState(worldState)
        val controller = worldState.getController()
        val centerPos = controller.self().pos
        val facing = controller.self().getFrontFacing()
        val upwardsFacing = controller.self().upwardsFacing
        val isFlipped = controller.self().isFlipped()
        val cacheGlobal = worldState.globalCount
        val cacheLayer = worldState.layerCount
        val blocks = HashMap<BlockPos, Any?>()
        val placeBlockPos = HashSet<BlockPos?>()
        blocks.put(centerPos, controller)

        val repeat = IntArray(this.advfingerLength)
        for (h in 0..<this.advfingerLength) {
            val minH = advaisleRepetitions[h]!![0]
            val maxH = advaisleRepetitions[h]!![1]
            if (minH != maxH) {
                repeat[h] = max(minH, min(maxH, autoBuildSetting.repeatCount))
            } else {
                repeat[h] = minH
            }
        }

        var c = 0
        var z = minZ++
        var r: Int
        while (c < this.advfingerLength) {
            r = 0
            while (r < repeat[c]) {
                cacheLayer.clear()
                var b = 0
                var y = -advcenterOffset[1]
                while (b < this.advthumbLength) {
                    var a = 0
                    var x = -advcenterOffset[0]
                    while (a < this.advpalmLength) {
                        val predicate = this.advblockMatches[c]!![b]!![a]
                        val pos: BlockPos = setActualRelativeOffset(x, y, z, facing, upwardsFacing, isFlipped)
                            .offset(centerPos.x, centerPos.y, centerPos.z)
                        updateWorldState(worldState, pos, predicate)
                        var coilItemStack: ItemStack? = null
                        if (!world.isEmptyBlock(pos)) {
                            if (world.getBlockState(pos).block is CoilBlock && autoBuildSetting.isReplaceCoilMode()
                            ) {
                                coilItemStack = world.getBlockState(pos).block.asItem().defaultInstance
                            } else {
                                blocks.put(pos, world.getBlockState(pos))
                                for (limit in predicate.limited) {
                                    limit.testLimited(worldState)
                                }
                                a++
                                x++
                                continue
                            }
                        }

                        var find = false
                        var infos: Array<BlockInfo?>? = arrayOfNulls(0)
                        for (limit in predicate.limited) {
                            if (limit.minLayerCount > 0 && autoBuildSetting.isPlaceHatch(limit.candidates!!.get())) {
                                val curr = cacheLayer.getInt(limit)
                                if (curr < limit.minLayerCount &&
                                    (limit.maxLayerCount == -1 || curr < limit.maxLayerCount)
                                ) {
                                    cacheLayer.addTo(limit, 1)
                                } else {
                                    continue
                                }
                            } else {
                                continue
                            }
                            infos = if (limit.candidates == null) null else limit.candidates!!.get()
                            find = true
                            break
                        }
                        if (!find) {
                            for (limit in predicate.limited) {
                                if (limit.minCount > 0 && autoBuildSetting.isPlaceHatch(limit.candidates!!.get())) {
                                    val curr = cacheGlobal.getInt(limit)
                                    if (curr < limit.minCount && (limit.maxCount == -1 || curr < limit.maxCount)) {
                                        cacheGlobal.addTo(limit, 1)
                                    } else {
                                        continue
                                    }
                                } else {
                                    continue
                                }
                                infos = if (limit.candidates == null) null else limit.candidates!!.get()
                                find = true
                                break
                            }
                        }
                        if (!find) { // no limited
                            for (limit in predicate.limited) {
                                if (!autoBuildSetting.isPlaceHatch(limit.candidates!!.get())) {
                                    continue
                                }
                                if (limit.maxLayerCount != -1 &&
                                    cacheLayer.getOrDefault(limit, Int.Companion.MAX_VALUE) == limit.maxLayerCount
                                ) {
                                    continue
                                }
                                if (limit.maxCount != -1 &&
                                    cacheGlobal.getOrDefault(limit, Int.Companion.MAX_VALUE) == limit.maxCount
                                ) {
                                    continue
                                }
                                cacheLayer.addTo(limit, 1)
                                cacheGlobal.addTo(limit, 1)
                                infos = ArrayUtils.addAll<BlockInfo?>(
                                    infos,
                                    *if (limit.candidates == null) null else limit.candidates!!.get(),
                                )
                            }
                            for (common in predicate.common) {
                                if (common.candidates != null && predicate.common.size > 1 && !autoBuildSetting.isPlaceHatch(
                                        common.candidates!!.get(),
                                    )
                                ) {
                                    continue
                                }
                                // infos = ArrayUtils.addAll<BlockInfo?>(infos, common.candidates?.get())
                                if (common.candidates?.get() != null) {
                                    common.candidates?.get()?.forEach { info -> infos = ArrayUtils.addAll(infos, info) }
                                } else {
                                    infos = ArrayUtils.addAll(infos, null)
                                }
                            }
                        }

                        val candidates = autoBuildSetting.apply(infos)

                        if (autoBuildSetting.isReplaceCoilMode() && coilItemStack != null && ItemStack.isSameItem(
                                candidates[0],
                                coilItemStack,
                            )
                        ) {
                            a++
                            x++
                            continue
                        }

                        if (candidates.isEmpty()) {
                            a++
                            x++
                            continue
                        }

                        // check inventory
                        val result = foundItem(player, candidates, autoBuildSetting.isUseAE)
                        val found = result.getA()
                        val handler = result.getB()
                        val foundSlot: Int = result.getC()!!

                        if (found == null) {
                            a++
                            x++
                            continue
                        }

                        // check can get old coilBlock
                        var holderHandler: IItemHandler? = null
                        var holderSlot = -1
                        if (autoBuildSetting.isReplaceCoilMode() && coilItemStack != null) {
                            val holderResult = foundHolderSlot(player, coilItemStack)
                            holderHandler = holderResult.getFirst()
                            holderSlot = holderResult.getSecond()!!

                            if (holderHandler != null && holderSlot < 0) {
                                a++
                                x++
                                continue
                            }
                        }

                        if (autoBuildSetting.isReplaceCoilMode() && coilItemStack != null) {
                            world.removeBlock(pos, true)
                            holderHandler?.insertItem(holderSlot, coilItemStack, false)
                        }

                        val itemBlock = found.item as BlockItem
                        val context = BlockPlaceContext(
                            world,
                            player,
                            InteractionHand.MAIN_HAND,
                            found,
                            BlockHitResult.miss(player.getEyePosition(0f), Direction.UP, pos),
                        )
                        val interactionResult = itemBlock.place(context)
                        if (interactionResult != InteractionResult.FAIL) {
                            placeBlockPos.add(pos)
                            handler?.extractItem(foundSlot, 1, false)
                        }
                        if (world.getBlockEntity(pos) is IMachineBlockEntity) {
                            blocks.put(pos, (world.getBlockEntity(pos) as IMachineBlockEntity).metaMachine)
                        } else {
                            blocks.put(pos, world.getBlockState(pos))
                        }

                        a++
                        x++
                    }
                    b++
                    y++
                }
                z++
                r++
            }
            c++
        }
        val frontFacing = controller.self().getFrontFacing()
        blocks.forEach { (pos: BlockPos, block: Any?) ->
            // adjust facing
            if (block !is IMultiController) {
                if (block is BlockState && placeBlockPos.contains(pos)) {
                    resetFacing(
                        pos,
                        block,
                        frontFacing,
                        BiPredicate { p: BlockPos?, f: Direction ->
                            val `object` = blocks[p!!.relative(f)]
                            `object` == null ||
                                (`object` is BlockState && `object`.block === Blocks.AIR)
                        },
                        Consumer { state: BlockState -> world.setBlock(pos, state, 3) },
                    )
                } else if (block is MetaMachine) {
                    resetFacing(
                        pos,
                        block.blockState,
                        frontFacing,
                        BiPredicate { p: BlockPos?, f: Direction ->
                            val `object` = blocks.get(p!!.relative(f))
                            if (`object` == null || (`object` is BlockState && `object`.isAir)) {
                                return@BiPredicate block.isFacingValid(f)
                            }
                            return@BiPredicate false
                        },
                        Consumer { state: BlockState -> world.setBlock(pos, state, 3) },
                    )
                }
            }
        }
    }

    private fun foundItem(player: Player, candidates: MutableList<ItemStack>, isUseAE: Int): Triplet<ItemStack?, IItemHandler?, Int?> {
        var found: ItemStack? = null
        var handler: IItemHandler? = null
        var foundSlot = -1
        if (!player.isCreative) {
            val foundHandler = getMatchStackWithHandler(
                candidates,
                player.getCapability(ForgeCapabilities.ITEM_HANDLER),
                player,
                isUseAE,
            )
            if (foundHandler != null) {
                foundSlot = foundHandler.firstInt()
                handler = foundHandler.second()
                found = handler!!.getStackInSlot(foundSlot).copy()
            }
        } else {
            for (candidate in candidates) {
                found = candidate.copy()
                if (!found!!.isEmpty && found.item is BlockItem) {
                    break
                }
                found = null
            }
        }
        return Triplet<ItemStack?, IItemHandler?, Int?>(found, handler, foundSlot)
    }

    private fun foundHolderSlot(player: Player, coilItemStack: ItemStack): Pair<IItemHandler?, Int?> {
        if (!player.isCreative) {
            val handler: IItemHandler = player.getCapability(ForgeCapabilities.ITEM_HANDLER) as IItemHandler
            var foundSlot = -1
            for (i in 0..<handler.slots) {
                val stack = handler.getStackInSlot(i)
                if (stack.isEmpty) {
                    if (foundSlot < 0) {
                        foundSlot = i
                    }
                } else if (ItemStack.isSameItemSameTags(
                        coilItemStack,
                        stack,
                    ) && (stack.count + 1) <= stack.maxStackSize
                ) {
                    foundSlot = i
                }
            }
            return Pair<IItemHandler?, Int?>(handler, foundSlot)
        } else {
            return Pair<IItemHandler?, Int?>(null, -1)
        }
    }

    private fun clearWorldState(worldState: MultiblockState?) {
        try {
            val clazz = Class.forName("com.gregtechceu.gtceu.api.pattern.MultiblockState")
            // Object obj = clazz.newInstance();
            val method = clazz.getDeclaredMethod("clean")
            method.setAccessible(true)
            method.invoke(worldState)
        } catch (_: java.lang.Exception) {
        }
    }

    private fun updateWorldState(worldState: MultiblockState?, posIn: BlockPos?, predicate: TraceabilityPredicate?) {
        try {
            val clazz = Class.forName("com.gregtechceu.gtceu.api.pattern.MultiblockState")
            val method = clazz.getDeclaredMethod("update", BlockPos::class.java, TraceabilityPredicate::class.java)
            method.setAccessible(true)
            method.invoke(worldState, posIn, predicate)
        } catch (_: java.lang.Exception) {
        }
    }

    private fun setActualRelativeOffset(x: Int, y: Int, z: Int, facing: Direction, upwardsFacing: Direction, isFlipped: Boolean): BlockPos {
        val c0 = intArrayOf(x, y, z)
        val c1 = IntArray(3)
        if (facing == Direction.UP || facing == Direction.DOWN) {
            val of = if (facing == Direction.DOWN) upwardsFacing else upwardsFacing.opposite
            for (i in 0..2) {
                when (advstructureDir[i]!!.getActualDirection(of)) {
                    Direction.UP -> c1[1] = c0[i]
                    Direction.DOWN -> c1[1] = -c0[i]
                    Direction.WEST -> c1[0] = -c0[i]
                    Direction.EAST -> c1[0] = c0[i]
                    Direction.NORTH -> c1[2] = -c0[i]
                    Direction.SOUTH -> c1[2] = c0[i]
                }
            }
            val xOffset = upwardsFacing.stepX
            val zOffset = upwardsFacing.stepZ
            val tmp: Int
            if (xOffset == 0) {
                tmp = c1[2]
                c1[2] = if (zOffset > 0) c1[1] else -c1[1]
                c1[1] = if (zOffset > 0) -tmp else tmp
            } else {
                tmp = c1[0]
                c1[0] = if (xOffset > 0) c1[1] else -c1[1]
                c1[1] = if (xOffset > 0) -tmp else tmp
            }
            if (isFlipped) {
                if (upwardsFacing == Direction.NORTH || upwardsFacing == Direction.SOUTH) {
                    c1[0] = -c1[0] // flip X-axis
                } else {
                    c1[2] = -c1[2] // flip Z-axis
                }
            }
        } else {
            for (i in 0..2) {
                when (advstructureDir[i]!!.getActualDirection(facing)) {
                    Direction.UP -> c1[1] = c0[i]
                    Direction.DOWN -> c1[1] = -c0[i]
                    Direction.WEST -> c1[0] = -c0[i]
                    Direction.EAST -> c1[0] = c0[i]
                    Direction.NORTH -> c1[2] = -c0[i]
                    Direction.SOUTH -> c1[2] = c0[i]
                }
            }
            if (upwardsFacing == Direction.WEST || upwardsFacing == Direction.EAST) {
                val xOffset =
                    if (upwardsFacing == Direction.EAST) {
                        facing.clockWise.stepX
                    } else {
                        facing.clockWise
                            .opposite.stepX
                    }
                val zOffset =
                    if (upwardsFacing == Direction.EAST) {
                        facing.clockWise.stepZ
                    } else {
                        facing.clockWise
                            .opposite.stepZ
                    }
                val tmp: Int
                if (xOffset == 0) {
                    tmp = c1[2]
                    c1[2] = if (zOffset > 0) -c1[1] else c1[1]
                    c1[1] = if (zOffset > 0) tmp else -tmp
                } else {
                    tmp = c1[0]
                    c1[0] = if (xOffset > 0) -c1[1] else c1[1]
                    c1[1] = if (xOffset > 0) tmp else -tmp
                }
            } else if (upwardsFacing == Direction.SOUTH) {
                c1[1] = -c1[1]
                if (facing.stepX == 0) {
                    c1[0] = -c1[0]
                } else {
                    c1[2] = -c1[2]
                }
            }
            if (isFlipped) {
                if (upwardsFacing == Direction.NORTH || upwardsFacing == Direction.SOUTH) {
                    if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                        c1[0] = -c1[0] // flip X-axis
                    } else {
                        c1[2] = -c1[2] // flip Z-axis
                    }
                } else {
                    c1[1] = -c1[1] // flip Y-axis
                }
            }
        }
        return BlockPos(c1[0], c1[1], c1[2])
    }

    private fun getMatchStackWithHandler(candidates: MutableList<ItemStack>, cap: LazyOptional<IItemHandler?>, player: Player, isUseAE: Int): IntObjectPair<IItemHandler?>? {
        val handler = cap.resolve().orElse(null)
        if (handler == null) {
            return null
        }
        for (i in 0..<handler.slots) {
            val stack = handler.getStackInSlot(i)
            if (stack.isEmpty) continue

            val stackCap = stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
            if (stackCap.isPresent) {
                val rt = getMatchStackWithHandler(candidates, stackCap, player, isUseAE)
                if (rt != null) {
                    return rt
                }
            } else if (isUseAE == 1 && stack.item is WirelessTerminalItem && stack.hasTag() && stack.tag!!
                    .contains("accessPoint", 10)
            ) {
                val grid: IGrid? = (stack.item as WirelessTerminalItem).getLinkedGrid(stack, player.level(), player)
                if (grid != null) {
                    val storage = grid.storageService.inventory
                    for (candidate in candidates) {
                        if (storage.extract(AEItemKey.of(candidate), 1, Actionable.MODULATE, null) > 0) {
                            val stacks = NonNullList.withSize(1, candidate)
                            val handler1: IItemHandler = ItemStackHandler(stacks)
                            return IntObjectPair.of<IItemHandler>(0, handler1)
                        }
                    }
                }
            } else if (candidates.stream().anyMatch { candidate: ItemStack ->
                    ItemStack.isSameItemSameTags(
                        candidate,
                        stack,
                    )
                } && !stack.isEmpty && stack.item is BlockItem
            ) {
                return IntObjectPair.of<IItemHandler>(i, handler)
            }
        }
        return null
    }

    private fun resetFacing(pos: BlockPos?, blockState: BlockState, facing: Direction?, checker: BiPredicate<BlockPos?, Direction?>, consumer: Consumer<BlockState?>) {
        if (blockState.hasProperty(BlockStateProperties.FACING)) {
            tryFacings(
                blockState,
                pos,
                checker,
                consumer,
                BlockStateProperties.FACING,
                if (facing == null) FACINGS else ArrayUtils.addAll<Direction?>(arrayOf(facing), *FACINGS),
            )
        } else if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            tryFacings(
                blockState,
                pos,
                checker,
                consumer,
                BlockStateProperties.HORIZONTAL_FACING,
                if (facing == null || facing.axis === Direction.Axis.Y) {
                    FACINGS_H
                } else {
                    ArrayUtils.addAll<Direction?>(
                        arrayOf(facing),
                        *FACINGS_H,
                    )
                },
            )
        }
    }

    private fun tryFacings(blockState: BlockState, pos: BlockPos?, checker: BiPredicate<BlockPos?, Direction?>, consumer: Consumer<BlockState?>, property: Property<Direction?>, facings: Array<Direction?>) {
        var found: Direction? = null
        for (facing in facings) {
            if (checker.test(pos, facing)) {
                found = facing
                break
            }
        }
        if (found == null) {
            found = Direction.NORTH
        }
        consumer.accept(blockState.setValue<Direction?, Direction?>(property, found))
    }
}
