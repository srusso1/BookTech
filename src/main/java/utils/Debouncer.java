package utils;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class Debouncer {
    private final PauseTransition pauseTransition;

    public Debouncer(long durationMillis) {
        this.pauseTransition = new PauseTransition(Duration.millis(durationMillis));
    }

    public void debounce(Runnable runnable) {
        pauseTransition.setOnFinished(e -> runnable.run());
        pauseTransition.playFromStart();
    }
}
