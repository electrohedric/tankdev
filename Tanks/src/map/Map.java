package map;

import constants.Resources;
import constants.Textures;
import main.Game;
import util.FileUtil;
import util.ImageStore;

public class Map {

	// this is the minimum space that must be on both sides of the screen
	protected static final float SPACING_HEIGHT = 0.02f; // 0.01 is ~10 pixels
	protected static final float SPACING_WIDTH = 0.02f; // 0.01 is ~20 pixels
	protected static final int SPACING_HEIGHT_PIXELS = (int) (SPACING_HEIGHT * Game.HEIGHT);
	protected static final int SPACING_WIDTH_PIXELS = (int) (SPACING_WIDTH * Game.WIDTH);
	
	private int width;
	private int height;
	private float totalWidth;
	private float totalHeight;
	private Block[] blocks;
	private float blockScale;
	
	public Map(String imgName) {
		ImageStore store = FileUtil.loadImage(Resources.MAPS_PATH + imgName);
		this.width = store.getWidth();
		this.height = store.getHeight();
		this.blocks = new Block[width * height];
		this.totalWidth = Game.WIDTH - SPACING_WIDTH_PIXELS; // one of these will be resized
		this.totalHeight = Game.HEIGHT - SPACING_HEIGHT_PIXELS; // display size minus spacing on both sides
		float widthRatio = totalWidth / width; // essentially the predicted block scale for each dimension
		float heightRatio = totalHeight / height;
		
		// take the smaller predicted blockscale and adjust the other dimension total size to match
		if(heightRatio < widthRatio) {
			blockScale = (int) heightRatio;
			totalWidth = width * blockScale;
		} else {
			blockScale = (int) widthRatio;
			totalHeight = height * blockScale;
		}
		
		// go through the whole image and set blocks based on brightness
		for(int y = 0; y < store.getHeight(); y++) {
			for(int x = 0; x < store.getWidth(); x++) {
				int light = store.getLight(x, y);
				int i = y * width + x;
				if(light == 0) { // black is solid
					blocks[i] = createSolidBlock(x, y);
				} else if(light == 255) { // white is air
					blocks[i] = createAirBlock(x, y);
				} else { // gray is breakable
					blocks[i] = createBreakableBlock(x, y);
				}
			}
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Block[] getBlocks() {
		return blocks;
	}
	
	public Block getBlock(int x, int y) {
		return blocks[y * width + x];
	}

	public float getBlockScale() {
		return blockScale;
	}
	
	private float displayX(int x) {
		return x * blockScale + SPACING_WIDTH_PIXELS;
	}
	
	private float displayY(int y) {
		return y * blockScale + SPACING_HEIGHT_PIXELS;
	}
	
	private float textureScale() {
		return blockScale * (width / height) / totalHeight;
	}
	
	private BlockSolid createSolidBlock(int x, int y) {
		return new BlockSolid(displayX(x), displayY(y), textureScale(), x, y, Textures.WALL);
	}
	
	private BlockAir createAirBlock(int x, int y) {
		return new BlockAir(displayX(x), displayY(y), textureScale(), x, y);
	}
	
	private BlockBreakable createBreakableBlock(int x, int y) {
		return new BlockBreakable(displayX(x), displayY(y), textureScale(), x, y, Textures.BREAK);
	}
	
	public void render() {
		for(Block b : blocks)
			b.render();
	}
	
}
