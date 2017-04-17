package avion.piedrapapellagarto.model;

public class Gamer {
    String name;
    GameChoice choice;

    public Gamer() {
    }

    public Gamer(String name, GameChoice choice) {
        this.name = name;
        this.choice = choice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GameChoice getChoice() {
        return choice;
    }

    public void setChoice(GameChoice choice) {
        this.choice = choice;
    }
}
