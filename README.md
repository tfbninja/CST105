# CST105
## UNO
--------
### Overview
UNOConsoleDriver.java can be run to simulate a game of UNO. In it's current state you must run it at least twice, ideally on two+ separate computers so that two different players can play and not see each other's cards. On one instance you select "host" and on all the others you select "join". After the game is started you can play cards and draw cards using a simple notation. The game uses port 42069 for listening for new players. New connections should join automatically once the host is listening.

### Notation
| Action               | Command                     |
| -----------          | -----------                 |
| Draw a card          | **D** or **Draw**           |
| Play red 7           | **Play red 7** or **pr7**   |
| Play blue +2         | **Play blue +2** or **pb+2**|
| Play +4              | **Play +4** or **p+4**      |
| Select the new color | **Green** or **r** or **y** |

### Players
When a player starts the program for the first time, they will be prompted to enter a username which will be stored in *settings.json*. The game will attempt to read the username from this file on future runs. When a player joins your hosted game, there is functionality to kick them. If a player is kicked, their personal ID will be stored in *settings.json* and will be blacklisted, if they attempt to re-join they will be immediately kicked again.

### Logs
Every game instance will generate a log file that documents debug info and most text displayed to the screen, along with timestamps. Logs older than one day are automatically deleted upon the next run.

### Known bugs
- As it currently stands, the TCP/IP functionality messes up the game where after the first round of playing cards the game will not prompt any player to do an action. The game worked when it was solely on console, but the TCP functionality did something to mess it up.
- As a client you will occasionally have to press enter 2x to receive the data sent from the host after an instruction, instead of 1x.

### Fun features
- On startup the program will query the GitHub API to get the timestamp of the last push and let you know the last time the program was updated.
- You can type exit at most points in the game to leave the game
- There is a section of code that generates an arbitrary line of regex.... that was fun to write
- I tried to comment my code at interesting points, read them if you want
