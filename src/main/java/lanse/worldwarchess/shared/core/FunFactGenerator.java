package lanse.worldwarchess.shared.core;

import lanse.worldwarchess.steam.Account;

import java.util.concurrent.ThreadLocalRandom;

public class FunFactGenerator {

    //Adding more here soon.
    private static String[] funFacts = new String[]{"",""};

    public static void initializeFunFacts(){
        funFacts = new String[]{
                "There are currently {COUNT} Fun Facts!",
                "The First Fun Fact ever made says how many fun facts there are!",
                "Click the bar on the right to see more options!",
                "You can make your own custom maps in sandbox!",
                "The most difficult bug to solve was called Parallel Universes! They made multiplayer SO DIFFICULT TO CODE!",
                "James is a magical unicorn!",
                "The Celestials are coming!",
                "This was the first game Lanse ever made!",
                "If you want to add a boss or enemy to this game, draw it, join the Discord in Credits, and show off your creation! If it looks good, it will probably be added!",
                "Lanse is American but never had a Burger or Soda!",
                "The original version of this game took 7 days to make, while the full game with Multiplayer took a few months!",
                "YOUUUUU ARREEEEEEE " + Account.username() + "!!!",
                "Zombies will move randomly!",
                "Vampies will move randomly, but must capture if they can!",
                "The most dangerous level is underground...",
                "Spartans will charge directly at your Commander King!",
                "Assasins will charge directly towards your nearest King!",
                "Sentinels are the smartest current AI!",
                "Beware of the Phoenix!"
        };
    }

    //Since doing this the easy way gave me a null pointer exception:
    public static String getRandomFunFact() {
        int idx = ThreadLocalRandom.current().nextInt(funFacts.length);
        String fact = funFacts[idx].replace("{COUNT}", String.valueOf(funFacts.length));
        return "Fun Fact: " + fact;
    }

    //for testing:
//    public static String getFunFactWithIndex(int index){
//        String fact = funFacts[index].replace("{COUNT}", String.valueOf(funFacts.length));
//        return "Fun Fact: " + fact;
//    }
}
