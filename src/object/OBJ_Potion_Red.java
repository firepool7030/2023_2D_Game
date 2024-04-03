package object;

import entiiy.Entity;
import main.GamePanel;

public class OBJ_Potion_Red extends Entity {
    GamePanel gp;
    public OBJ_Potion_Red(GamePanel gp) {
        super(gp);

        this.gp = gp;

        type = type_consumable;
        name = "Red Potion";
        value = 5;
        down1 = setup("/objects/potion_red", gp.tileSize, gp.tileSize);
        description = "[Red Potion]\n Heals your life by " + value + ".";

        setDialogue();
    }
    public void setDialogue() {
        dialogues[0][0] = "You drink the " + name + "!\n"
                + "Your life had been recovered by " + value + ".";
    }
    public void use (Entity entity){

        startDialogue(this, 0);
        entity.life += value;
        gp.playSE(2);
    }
}
