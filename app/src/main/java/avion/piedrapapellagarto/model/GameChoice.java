package avion.piedrapapellagarto.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import avion.piedrapapellagarto.R;

public class GameChoice {

    public static final GameChoice ROCK;
    public static final GameChoice PAPER;
    public static final GameChoice SCISSORS;
    public static final GameChoice SPOCK;
    public static final GameChoice LIZARD;

    public static final List<GameChoice> ALL_CHOICES;

    static {
        ROCK = new GameChoice(R.drawable.rock);
        PAPER = new GameChoice(R.drawable.paper);
        SCISSORS = new GameChoice(R.drawable.scissors);
        SPOCK = new GameChoice(R.drawable.spock);
        LIZARD = new GameChoice(R.drawable.lizard);


        ROCK.setStrongerAgainst(SCISSORS, LIZARD);
        PAPER.setStrongerAgainst(ROCK, SPOCK);
        SCISSORS.setStrongerAgainst(PAPER, LIZARD);
        SPOCK.setStrongerAgainst(ROCK, SCISSORS);
        LIZARD.setStrongerAgainst(PAPER, SPOCK);

        ALL_CHOICES = Arrays.asList(ROCK, PAPER, SCISSORS, SPOCK, LIZARD);
    }

    public final int drawable;

    private List<GameChoice> strongerAgainst = new ArrayList<>();

    private GameChoice(int drawable) {

        this.drawable = drawable;
    }

    private void setStrongerAgainst(GameChoice ...strongerAgainst) {
        this.strongerAgainst.addAll(Arrays.asList(strongerAgainst));
    }

    public List<GameChoice> getStrongerAgainst() {
        return strongerAgainst;
    }

}
