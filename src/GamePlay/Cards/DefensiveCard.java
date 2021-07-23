package GamePlay.Cards;

import java.util.List;

public class DefensiveCard extends Card{

    private List<TypeOfOffensive> against;

    public DefensiveCard(String name, String path, int id, int value, List<TypeOfOffensive> against) {
        super(name, path, id, value);
        this.against = against;
    }

    @Override
    public String getInfo() {
        boolean isBlock = (against.size() == 1);
        if (isBlock) {
            return "Use this card as defense against HIT";
        }
        boolean isDefense = (against.size() == 2);
        if (isDefense) {
            return "Use this card as defense against THEFT or DISARM";
        }
        return "";
    }

    public boolean against(TypeOfOffensive type) {
        return against.contains(type);
    }

}
