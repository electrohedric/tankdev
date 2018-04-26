package guis;

import java.util.ArrayList;
import java.util.List;

import constants.Mode;
import constants.Sounds;
import constants.Textures;
import guis.elements.RadioButton;
import guis.elements.RadioButtonChannel;
import staindev.Game;
import util.ClickListener;
import util.Cursors;
import util.Mouse;
import util.Music;

/** Singleton which represents the only Editor Screen */
public class EditorScreen extends Gui implements ClickListener {
	
	private static EditorScreen instance;
	
	private Segment ghostWall;
	private Dot ghostDot;
	private List<Segment> map;
	private Segment[] gridLines;
	
	private Tool tool;
	private boolean firstPointDown;
	private int firstClickX;
	private int firstClickY;
	
	private Segment intersecting;
	
	private int GRID_SIZE, GRID_OFFSET_X, GRID_OFFSET_Y, GRID_WIDTH, GRID_HEIGHT, GRID_MAX_X, GRID_MAX_Y;
	
	private EditorScreen() {
		super(Textures.Editor.BG);
		RadioButtonChannel toolsChannel = new RadioButtonChannel();
		tool = Tool.SELECT; // could get overwritten if we select one, but just for safety
 		elements.add(new RadioButton(toolsChannel, Game.WIDTH * 0.92f, Game.HEIGHT * 0.8f, 1.0f, Textures.Editor.LINE, Mode.EDITOR, true, () -> {
 			firstPointDown = false;
			tool = Tool.LINE;
			setMousePointer(Cursors.POINTER);
		}));
		elements.add(new RadioButton(toolsChannel, Game.WIDTH * 0.92f, Game.HEIGHT * 0.6f, 1.0f, Textures.Editor.FILLET, Mode.EDITOR, true, () -> {
			tool = Tool.FILLET;
			setMousePointer(Cursors.HAND);
		}));
		elements.add(new RadioButton(toolsChannel, Game.WIDTH * 0.92f, Game.HEIGHT * 0.4f, 1.0f, Textures.Editor.REMOVE, Mode.EDITOR, true, () -> {
			tool = Tool.REMOVE;
			setMousePointer(Cursors.CROSS);
		}));
		
		this.ghostWall = new Segment(0, 0, 0, 0, 3.0f, 127, 127, 127, 255); // instantiate a wall with just a gray color
		this.ghostDot = new Dot(0, 0, 9.0f, 127, 127, 127, 255);
		this.map = new ArrayList<>();
		this.firstPointDown = false;
		this.firstClickX = 0;
		this.firstClickY = 0;
		
		this.GRID_SIZE = 35;
		this.GRID_OFFSET_X = 50;
		this.GRID_OFFSET_Y = 30;
		this.GRID_WIDTH = 45;
		this.GRID_HEIGHT = 30;
		this.gridLines = new Segment[GRID_WIDTH + GRID_HEIGHT];
		this.GRID_MAX_X = GRID_OFFSET_X + (GRID_WIDTH - 1) * GRID_SIZE;
		this.GRID_MAX_Y = GRID_OFFSET_Y + (GRID_HEIGHT - 1) * GRID_SIZE;
		
		for(int c = 0; c < GRID_WIDTH; c++)
			gridLines[c] = new Segment(c * GRID_SIZE + GRID_OFFSET_X, GRID_OFFSET_Y, c * GRID_SIZE + GRID_OFFSET_X, GRID_MAX_Y, 1.0f, 0, 0, 50, 255);
		for(int r = 0; r < GRID_HEIGHT; r++)
			gridLines[GRID_WIDTH + r] = new Segment(GRID_OFFSET_X, r * GRID_SIZE + GRID_OFFSET_Y, GRID_MAX_X, r * GRID_SIZE + GRID_OFFSET_Y, 1.0f, 0, 0, 50, 255);
		
		ClickListener.addToCallback(this, Mode.EDITOR);
		instance = this;
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
		switch(tool) {
		case FILLET:
			trySelect(50, 50, 200);  // blueish
			break;
		case LINE:
			if(firstPointDown) {
				ghostWall.setEndPoint(mouseGridX(), mouseGridY());
			} else {
				ghostDot.setPos(mouseGridX(), mouseGridY());
			}
			break;
		case REMOVE:
			trySelect(200, 50, 50);  // redish
			break;
		case SELECT:
			trySelect(0, 0, 0);
			break;
		}
	}
	
	private void trySelect(int r, int g, int b) {
		if(intersecting != null)
			intersecting.resetColor();
		float bestAccuracy = 1.0f; // 1.0f == worst possible
		for(Segment wall : map) {
			float accuracy = wall.intersectsPoint(Mouse.x, Mouse.y);
			if(accuracy < bestAccuracy) {
				bestAccuracy = accuracy;
				intersecting = wall;
			}
		}
		if(bestAccuracy < 0.005f)  // if accuracy better than 0.5%, we've got an intersection
			intersecting.setColor(r, g, b, 255); // reset prior intersecting wall's color
		else
			intersecting = null;
	}
	
	@Override
	public void render() {
		super.render();
		for(Segment gridLine : gridLines)
			gridLine.render();
		for(Segment wall : map)
			wall.render();
		if(tool == Tool.LINE) { // we only need extra rendering for the line tool
			if(isOnMap()) {
				if(firstPointDown)
					ghostWall.render();
				else
					ghostDot.render();
			}
		} else if(tool == Tool.FILLET) {
			// TODO eventually fillet too
		}
	}
	
	private int grid(int value, int interval) {
		return (int) Math.floor(((value + interval / 2.0) / interval)) * interval; // floor so -0.5 will round to -1 not 0
	}
	
	@Override
	public void handleClick(int button) { // handle painting
		if(isOnMap()) {
			switch(tool) {
			case FILLET:
				// TODO help me fillet...
				break;
			case LINE:
				if(button == Mouse.LEFT) {
					int clickX = mouseGridX();
					int clickY = mouseGridY();
					if(!(clickX == firstClickX && clickY == firstClickY && firstPointDown)) { // don't do anything if starting point and ending point are the same
						if(firstPointDown)
							map.add(new Segment(firstClickX, firstClickY, clickX, clickY, 3.0f, 250, 250, 250, 255)); // TODO no duplicate walls and no intersections. color red if these
						firstClickX = clickX; // start next wall off where this one ended
						firstClickY = clickY;
						ghostWall.setStartPoint(firstClickX, firstClickY);
						firstPointDown = true;
					}
				} else if(button == Mouse.RIGHT) {
					firstPointDown = false;
				}
				break;
			case REMOVE:
				if(button == Mouse.LEFT) {
					if(intersecting == null) {
						// TODO bounding box
					} else {
						map.remove(intersecting);
					}
				}
				break;
			case SELECT:
				
				break;
			}
		}
	}
	
	private boolean isOnMap() {
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
		Music.transition(2.0f, () -> {
			Music.queueLoop(Sounds.TITLE_INTRO); // TODO new loop plz
			Music.play();
		});
	}

	
	private static enum Tool {
		SELECT, LINE, FILLET, REMOVE
	}
	
}
