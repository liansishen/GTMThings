<h1 align="center">GTMThings</h1>

<p align="center">
  Practical machines and wireless power infrastructure for <a href="https://github.com/GregTechCEu/GregTech-Modern">GregTech Modern</a>.
</p>

<p align="center">
  <a href="https://www.curseforge.com/minecraft/mc-mods/gtmthings">
    <img src="https://cf.way2muchnoise.eu/1104310.svg?badge_style=for_the_badge" alt="CurseForge">
  </a>
</p>

---

## Compatibility

| Mod Version   | GTM Version   | Status                  |
|---------------|---------------|-------------------------|
| <= 1.4.6      | 1.6.4         | End of Support          |
| 1.4.7 - 1.5.2 | 7.0 Snapshot  | Not Recommended         |
| 1.5.3         | 7.0.0 - 7.0.2 | End of Support          |
| >= 1.5.4      | 7.1.0         | Currently Supported     |

---

## Machines

### Creative Mode Input Bus
Tags up to 25 items. Tagged items are consumed directly in recipes without needing to be physically present in the machine.

### Creative Mode Input Hatch
Tags up to 9 fluids. Tagged fluids are consumed directly in recipes without needing to be physically present in the machine.

### Creative Mode Energy Hatch
- Voltage: LV - MAX, Current: 2A / 4A / 16A
- Recharges to full every tick after placement.
- Supports dual-hatch voltage doubling.

### Creative Mode Laser Target Hatch
- Voltage: IV - MAX, Current: 256A / 1024A / 4096A
- Recharges to full every tick after placement.

---

## Wireless Network

GTMThings adds a wireless energy network system. Machines bind to you (or FTB Teams team if mod is present). Energy is stored in a BigInteger pool with no upper capacity limit.

> An alternative wireless power implementation is available at [EUNetwork](https://github.com/EpimorphicPioneers/EUNetwork).

### Wireless Energy Hatch
Pulls energy from the wireless network into the machine every tick.

- LV - HV: 2A / 4A / 16A
- EV - MAX: 2A / 4A / 16A / 64A / 256A / 1024A / 4096A / 16384A / 65536A
- Cross-dimension support.
- Auto-binds to the placer's network on placement.
- Right-click with a Data Stick to change owner. Sneak + right-click to unbind.

### Wireless Power Hatch
Pushes all stored machine energy into the wireless network every tick. Operates identically to the Wireless Energy Hatch otherwise.

- Cross-dimension support.
- Network storage is unbounded (BigInteger).

### Wireless Energy Monitor
Displays total energy stored in your network alongside average input/output over the last minute, hour, and day.

### Wireless Energy Receiver
Used as a cover. Pulls energy from the wireless network to power the machine it is installed on.

- Voltage: LV - OPV
- Single-block machines only. Cannot exceed the machine's own voltage tier or a Boom happens
- Transfer rate: 1A at the cover's voltage tier.

### Wireless Item Transfer Cover
Used as a cover. Extracts items from a bound container into the machine.

### Wireless Fluid Transfer Cover
Used as a cover. Extracts fluids from a bound container into the machine.

---

## Programmable Circuit Series
Modifies a machine's circuit slot via a virtual item provider, removing the need to physically swap circuits.

---

## Contributing
Issues and PRs welcome. The codebase targets GTM >= 1.5.4 / GTM 7.1.0.

For bug reports please include your mod version, GTM version, and a description of the issue.

---
