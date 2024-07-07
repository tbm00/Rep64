# Rep64
A spigot plugin that enables player reputations.
Created by tbm00 for play.mc64.wtf.

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

#### Admin Commands
- `/repadmin` Display this command list
- `/repadmin mod <player> <#>` Set player's rep modifier (defaults to 0, added to rep avg)
- `/repadmin mod <player> show` Display player's rep modifier + more
- `/repadmin show <initiator> <receiver>` Display a specific RepEntry
- `/repadmin delete <initiator> <receiver>` Delete a specific RepEntry
- `/repadmin deleterepsby <initiator>` Delete RepEntries created by initiator
- `/repadmin deleterepson <receiver>` Delete RepEntries created on receiver
- `/repadmin reset <player>` Reset PlayerEntry & delete all associated RepEntries
- `/repadmin reload` Reload MySQL database and refresh plugin's caches

## Permissions
#### Player Permissions
- `rep64.show` Ability to view your average reputation.
- `rep64.show.others` Ability to view others' average reputation.
- `rep64.set` Ability to give someone a rep score.

#### Admin Permissions
- `rep64.admin` Ability to use admin commands.
- `rep64.set.others` Ability to give yourself a rep score.

## Config
```
database:
  host: 'host'
  port: '3306'
  database: 'db'
  username: 'user'
  password: 'pass'
  options: '?autoReconnect=true'

autoCacheReloader:
  enabled: true
  ticksBetween: 36000

repScoring:
  defaultRep: 5
  maxRep: 10
  minRep: 0
  maxModifier: 10
  minModifier: -10
```

## Plans
- Finish testing & release the plugin on Spigot.
- Move messages' text to a new lang.yml file, so server admins can modify them.
- Create a listener that will run commands (configurable in config.yml) if a player's rep average is below a threshold for a certain amount of time, or possibly at a specific time.