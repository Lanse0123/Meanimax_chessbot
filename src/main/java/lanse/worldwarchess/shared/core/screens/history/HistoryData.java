package lanse.worldwarchess.shared.core.screens.history;

import lanse.worldwarchess.shared.server.StartGame;

import javax.swing.*;
import java.nio.file.Path;

public class HistoryData {

    //this class will contain the instructions on how to read / play the game

    public static void initializeGame(Path gamePath, JFrame frame) {
        StartGame.startGame(frame, true, false);

        //On a new thread:
        //TODO - step 1. Read all data from the file and save it to a data structure.
        // Step 2. do all instructions before move{0} and recreate it. Those instructions can then be discarded from memory.
        // Step 3. Store all of the moves into a dataset so when the user clicks left or right, it will
        // read from those instructions to recreate the game.
        // The MoveDecoder class might be extremely useful for this
    }

}
