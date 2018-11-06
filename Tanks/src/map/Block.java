package map;

import gl.Texture;
import objects.GameObject;

public class Block extends GameObject {

	int gridX;
	int gridY;
	
	public Block(float x, float y, float scale, int gridX, int gridY, Texture texture) {
		super(x, y, 0, scale);
		this.setActiveTexture(texture);
		this.gridX = gridX;
		this.gridY = gridY;
	}
	
}
