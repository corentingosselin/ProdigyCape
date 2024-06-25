<div align="center">
  <a href="https://www.spigotmc.org/resources/116899/"><img src="https://img.shields.io/badge/Minecraft%20version-1.19.4_--_1.20.6-brightgreen.svg" alt="Minecraft version"></a>
  <a href="https://www.spigotmc.org/resources/116899/reviews"><img src="https://img.shields.io/spiget/rating/116899?label=Spigot%20rating" alt="Spigot rating"></a>
  <a href="https://www.spigotmc.org/resources/116899/"><img src="https://img.shields.io/spiget/downloads/116899?label=Spigot%20downloads" alt="Spigot downloads"></a>
  <a href="https://www.paypal.com/donate/?hosted_button_id=56KN7WE2G324C"><img src="https://img.shields.io/badge/Donate-PayPal-blue.svg" alt="Minecraft version"></a>
  <img width="1000px" src="https://github.com/max1mde/ProdigyCape/assets/114857048/1f06b099-42ec-4f9c-9ea9-e6bd669ba4c9">
  <h1>Add custom capes to your Minecraft server</h1>
  <h3>Which works even without a mod or resourcepack!</h3>
  <img src="https://github.com/max1mde/ProdigyCape/assets/114857048/40b6942c-4c4a-4736-9db3-1a44868f17a6">
</div>

> [!CAUTION]
> The Mojang EULA does not allow to sell capes! We are not responsible for your actions!

> [!IMPORTANT]  
> **PacketEvents & PassengerAPI are currently required as dependencies!**     
> [PassengerAPI](https://www.spigotmc.org/resources/passengerapi-entity-passenger-bug-fixes-more.117017/)    
> [PacketEvents](https://www.spigotmc.org/resources/packetevents-api.80279/)    
> Also add [Vault](https://www.spigotmc.org/resources/vault.34315/) if you want to use the shop system


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

# TO-DO List

- [x] Full packet events implementation (No NMS anymore)
- [ ] Better cape physics
- [ ] Java API for developers
