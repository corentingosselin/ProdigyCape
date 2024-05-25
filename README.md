<div align="center">
  <img width="1100px" src="https://github.com/max1mde/ProdigyCape/assets/114857048/0dc55096-1f23-4f1c-8ceb-d84af1bb7ea4">
  <h1>Add custom capes to your Minecraft server</h1>
  <h3>Which works even without a mod or resourcepack!</h3>
  <img src="https://github.com/max1mde/ProdigyCape/assets/114857048/40b6942c-4c4a-4736-9db3-1a44868f17a6">
</div>


**ProtocolLib required!**
https://www.spigotmc.org/resources/protocollib.1997/

## Example config
```yml
mojang:
  enabled: true
  texture: eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjc3MDVlM2U5OTdlNWNlNTIxNjY2M2M5ZTY0YjM5NmZhNDNlZGRlODI1NWZkOTEwZjBjYzgxYTAzMjVlNmIifX19
  name: §4Mojang Staff
  description: §7Mojang's official cape
  price: 0
  limited_edition: 0
  number_sold: 0
minecon_creeper:
  enabled: true
  texture: eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzk3NmFhYzc2MjEwYjAzZTRjMzg5MWJkZjc5OTMyMmUzMGE3ZThhMTI3MmIyNzkwMzI2YmYwOGYyMTkyYWNkNiJ9fX0=
  name: §cMinecon 2011
  description: §7Minecon cape from 2011
  price: 0
  limited_edition: 0
  number_sold: 0
minecon_pickaxe:
  enabled: true
  texture: eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTRlNDM1OGQ3MzRhNmUwNjhlYjA3Y2I4ZmM1ZmZkZThiOTQ4MDBlYjM5Njc3NzQyOGE0ZjU1OTMxNWExZmY0ZCJ9fX0=
  name: §2Minecon 2012
  description: §7Minecon cape from 2012
  price: 0
  limited_edition: 0
  number_sold: 0
```

## Commands
```
/cape help - prodigycape.help - Show help menu
/cape apply <cape> - no permission - Apply any owned cape
/cape menu - prodigycape.menu - Show your owned cape in inventory menu
/cape shop - prodigycape.shop - Open inventory menu to buy cape
/cape reload - prodigycape.admin - Reload the configurations
/cape sync - prodigycape.admin - Synchronise capes.yml with database mysql
```

## Permissions
```
Individual cape permission: prodigy.cape.<cape_name>
All cape: prodigy.cape.*
Bypass disabled cape to apply:
prodigy.cape.bypass
```
