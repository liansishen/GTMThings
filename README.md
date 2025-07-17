<h1 align="center">
  <a href="https://www.curseforge.com/minecraft/mc-mods/gtmthings"><img src="https://cf.way2muchnoise.eu/1104310.svg?badge_style=for_the_badge" alt="CurseForge"></a>
</h1>

| Mod Version             | GTM Version      |Status|
|-------------------------|------------------|----|
| ≤ 1.4.6                 | 1.6.4            |⛔️ End of Support  |
| 1.4.7 ~ 1.5.2           | 7.0 Snapshot     |⚠️ Not Recommended|
| ≥ 1.5.3                 | 7.0.0 ~ 7.0.1    |🟢 Currently Supported|

# 关于本mod / About This Mod
给GTM添加一些实用的机器 / Add some practical machines to GregTech-Modern.

让GregTech-Modern再次伟大！ / Make GregTech-Modern great again!

# 已添加的机器 / Machines Added
## 创造模式输入总线 / Creative Mode Input Bus
可标记最多25种物品，被标记的物品可直接用于合成，无须额外输入。 / Can tag up to 25 items, tagged items can be used directly in crafting without additional input.

## 创造模式输入仓 / Creative Mode Input Hatch
可标记最多9种流体，被标记的流体可直接用于合成，无需额外输入。 / Can tag up to 9 types of fluids, tagged fluids can be used directly in crafting without additional input.

## 创造模式能源仓 / Creative Mode Energy Hatch
电压：LV-MAX、电流：2A 4A 16A / Voltage: LV-MAX, Current: 2A 4A 16A  
放置后每tick自动恢复至满电状态 / Automatically recharges to full state every tick after placement  
可双仓升压 / Can double the voltage in dual hatches

## 创造模式激光靶仓 / Creative Mode Laser Target Hatch
电压：IV-MAX、电流：256A 1024A 4096A / Voltage: IV-MAX, Current: 256A 1024A 4096A  
放置后每tick自动恢复至满电状态 / Automatically recharges to full state every tick after placement

## 无线能源仓 / Wireless Energy Hatch
电压：LV-HV、电流：2A 4A 16A / Voltage: LV-HV, Current: 2A 4A 16A  
电压：EV-MAX、电流：2A 4A 16A 64A 256A 1024A 4096A 16384A 65536A / Voltage: EV-MAX, Current: 2A 4A 16A 64A 256A 1024A 4096A 16384A 65536A  
放置后自动绑定至放置者所属电网，如果处于FTB组队状态则绑定至所属队伍 / Automatically binds to the electrical grid of the placer upon placement, or to the team if in FTB teams mode  
使用闪存右键可以改变所属者，潜行状态右键则取消绑定状态 / Using a flash drive with a right-click can change the owner, sneaking with a right-click will unbind it  
放置后每tick自动从电网拉取能量至满电状态 / Automatically draws energy from the grid to full state every tick after placement  
可跨维度使用 / Can be used across dimensions  
也有其他模组实现了无线传电功能 https://github.com/EpimorphicPioneers/EUNetwork / Other mods have implemented wireless power transmission features https://github.com/EpimorphicPioneers/EUNetwork

## 无线动力仓 / Wireless Power Hatch
操作同无线能源仓 / Operates the same as the Wireless Energy Hatch  
放置后每tick自动将所有能量存至电网，电网能量存储无上限（BigInteger） / Automatically stores all energy to the grid every tick, the grid's energy storage has no upper limit (BigInteger)  
可跨维度使用 / Can be used across dimensions

## 无线能源监视器 / Wireless Energy Monitor
可查看所属电网中的能量总量和平均输入/输出 / Can view the total energy and average input/output in the owned grid

## 无线能源接收器 / Wireless Energy Receiver
电压：LV-OPV / Voltage: LV-OPV  
作覆盖板时从电网拉取能量传输到机器。 / When used as a cover, it draws energy from the grid to power the machine.  
只可用于单方块机器。无法将超过机器电压等级的覆盖版安装到机器上。 / Only usable on single-block machines. Cannot install a cover that exceeds the machine's voltage level on the machine.  
传输速率为当前电压等级的1A / The transfer rate is 1A of the current voltage level

## 无线物品传输覆盖板 / Wireless Item Transfer Cover
作覆盖板时机器中提取物品到绑定的容器中。 / When used as a cover, it draws Item from the bind container to the machine.

## 无线流体传输覆盖板 / Wireless Fluid Transfer Cover
作覆盖板时机器中提取流体到绑定的容器中。 / When used as a cover, it draws Fluid from the bind container to the machine.

## 可编程电路系列 / Programmable Circuit Series
通过虚拟物品提供器修改机器电路槽的物品。 / Modify the items in the machine circuit slots through a virtual item provider.

