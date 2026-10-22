package lanse.worldwarchess.shared.core.screens.pregame;

import javafx.application.Platform;

public class JavaFXInitialization {

    //This is a sketchy class that initializes JavaFX without causing a library crash...

    public static void initJavaFXToolkit() {

        //WHY IS THIS TRUE ON DEFAULT???
        Platform.setImplicitExit(false);

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("Uncaught exception in thread " + t.getName());
            e.printStackTrace();
        });
    }


}
