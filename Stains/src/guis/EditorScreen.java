package guis;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import constants.Mode;
import constants.Resources;
import constants.Sounds;
import constants.Textures;
import gl.FrameBufferRenderBuffer;
import gl.Renderer;
import guis.elements.Button;
import guis.elements.RadioButton;
import guis.elements.RadioButtonChannel;
import staindev.Game;
import util.ClickListener;
import util.Cursors;
import util.FileUtil;
import util.Key;
import util.Log;
import util.Mouse;
import util.Music;

/** Singleton which represents the only Editor Screen */
public class EditorScreen extends Gui implements ClickListener {
	
	private static EditorScreen instance;
	protected static float wallWidth = 3.0f;
	
	private Segment ghostWall;
	private Dot ghostDot;
	private Arc ghostArc;
	private List<Segment> map;
	private List<Arc> fillets;
	private Segment[] gridLines;
	
	private Tool tool;
	private boolean firstPointDown;
	private int firstClickX;
	private int firstClickY;
	
	private boolean filletFirstSelection;
	private boolean filletSelectingRadius;
	
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
		elements.add(new Button(Game.WIDTH * 0.97f, Game.HEIGHT * 0.95f, 1.0f, Textures.Editor.SAVE, Mode.EDITOR, true, () -> {
			saveMap();
			if(Key.down(GLFW.GLFW_KEY_LEFT_SHIFT) || Key.down(GLFW.GLFW_KEY_RIGHT_SHIFT))
				saveMapImage();
		}));
		elements.add(new Button(Game.WIDTH * 0.90f, Game.HEIGHT * 0.95f, 1.0f, Textures.Editor.LOAD, Mode.EDITOR, true, () -> {
			loadMap();
		}));
		
		this.ghostWall = new Segment(0, 0, 0, 0, EditorScreen.wallWidth, 127, 127, 127, 255); // instantiate a wall with just a gray color
		this.ghostDot = new Dot(0, 0, EditorScreen.wallWidth * 3, 127, 127, 127, 255);
		this.ghostArc = new Arc(null, null, 0, EditorScreen.wallWidth, 60, 60, 60, 255);
		this.map = new ArrayList<>();
		this.fillets = new ArrayList<>();
		resetClicks();
		
		this.GRID_SIZE = 35;
		this.GRID_OFFSET_X = 50;
		this.GRID_OFFSET_Y = 30;
		this.GRID_WIDTH = 45;
		this.GRID_HEIGHT = 30;
		this.gridLines = new Segment[GRID_WIDTH + GRID_HEIGHT];
		this.GRID_MAX_X = GRID_OFFSET_X + (GRID_WIDTH - 1) * GRID_SIZE;
		this.GRID_MAX_Y = GRID_OFFSET_Y + (GRID_HEIGHT - 1) * GRID_SIZE;
		
		float gridline_width = 1.0f;
		for(int c = 0; c < GRID_WIDTH; c++)
			gridLines[c] = new Segment(c * GRID_SIZE + GRID_OFFSET_X, GRID_OFFSET_Y, c * GRID_SIZE + GRID_OFFSET_X, GRID_MAX_Y, gridline_width, 0, 0, 50, 255);
		for(int r = 0; r < GRID_HEIGHT; r++)
			gridLines[GRID_WIDTH + r] = new Segment(GRID_OFFSET_X, r * GRID_SIZE + GRID_OFFSET_Y, GRID_MAX_X, r * GRID_SIZE + GRID_OFFSET_Y, gridline_width, 0, 0, 50, 255);
		
		ClickListener.addToCallback(this, Mode.EDITOR);
		instance = this;
	}
	
	private void resetClicks() {
		firstPointDown = false;
		firstClickX = 0;
		firstClickY = 0;
		filletFirstSelection = false;
		filletSelectingRadius = false;
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
			if(filletSelectingRadius) {
				Vector2f corner = ghostArc.getTangent1().findCorner(ghostArc.getTangent2());
				Vector2f cursorVec = new Vector2f(mouseGridX() - corner.x, mouseGridY() - corner.y);
				float dist = cursorVec.length();
				if(dist != ghostArc.getDistance()) { // we don't want to force an update if we don't have to
					ghostArc.setDistance(dist);
				}
				
			} else {
				trySelect(50, 50, 200);  // blueish
			}
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
	
	/**
	 * Attempts to select a segment and colors it according to r,g,b if found. Sets <code>interesting</code> if found
	 */
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
		for(Arc arc : fillets)
			arc.render();
		if(tool == Tool.FILLET) {
			if(filletSelectingRadius) {
				ghostArc.render();
			}
		}
		for(Segment wall : map)
			wall.render();
		if(tool == Tool.LINE) { // we only need extra rendering for the line tool
			if(isOnMap()) {
				if(firstPointDown)
					ghostWall.render();
				else
					ghostDot.render();
			}
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
				if(button == Mouse.LEFT) {
					if(filletSelectingRadius) {
						fillets.add(new Arc(ghostArc.getTangent1(), ghostArc.getTangent2(), ghostArc.getDistance(), EditorScreen.wallWidth, 250, 250, 250, 255));
						filletFirstSelection = false;
						filletSelectingRadius = false;
					} else if(intersecting != null) {
						if(filletFirstSelection) {
							ghostArc.setTangent2(intersecting);
							filletFirstSelection = false;
							filletSelectingRadius = true;
						} else {
							ghostArc.setTangent1(intersecting);
							filletFirstSelection = true;
						}
					}
				} else if(button == Mouse.RIGHT){
					filletFirstSelection = false;
					filletSelectingRadius = false;
				}
				break;
			case LINE:
				if(button == Mouse.LEFT) {
					int clickX = mouseGridX();
					int clickY = mouseGridY();
					if(!(clickX == firstClickX && clickY == firstClickY && firstPointDown)) { // don't do anything if starting point and ending point are the same
						if(firstPointDown)
							map.add(new Segment(firstClickX, firstClickY, clickX, clickY, EditorScreen.wallWidth, 250, 250, 250, 255)); // TODO no duplicate walls and no intersections. color red if these
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
	
	private void saveMapImage() {
		FrameBufferRenderBuffer fbo = new FrameBufferRenderBuffer(1920, 1080); // TODO determine image size
		fbo.bind();
		Renderer.setClearColor(0, 0, 0);
		glClear(GL_COLOR_BUFFER_BIT); // set background for temp fbo
		
		// render just the parts we want
		float renderWidth = 1.2f;
		for(Arc arc : fillets) {
			arc.setWidth(renderWidth);
			arc.render();
			arc.setWidth(EditorScreen.wallWidth);
		}
		for(Segment wall : map) {
			wall.setWidth(renderWidth);
			wall.render();
			wall.setWidth(EditorScreen.wallWidth);
		}
		
		// save the fbo to a file
		BufferedImage img = fbo.readPixels();
		fbo.unbind();
		String name = "mostRecentMap.png";
		try {
		    ImageIO.write(img, "png", new File(Resources.MAPS_PATH + name));
		} catch (IOException e) {
			Log.err("Cannot write to file: " + Resources.MAPS_PATH + name);
			e.printStackTrace();
		}
		fbo.delete();
		Sounds.SCARY.forcePlay();
	}
	
	private void saveMap() {
		StringBuilder fileData = new StringBuilder();
		for(Segment seg : map)
			fileData.append(seg.toString() + "\n");
		fileData.append("$\n");
		for(Arc arc : fillets)
			fileData.append(arc.toString(map) + "\n");
		FileUtil.writeTo(Resources.MAPS_PATH + "mostRecentMap.csmap", fileData.toString());
		Sounds.SPRAY.forcePlay();
	}
	
	private void loadMap() {
		String data = FileUtil.readFrom(Resources.MAPS_PATH + "mostRecentMap.csmap");
		String[] dataSplit = data.split("\n\\$\n"); // matches newline $ newline
		if(dataSplit.length != 2) {
			Log.err("Loading map failed. 2 segments of map data expected. Got " + dataSplit.length + ". Map probably corrupted.");
			return;
		}
		map.clear();
		fillets.clear();
		resetClicks();
		String[] mapString = dataSplit[0].split("\n");
		String[] filletString = dataSplit[1].split("\n");
		for(String s : mapString)
			if(!s.equals(""))
				map.add(Segment.fromString(s));
		for(String s : filletString)
			if(!s.equals(""))
				fillets.add(Arc.fromString(s, map));
		
	}

	@Override
	public void switchTo() {
		Game.mode = Mode.EDITOR;
		Music.transition(1.0f, () -> {
			Music.queueLoop(Sounds.EDITOR_LOOP); // TODO new loop plz
			Music.play();
		});
	}

	
	private static enum Tool {
		SELECT, LINE, FILLET, REMOVE
	}
	
}
