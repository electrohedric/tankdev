package objects;

import constants.StainType;
import constants.Textures;
import entities.Stain;

public class Spawner extends GameObject {
	
	private StainType spawnType;
	
	public Spawner(StainType spawnType, int x, int y) {
		super(x, y, 0.0f, 0.03f); // spawner size really only matters in the EditorScreen which will stay small at 0.03
		this.spawnType = spawnType;
	}
	
	public void spawn() {
		switch(spawnType) {
		case KETCHUP:  //TODO make Stain classes for each
			new Stain(x, y, 0.05f, Textures.KETCHUP_ALIVE, Textures.KETCHUP_DEATH); // for testing
			break;
		default:
			break;
		}
	}
	
}
