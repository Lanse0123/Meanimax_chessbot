package lanse.worldwarchess;

public class Changelog {

    /** Remember to add new changes to the TOP of the String, not the bottom. **/

    public static final String changeLog = """
                v1.1.2 - Jan 2 2026:
                * Added a slow mode to NPCs
                * Added global player count getter
                * Added War Gamemode back to singleplayer
                * Added a basic rulebook
                
                
                v1.1.1 - Dec 22 2025:
                * Fixed custom map IDs for singleplayer
                * Fixed a few other sandbox bugs
                * Added fun facts!
                * Attempted to fix Parallel Universe bugs
                
                
                v1.1.0 - Dec 19 2025:
                * Added Sandbox Gamemode (Still a little buggy)
                * Fixed a few more bugs and crashes
                * Started work on making use of custom maps for games
                * Trying to fix final crash: When you kill a piece right after
                 it's king has died, the game sometimes crashes...
                
                
                v1.0.3 - Dec 13 2025:
                * Started work on Sandbox Gamemode
                * Fixed Reset Camera button
                * Added Questionaire mode when making a new game
                * Fixed healer pieces
                * Fixed a few other weird bugs
                
                
                v1.0.2 - Dec 11 2025:
                * Fixed major crash with a king trying to castle 2.14 Billion tiles away
                
                
                v1.0.1 - Dec 10 2025:
                * Added fischer random gamemode to single player
                
                
                v1.0.0 - Dec 8 2025:
                * Added Devlog. More updates coming soon!
                * Make global chat cooler, and added the player name before a message
                * Added player list to lobby
                * Fix crash with some NPCs
                """;


    /** Remember to remove old changes that were made from this **/

    public static final String plannedChanges = """
                * Fix bugs with Multiplayer
                * Fix multiplayer bug which breaks when players rejoin a room
                * Add more bosses and AI
                * Fix Timer Skip Packets so they stop Desyncing
                * Fix parallel universe bugs
                * Add more maps and gamemodes
                * Fix last issues with Tutorial
                * Fix weird bugs with Piece Movement
                * Fix JavaFX causing non deterministic errors from hell with main menu screen
                * Add skins and achivements
                * Bring more Fairy Pieces into the game
                * Add a feedback panel so players can submit ideas for the game, or even submit piece sets to be imported and used.
                """;
}