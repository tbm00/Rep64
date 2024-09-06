# Rep64
A spigot plugin that enables player reputations.

Created by tbm00 for play.mc64.wtf.

## Features
- Players can give each other reputation scores, with averages shown to everyone.
- Commands to view detailed reputations lists in game.
- Many admin commands to manage player's reputations in game.
- Ability to run commands on player joins, triggered by the player's average reputation.
- Placeholders returning a player's reputation.
- Configurable reputation score range.

## Dependencies
- **Java 17+**: REQUIRED
- **Spigot 1.18.1+**: UNTESTED ON OLDER VERSIONS
- **MySQL**: REQUIRED
- **PlaceholderAPI**: OPTIONAL

## Commands
#### Player Commands
- `/rep` Display your average reputation
- `/rep help` Display this command list
- `/rep <player>` Display player's average reputation
- `/rep <player> ?` Display the rep score you gave player
- `/rep <player> <#>` Give player a rep score
- `/rep <player> unset` Delete your rep score on player
- `/rep list` Display your rep lists (trimmed data)
- `/rep list <player>` Display player's rep lists (trimmed data)

#### Admin Commands
- `/repadmin` Display this command list
- `/repadmin mod <player> <#>` Set player's rep modifier (gets added to rep avg)
- `/repadmin show <player>` Display player's rep data
- `/repadmin show <initiator> <receiver>` Display a specific RepEntry
- `/repadmin delete <initiator> <receiver>` Delete a specific RepEntry
- `/repadmin deleteRepsBy <initiator>` Delete RepEntries created by initiator
- `/repadmin deleteRepsOn <receiver>` Delete RepEntries created on receiver
- `/repadmin reset <player>` Reset PlayerEntry & delete all associated RepEntries
- `/repadmin reloadCache` Reload plugin's cache

## Permissions
#### Player Permissions
- `rep64.show` Ability to view your average reputation *(default: everyone)*.
- `rep64.show.others` Ability to view others' average reputation *(default: everyone)*.
- `rep64.set` Ability to give others a rep score *(default: everyone)*.
- `rep64.set.self` Ability to give yourself a rep score *(default: op)*. 
- `rep64.list` Ability to view your rep lists (trimmed data) *(default: op)*.
- `rep64.list.others` Ability to view others' rep lists (trimmed data) *(default: op)*.

#### Admin Permissions
- `rep64.admin` Ability to use admin commands *(default: op)*.

## Placeholders
- `rep64_shown` Returns player's RepShown as a double.
- `rep64_shown_int` Returns player's RepShown as an integer.

## Config
```
mysql:
  host: 'host'
  port: 3306
  database: 'db'
  username: 'user'
  password: 'pass'
  useSSL: false
  hikari:
    maximumPoolSize: 12
    minimumPoolSize: 2

autoCacheReloader:
  enabled: true
  ticksBetween: 36000

# Notch's trueAvg = average(rep scores set on Notch by other players)
# Notch's shownAvg = trueAvg + repModifier
# repModifier defaults to 0 & gets applied by staff with `/repadmin mod Notch #`
repScoring:
  # default rep score when a player first joins
  defaultRep: 5
  # maximum rep score a player can give someone else
  maxRep: 10
  # minimum rep score a player can give someone else
  minRep: 0
  # minimum modifier staff can give a player
  maxModifier: 10
  # minimum modifier staff can give a player
  minModifier: -10

# Run commands when a player join the server.
# These are just examples and should be modified.
logicCommandEntries:
  enabled: false
  # leftOperand: trueAvg, shownAvg
  # Operator: ==, !=, >, >=, <, <=
  # rightOperand: a double
  # command: can include <player>, <trueAvg>, and <shownAvg>
  '1':
    enabled: true
    leftOperand: "shownAvg"
    operator: "=="
    rightOperand: 5.0
    command: "say <player>'s rep avg is equal to 5.0!"
  '2':
    enabled: true
    leftOperand: "shownAvg"
    operator: "!="
    rightOperand: 5.0
    command: "say <player>'s rep avg is not equal to 5.0!"
  '3':
    enabled: true
    leftOperand: "trueAvg"
    operator: ">"
    rightOperand: 2.0
    command: "say <player>'s actual rep avg is greater than 2!"
  '4':
    enabled: true
    leftOperand: "trueAvg"
    operator: "<="
    rightOperand: 2.0
    command: "say <player>'s actual rep avg is less than (or equal to) 2!"
  # Add more entries as needed
```