package GamePlay.Players;

import java.io.Serializable;

public class Team implements Serializable {

    private String name;
    private String imagePath;

    public Team(String name, String imagePath) {
        this.name = name;
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return name.equals(team.getName()) && imagePath.equals(team.getImagePath());
    }



    @Override
    public String toString() {
        return name;
    }
}
