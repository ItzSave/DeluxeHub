# DeluxeHub Conditions System - Overzicht

## 📋 Overzicht
Het conditions systeem is toegevoegd aan DeluxeHub menu's waarmee je dynamisch items kunt tonen/verbergen op basis van:
- Player permissies
- PlaceholderAPI placeholders
- Numerieke waarden (level, geld, health, etc.)
- String vergelijkingen (naam, wereld, gamemode, etc.)
- Complexe logica met AND, OR, NOT operators

## 🎯 Alle Beschikbare Operators

### **Permission Operators**
| Operator | Beschrijving | Voorbeeld |
|----------|-------------|-----------|
| `HASPERM` | Check of speler een permissie heeft | `HASPERM deluxehub.vip` |
| `NOTHASPERM` | Check of speler een permissie NIET heeft | `NOTHASPERM deluxehub.banned` |

### **String Operators**
| Operator | Beschrijving | Case Sensitive | Voorbeeld |
|----------|-------------|----------------|-----------|
| `EQUALS` | Waarden zijn exact gelijk | ✅ Ja | `%player_name% EQUALS Notch` |
| `EQUALSIGNORECASE` | Waarden zijn gelijk (hoofdletters negeren) | ❌ Nee | `%player_world% EQUALSIGNORECASE LOBBY` |
| `NOTEQUALS` | Waarden zijn NIET gelijk | ❌ Nee | `%player_gamemode% NOTEQUALS SPECTATOR` |
| `CONTAINS` | Eerste waarde bevat tweede waarde | ❌ Nee | `%player_world% CONTAINS spawn` |
| `STARTSWITH` | Waarde begint met tekst | ❌ Nee | `%player_name% STARTSWITH Admin` |
| `ENDSWITH` | Waarde eindigt met tekst | ❌ Nee | `%player_world% ENDSWITH _nether` |
| `MATCHES` | Waarde matcht regex patroon | ✅ Ja | `%player_name% MATCHES ^[A-Z].*` |

### **Numerieke Operators**
| Operator | Beschrijving | Symbool | Voorbeeld |
|----------|-------------|---------|-----------|
| `ATLEAST` | Groter dan of gelijk aan | `>=` | `%player_level% ATLEAST 10` |
| `ATMOST` | Kleiner dan of gelijk aan | `<=` | `%player_health% ATMOST 5` |
| `GREATERTHAN` | Strikt groter dan | `>` | `%vault_eco_balance% GREATERTHAN 1000` |
| `LESSTHAN` | Strikt kleiner dan | `<` | `%player_food_level% LESSTHAN 10` |

### **Logische Operators**
| Operator | Beschrijving | Voorbeeld |
|----------|-------------|-----------|
| `AND` | Beide condities moeten waar zijn | `HASPERM deluxehub.vip AND %player_level% ATLEAST 5` |
| `OR` | Minimaal één conditie moet waar zijn | `HASPERM deluxehub.vip OR HASPERM deluxehub.admin` |
| `NOT` | Inverteert de conditie | `NOT HASPERM deluxehub.banned` |

## 💡 Praktische Voorbeelden

### Basis Voorbeelden

#### VIP Item
```yaml
vip_diamond:
  material: DIAMOND
  slot: 10
  display_name: "&b&lVIP Diamond"
  conditions: "HASPERM deluxehub.vip"
```

#### Level Requirement
```yaml
high_level_item:
  material: EXPERIENCE_BOTTLE
  slot: 12
  display_name: "&eLevel 50+ Item"
  conditions: "%player_level% ATLEAST 50"
```

#### Wereld Check
```yaml
lobby_only:
  material: COMPASS
  slot: 14
  display_name: "&aLobby Item"
  conditions: "%player_world% CONTAINS lobby"
```

### Geavanceerde Voorbeelden

#### Multiple Conditions (AND logica)
```yaml
premium_item:
  material: NETHER_STAR
  slot: 20
  display_name: "&6Premium Item"
  conditions:
    - "HASPERM deluxehub.premium"
    - "%player_level% ATLEAST 20"
    - "%vault_eco_balance% ATLEAST 5000"
```

#### Complex OR Statement
```yaml
staff_item:
  material: COMMAND_BLOCK
  slot: 22
  display_name: "&cStaff Item"
  conditions: "HASPERM deluxehub.admin OR HASPERM deluxehub.moderator OR HASPERM deluxehub.helper"
```

#### Grouped Conditions
```yaml
special_access:
  material: ENCHANTED_GOLDEN_APPLE
  slot: 24
  display_name: "&dSpecial Access"
  conditions: "(HASPERM deluxehub.vip AND %player_level% ATLEAST 30) OR HASPERM deluxehub.admin"
```

#### NOT Operator
```yaml
non_banned_item:
  material: GREEN_WOOL
  slot: 26
  display_name: "&aActive Player"
  conditions: "NOT HASPERM deluxehub.banned"
```

### PlaceholderAPI Voorbeelden

#### Vault Balance Check
```yaml
rich_player:
  material: GOLD_BLOCK
  slot: 30
  display_name: "&6Rich Player Item"
  conditions: "%vault_eco_balance% GREATERTHAN 100000"
```

#### Health Warning
```yaml
low_health_warning:
  material: RED_DYE
  slot: 32
  display_name: "&c&lLow Health!"
  conditions: "%player_health% LESSTHAN 6"
```

#### Gamemode Check
```yaml
survival_only:
  material: DIAMOND_SWORD
  slot: 34
  display_name: "&eSurvival Item"
  conditions: "%player_gamemode% EQUALS SURVIVAL"
```

#### Time-Based (met PlaceholderAPI)
```yaml
night_item:
  material: CLOCK
  slot: 36
  display_name: "&9Night Item"
  conditions: "%server_time% GREATERTHAN 18000"
```

### Regex Voorbeelden

#### Namen die beginnen met specifieke letter
```yaml
abc_names:
  material: NAME_TAG
  slot: 40
  display_name: "&dA-C Names"
  conditions: "%player_name% MATCHES ^[ABC].*"
```

#### Email-achtig patroon
```yaml
special_format:
  material: PAPER
  slot: 42
  display_name: "&eSpecial Format"
  conditions: "%custom_tag% MATCHES ^[a-z]+_[0-9]+$"
```

## 🔧 Implementatie in Menu

### Menu Structuur
```yaml
slots: 54
title: "&6My Custom Menu"

refresh:
  enabled: true  # Enables live updates
  rate: 40       # Update every 2 seconds (40 ticks)

items:
  my_item:
    material: DIAMOND
    slot: 10
    display_name: "&bMy Item"
    lore:
      - "&7This item has conditions"
    conditions: "HASPERM my.permission"
    actions:
      - "[MESSAGE] &aYou clicked the item!"
```

## 📁 Bestanden

### Nieuwe Java Classes
- `net.zithium.deluxehub.inventory.condition.Condition` - Interface voor conditie evaluatie
- `net.zithium.deluxehub.inventory.condition.ConditionParser` - Parser voor conditie strings

### Aangepaste Classes
- `InventoryItem` - Toegevoegd: conditie support
- `InventoryBuilder` - Toegevoegd: player-specifieke inventory generatie
- `AbstractInventory` - Toegevoegd: player-specifieke inventory methode
- `CustomGUI` - Toegevoegd: conditie parsing vanuit config

### Voorbeeld Config
- `conditional-menu-example.yml` - Complete gids met 20+ voorbeelden

## 🎮 Gebruik

### Menu Openen
Gebruik de bestaande `[MENU]` action:
```yaml
actions:
  - "[MENU] conditional-menu-example"
```

### Menu Maken
1. Kopieer `conditional-menu-example.yml` naar je `menus/` folder
2. Hernoem naar gewenste menu naam (bijv. `vip-shop.yml`)
3. Pas items aan met je eigen conditions
4. Open menu met `[MENU] vip-shop`

## ⚡ Performance

- Conditions worden geëvalueerd wanneer menu wordt geopend
- Met `refresh: enabled: true` worden conditions live ge-update
- PlaceholderAPI placeholders worden per speler vervangen
- Efficiënte parsing: left-to-right evaluatie met early exit

## 🔍 Debug Tips

### Conditie werkt niet?
1. Check spelling van operators (hoofdlettergevoelig!)
2. Check spaties rond operators (verplicht!)
3. Test placeholders met `/papi parse me %placeholder%`
4. Check server logs voor parsing errors

### Voorbeelden van veelgemaakte fouten:
❌ Fout: `%player_level%ATLEAST10` (geen spaties)
✅ Goed: `%player_level% ATLEAST 10`

❌ Fout: `hasperm deluxehub.vip` (lowercase)
✅ Goed: `HASPERM deluxehub.vip`

❌ Fout: `%player_level% >= 10` (verkeerd symbool)
✅ Goed: `%player_level% ATLEAST 10`

## 🚀 Geavanceerde Use Cases

### Rank Progression Menu
```yaml
iron_rank:
  conditions: "NOTHASPERM ranks.gold"  # Niet goud of hoger
  
gold_rank:
  conditions: "HASPERM ranks.gold AND NOTHASPERM ranks.diamond"
  
diamond_rank:
  conditions: "HASPERM ranks.diamond"
```

### Skill-Based Items
```yaml
beginner_quest:
  conditions: "%player_level% LESSTHAN 10"
  
intermediate_quest:
  conditions: "%player_level% ATLEAST 10 AND %player_level% LESSTHAN 50"
  
expert_quest:
  conditions: "%player_level% ATLEAST 50"
```

### Location-Based Menu
```yaml
spawn_teleport:
  conditions: "NOT %player_world% CONTAINS spawn"
  
pvp_zone_item:
  conditions: "%player_world% EQUALS world_pvp"
  
safe_zone_only:
  conditions: "%factions_player_power% GREATERTHAN 0"
```

### Economy Integration
```yaml
tier_1_shop:
  conditions: "%vault_eco_balance% ATLEAST 100"
  
tier_2_shop:
  conditions: "%vault_eco_balance% ATLEAST 1000"
  
tier_3_shop:
  conditions: "%vault_eco_balance% ATLEAST 10000"
  
millionaire_shop:
  conditions: "%vault_eco_balance% GREATERTHAN 1000000"
```

## 🎨 Best Practices

1. **Use refresh for dynamic values**
   - Health, hunger, balance, time
   
2. **Use multiple conditions for AND logic**
   - More readable than long strings
   
3. **Group with brackets for complex logic**
   - Makes intent clearer
   
4. **Test all edge cases**
   - What if placeholder is empty?
   - What if player doesn't have PlaceholderAPI expansion?

5. **Document complex conditions**
   - Add comments to your config
   - Use descriptive lore

## 📝 Notes

- All string operators (except EQUALS) are case-insensitive
- Numeric operators work with doubles (decimals allowed)
- Brackets are single-depth only (no nesting)
- Conditions are parsed left-to-right
- PlaceholderAPI is optional but recommended

## 🔗 Useful Links

- [PlaceholderAPI Placeholders](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders)
- [Regex Tester](https://regex101.com/)
- [DeluxeHub Wiki](https://wiki.lewisdev.fun/free-resources/deluxehub)

