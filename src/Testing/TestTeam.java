package Testing;

import java.util.Objects;

public class TestTeam {

    private Difficulty difficulty;
    private int wins;
    private int id;

    public TestTeam(Difficulty difficulty, int id) {
        this.difficulty = difficulty;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestTeam)) return false;
        TestTeam testTeam = (TestTeam) o;
        return id == testTeam.id;
    }

    public void addWin() {
        wins++;
    }

    public int getWins() {
        return wins;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
