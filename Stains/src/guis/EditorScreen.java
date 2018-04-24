package guis;

import java.util.ArrayList;
import java.util.List;

import constants.Mode;
import constants.Textures;
import staindev.Game;
import util.ClickListener;
import util.Mouse;

/** Singleton which represents the only Editor Screen */
public class EditorScreen extends Gui implements ClickListener {
	
	private static EditorScreen instance;
	
	private Button lineButton;
	private Button filletButton;
	
	private Wall ghostWall;
	private Dot ghostDot;
	private List<Wall> map;
	private Wall[] gridLines;
	
	private boolean firstPointDown;
	private int firstClickX;
	private int firstClickY;
	
	private Wall intersecting;
	
	private int GRID_SIZE, GRID_OFFSET_X, GRID_OFFSET_Y, GRID_WIDTH, GRID_HEIGHT, GRID_MAX_X, GRID_MAX_Y;
	
	public EditorScreen() {
		super(Textures.Editor.BG);
		// TODO not Buttons, RadioButtons and make it extend Button
 		this.lineButton = new Button(Game.WIDTH * 0.8f, Game.HEIGHT * 0.2f, 1.0f, Textures.Editor.LINEOFF, Textures.Editor.LINEON, Mode.EDITOR, () -> {
			//TODO line button callback
		});
		this.filletButton = new Button(Game.WIDTH * 0.8f, Game.HEIGHT * 0.4f, 1.0f, Textures.Editor.FILLETOFF, Textures.Editor.FILLETON, Mode.EDITOR, () -> {
			//TODO fillet button callback
		});
		this.ghostWall = new Wall(0, 0, 0, 0, 3.0f, 127, 127, 127, 255); // instantiate a wall with essentially just a ghostly gray color
		this.ghostDot = new Dot(0, 0, 9.0f, 127, 127, 127, 255);
		this.map = new ArrayList<>();
		this.firstPointDown = false;
		this.firstClickX = 0;
		this.firstClickY = 0;
		
		this.GRID_SIZE = 35;
		this.GRID_OFFSET_X = 50;
		this.GRID_OFFSET_Y = 30;
		this.GRID_WIDTH = 40;
		this.GRID_HEIGHT = 30;
		this.gridLines = new Wall[GRID_WIDTH + GRID_HEIGHT];
		this.GRID_MAX_X = GRID_OFFSET_X + (GRID_WIDTH - 1) * GRID_SIZE;
		this.GRID_MAX_Y = GRID_OFFSET_Y + (GRID_HEIGHT - 1) * GRID_SIZE;
		
		for(int c = 0; c < GRID_WIDTH; c++)
			gridLines[c] = new Wall(c * GRID_SIZE + GRID_OFFSET_X, GRID_OFFSET_Y, c * GRID_SIZE + GRID_OFFSET_X, GRID_MAX_Y, 1.0f, 0, 0, 50, 255);
		for(int r = 0; r < GRID_HEIGHT; r++)
			gridLines[GRID_WIDTH + r] = new Wall(GRID_OFFSET_X, r * GRID_SIZE + GRID_OFFSET_Y, GRID_MAX_X, r * GRID_SIZE + GRID_OFFSET_Y, 1.0f, 0, 0, 50, 255);
		
		elements.add(lineButton);
		elements.add(filletButton);
		ClickListener.addToCallback(this, Mode.EDITOR);
		instance = this; // allow more calls, but only one instance can exist
	}
	
	public static EditorScreen getInstance() {
		if(instance != null)
			return instance;
		else
			return new EditorScreen();
	}
	
	private int mouseGridX() {
		return grid(Mouse.x - GRID_OFFSET_X, GRID_SIZE) + GRID_OFFSET_X;
	}
	
	private int mouseGridY() {
		return grid(Mouse.y - GRID_OFFSET_Y, GRID_SIZE) + GRID_OFFSET_Y;
	}
	
	@Override
	public void update() {
		super.update();
		if(firstPointDown) {
			ghostWall.setEndPoint(mouseGridX(), mouseGridY());
		} else {
			if(intersecting != null)
				intersecting.setColor(250, 250, 250, 255); // reset prior intersecting wall's color
			float bestAccuracy = 1.0f; // 1.0f == worst possible
			for(Wall wall : map) {
				float accuracy = wall.intersectsPoint(Mouse.x, Mouse.y);
				if(accuracy < bestAccuracy) {
					bestAccuracy = accuracy;
					intersecting = wall;
				}
			}
			if(bestAccuracy < 0.005f) {  // if accuracy better than 0.5%, we've got an intersection
				intersecting.setColor(255, 0, 0, 255);
			} else {
				intersecting = null;
				ghostDot.setPos(mouseGridX(), mouseGridY()); // TODO depends on tool selected not if they're hovering over a wall
			}
		}
	}
	
	@Override
	public void render() {
		super.render();
		for(Wall gridLine : gridLines)
			gridLine.render();
		for(Wall wall : map)
			wall.render();
		if(isOnMap()) {
			if(firstPointDown)
				ghostWall.render();
			else if(intersecting == null)
				ghostDot.render();
		}
	}
	
	private int grid(int value, int interval) {
		return (int) Math.floor(((value + interval / 2.0) / interval)) * interval; // floor so -0.5 will round to -1 not 0
	}
	
	@Override
	public void handleClick(int button) { // handle painting
		if(isOnMap()) {
			if(button == Mouse.LEFT) {
				if(firstPointDown) {
					int endX = mouseGridX();
					int endY = mouseGridY();
					if(endX != firstClickX || endY != firstClickY) { // don't do anything if starting point and ending point are the same
						map.add(new Wall(firstClickX, firstClickY, endX, endY, 3.0f, 250, 250, 250, 255));
						firstPointDown = false;
					}
				} else {
					firstClickX = mouseGridX();
					firstClickY = mouseGridY();
					ghostWall.setStartPoint(firstClickX, firstClickY);
					firstPointDown = true;
				}
			} else if(button == Mouse.RIGHT) {
				if(firstPointDown) {
					firstPointDown = false;
				} else {
					// TODO algorithm to determine closest corner and remove it
				}
			}
		}
	}
	
	private boolean isOnMap() {
		if(lineButton.isMouseHovering() || filletButton.isMouseHovering())
			return false;
		int gx = mouseGridX();
		int gy = mouseGridY();
		return gx >= GRID_OFFSET_X && gx <= GRID_MAX_X && gy >= GRID_OFFSET_Y && gy <= GRID_MAX_Y;
	}
	
	@Override
	public void handleRelease(int button) {
		
	}

	@Override
	public void switchTo() {
		Game.mode = Mode.EDITOR;
		// TODO setup sounds in editor
	}
	
}
