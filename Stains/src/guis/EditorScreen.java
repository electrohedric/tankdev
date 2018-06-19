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
import objects.GameObject;
import staindev.Game;
import util.Camera;
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
	
	private Segment ghostWall;
	private Dot ghostDot;
	private Arc ghostArc;
	private List<Segment> map;
	private List<Arc> fillets;
	private Segment[] gridLines;
	private GameObject spawnPoint;
	
	private Tool tool;
	private boolean firstPointDown;
	private int firstClickX;
	private int firstClickY;
	private boolean spawnPointFinalized;
	private boolean filletFirstSelection;
	private boolean filletSelectingRadius;
	
	private boolean scrollingScreen;
	private int lastMouseX;
	private int lastMouseY;
	private long lastCursor;
	protected Camera camera;
	
	private Segment intersecting;
	
	private int GRID_SIZE, GRID_WIDTH, GRID_HEIGHT, GRID_MAX_X, GRID_MAX_Y;
	protected float WALL_WIDTH;
	
	private EditorScreen() {
		super(Textures.Editor.BG);
		RadioButtonChannel toolsChannel = new RadioButtonChannel();
		
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
		elements.add(new RadioButton(toolsChannel, Game.WIDTH * 0.92f, Game.HEIGHT * 0.2f, 1.0f, Textures.Editor.SPAWNPOINT, Mode.EDITOR, true, () -> {
			spawnPointFinalized = false;
			tool = Tool.SPAWNPOINT;
			setMousePointer(Cursors.POINTER);
		}));
		elements.get(0).select();
		
		elements.add(new Button(Game.WIDTH * 0.97f, Game.HEIGHT * 0.95f, 1.0f, Textures.Editor.SAVE, Mode.EDITOR, true, () -> {
			saveMap();
			if(Key.down(GLFW.GLFW_KEY_LEFT_SHIFT) || Key.down(GLFW.GLFW_KEY_RIGHT_SHIFT))
				saveMapImage();
		}));
		elements.add(new Button(Game.WIDTH * 0.90f, Game.HEIGHT * 0.95f, 1.0f, Textures.Editor.LOAD, Mode.EDITOR, true, () -> {
			loadMap();
		}));
		
		this.camera = new Camera(0, 0);
		
		WALL_WIDTH = 3.0f;
		this.ghostWall = new Segment(0, 0, 0, 0, WALL_WIDTH, 127, 127, 127, 255); // instantiate a wall with just a gray color
		this.ghostDot = new Dot(0, 0, WALL_WIDTH * 3, 127, 127, 127, 255);
		this.ghostArc = new Arc(null, null, 0, WALL_WIDTH, 60, 60, 60, 255);
		this.map = new ArrayList<>();
		this.fillets = new ArrayList<>();
		resetClicks();
		
		this.spawnPointFinalized = true;
		this.spawnPoint = new GameObject(Game.WIDTH / 2, Game.HEIGHT / 2, 0, 1.0f);
		spawnPoint.setActiveTexture(Textures.Editor.PLAYER_SPAWN);
		
		this.GRID_SIZE = 35;
		this.GRID_WIDTH = 100;
		this.GRID_HEIGHT = 60;
		this.gridLines = new Segment[GRID_WIDTH + GRID_HEIGHT];
		this.GRID_MAX_X = (GRID_WIDTH - 1) * GRID_SIZE;
		this.GRID_MAX_Y = (GRID_HEIGHT - 1) * GRID_SIZE;
		
		// TODO grid possibly more effecient rendering?
		float gridline_width = 1.0f;
		for(int c = 0; c < GRID_WIDTH; c++)
			gridLines[c] = new Segment(c * GRID_SIZE, 0, c * GRID_SIZE, GRID_MAX_Y, gridline_width, 0, 0, 50, 255);
		for(int r = 0; r < GRID_HEIGHT; r++)
			gridLines[GRID_WIDTH + r] = new Segment(0, r * GRID_SIZE, GRID_MAX_X, r * GRID_SIZE, gridline_width, 0, 0, 50, 255);
		
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
		return grid(camera.getMouseX(), GRID_SIZE);
	}
	
	private int mouseGridY() {
		return grid(camera.getMouseY(), GRID_SIZE);
	}
	
	/**
	 * Attempts to select a segment and colors it according to r,g,b if found. Sets <code>interesting</code> if found
	 */
	private void trySelect(int r, int g, int b) {
		if(intersecting != null)
			intersecting.resetColor();
		float bestAccuracy = 1.0f; // 1.0f == worst possible
		for(Segment wall : map) {
			float accuracy = wall.intersectsPoint(camera.getMouseX(), camera.getMouseY());
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
			trySelect(200, 50, 50);  // red-ish
			break;
		case SELECT:
			trySelect(0, 0, 0);
			break;
		case SPAWNPOINT:
			if(!spawnPointFinalized) {
				spawnPoint.x = camera.getMouseX();
				spawnPoint.y = camera.getMouseY();
			}
			// TODO add limitations to coords
			break;
		}
		if(scrollingScreen) {
			int dx = Mouse.x - lastMouseX;
			int dy = Mouse.y - lastMouseY;
			camera.x -= dx;
			camera.y -= dy;
			lastMouseX = Mouse.x;
			lastMouseY = Mouse.y;
		}
	}
	
	@Override
	public void render() {
		super.render();
		for(Segment gridLine : gridLines)
			gridLine.render(camera);
		for(Arc arc : fillets)
			arc.render(camera);
		if(tool == Tool.FILLET) {
			if(filletSelectingRadius) {
				ghostArc.render(camera);
			}
		}
		for(Segment wall : map)
			wall.render(camera);
		if(tool == Tool.LINE) { // we only need extra rendering for the line tool
			if(isOnMap()) {
				if(firstPointDown)
					ghostWall.render(camera);
				else
					ghostDot.render(camera);
			}
		}
		spawnPoint.render(camera);
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
						fillets.add(new Arc(ghostArc.getTangent1(), ghostArc.getTangent2(), ghostArc.getDistance(), WALL_WIDTH, 250, 250, 250, 255));
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
							map.add(new Segment(firstClickX, firstClickY, clickX, clickY, WALL_WIDTH, 250, 250, 250, 255)); // TODO no duplicate walls and no intersections. color red if these
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
			case SPAWNPOINT:
				spawnPointFinalized = true;
				break;
			}
			if(button == Mouse.MIDDLE) { 
				scrollingScreen = true;
				lastMouseX = Mouse.x;
				lastMouseY = Mouse.y;
				lastCursor = getMousePointer();
				setMousePointer(Cursors.HAND);
			}
			// TODO future: allow zooming for convenience
		}
	}
	
	@Override
	public void handleRelease(int button) {
		if(button == Mouse.MIDDLE) {
			setMousePointer(lastCursor);
			scrollingScreen = false;
		}
	}
	
	private boolean isOnMap() {
		int gx = mouseGridX();
		int gy = mouseGridY();
		return gx >= 0 && gx <= GRID_MAX_X && gy >= 0 && gy <= GRID_MAX_Y;
	}
	
	
	private void saveMapImage() {
		float minX = spawnPoint.x;
		float maxX = minX;
		float minY = spawnPoint.y;
		float maxY = minY;
		for(Segment seg : map) {
			minX = Math.min(minX, Math.min(seg.getX1(), seg.getX2()));
			maxX = Math.max(maxX, Math.max(seg.getX1(), seg.getX2()));
			minY = Math.min(minY, Math.min(seg.getY1(), seg.getY2()));
			maxY = Math.max(maxY, Math.max(seg.getY1(), seg.getY2()));
		}
		int bufferX = 40;
		int bufferY = 30;
		int width = (int) (maxX - minX) + bufferX * 2;
		int height = (int) (maxY - minY) + 1 + bufferY * 2; // TODO is this finished? answer: no. weird offset issue
		
		Camera fboCam = new Camera(minX - bufferX, minY - bufferY);
		FrameBufferRenderBuffer fbo = new FrameBufferRenderBuffer(width, height);
		fbo.activate();// render to the renderbuffer instead of the default fbo for displaying
		Renderer.setClearColor(0, 0, 0);
		glClear(GL_COLOR_BUFFER_BIT); // set background for temp fbo
		
		// render just the parts we want
		float renderWidth = 1.2f;
		for(Arc arc : fillets) {
			arc.setWidth(renderWidth);
			arc.render(fboCam);
			arc.setWidth(WALL_WIDTH);
		}
		for(Segment wall : map) {
			wall.setWidth(renderWidth);
			wall.render(fboCam);
			wall.setWidth(WALL_WIDTH);
		}
		
		spawnPoint.scale = 0.5f;
		spawnPoint.render(fboCam);
		spawnPoint.scale = 1.0f;
		
		// save the fbo to a file
		BufferedImage img = fbo.readPixels();
		
		String name = "mostRecentMap.png";
		try {
		    ImageIO.write(img, "png", new File(Resources.MAPS_PATH + name));
		} catch (IOException e) {
			Log.err("Cannot write to file: " + Resources.MAPS_PATH + name);
			e.printStackTrace();
		}
		fbo.deactivate(); // restore
		fbo.delete();
		Sounds.SCARY.forcePlay();
	}
	
	private void saveMap() {
		String fileName = "mostRecentMap.csmap";
		StringBuilder fileData = new StringBuilder();
		fileData.append("SPAWN\n");
		fileData.append(String.format("%.1f, %.1f\n", spawnPoint.x, spawnPoint.y));
		fileData.append("MAP\n");
		for(Segment seg : map)
			fileData.append(seg.toString() + "\n");
		fileData.append("FILLETS\n");
		for(Arc arc : fillets) {
			String arcString = arc.toString(map);
			if(arcString != null)
				fileData.append(arcString + "\n");
			else {
				Sounds.LEMON.play();
				return;
			}
		}
		fileData.append("END\n");
		
		FileUtil.writeTo(Resources.MAPS_PATH + fileName, fileData.toString());
		Sounds.SPRAY.forcePlay(); // XXX this needs to change
		Log.log("Saved map as " + fileName);
	}
	
	// NOTE when updating this method with new information. Copy and make a new function.
	// name the old function loadMapOld#()
	// TODO add functionality to load using old methods if newer ones fail. i.e. saved using old methods
	private void loadMap() {
		String data = FileUtil.readFrom(Resources.MAPS_PATH + "mostRecentMap.csmap");
		int spawnIndexBegin = data.indexOf("SPAWN\n") + 6; //  XXX this is all temporary until we get format classes done
		int spawnIndexEnd = data.indexOf("MAP\n");
		int pointsIndexBegin = spawnIndexEnd + 4;
		int pointsIndexEnd = data.indexOf("FILLETS\n");
		int filletsIndexBegin = pointsIndexEnd + 8;
		int filletsIndexEnd = data.indexOf("END\n");
		
		map.clear();
		fillets.clear();
		resetClicks();
		
		String spawnString = data.substring(spawnIndexBegin, spawnIndexEnd);
		String[] mapArray = data.substring(pointsIndexBegin, pointsIndexEnd).split("\n");
		String[] filletArray = data.substring(filletsIndexBegin, filletsIndexEnd).split("\n");
		
		String[] spawnCoordsString = spawnString.split(",");
		if(spawnCoordsString.length != 2) {
			Log.err("Error parsing section I");
			return;
		}
		spawnPoint.x = Float.valueOf(spawnCoordsString[0]);
		spawnPoint.y = Float.valueOf(spawnCoordsString[1]); // TODO make functions to load and retrieve formats of data. e.g. saveData(%.1f, %.1f), loadData(%f, %f)
		
		for(String s : mapArray)
			if(!s.equals(""))
				map.add(Segment.fromString(s));
		
		for(String s : filletArray)
			if(!s.equals(""))
				fillets.add(Arc.fromString(s, map));
		
	}

	@Override
	public void switchTo() {
		Game.mode = Mode.EDITOR;
		Music.transition(1.0f, () -> {
			Music.queueLoop(Sounds.EDITOR_LOOP); // TODO new music loop plz
			Music.play();
		});
	}

	
	private static enum Tool {
		SELECT, LINE, FILLET, REMOVE, SPAWNPOINT;
	}
	
}
