package objects;

import java.util.ArrayList;
import java.util.List;

import constants.Resources;
import constants.Sounds;
import gl.Texture;
import guis.Arc;
import guis.Segment;
import util.FileUtil;
import util.Log;

public class Map {

	private Texture bg;
	public List<Spawner> spawners;
	public List<Segment> walls;
	public List<Arc> fillets; 
	public int initialSpawnX;
	public int initialSpawnY;
	
	/**
	 * Load a map based on its critera
	 * @param bg Background texture. Can be null if background is never intended to be rendered
	 * @param spawners List of spawner objects to spawn stains
	 * @param walls List of walls to collide with in the map
	 * @param fillets List of fillets on these walls to curve the corners
	 * @param initialSpawnX spawnpoint x for the player
	 * @param initialSpawnY spawnpoint y for the player
	 */
	public Map(Texture bg, List<Spawner> spawners, List<Segment> walls, List<Arc> fillets, int initialSpawnX, int initialSpawnY) {
		this.bg = bg;
		this.spawners = spawners;
		this.walls = walls;
		this.fillets = fillets;
		this.initialSpawnX = initialSpawnX;
		this.initialSpawnY = initialSpawnY;
	}
	
	/**
	 * Initializes a map with no data except a 0,0 spawnpoint
	 */
	public Map() {
		this(null, new ArrayList<Spawner>(), new ArrayList<Segment>(), new ArrayList<Arc>(), 0, 0);
	}
	
	public static Map loadMap() {
		String data = FileUtil.readFrom(Resources.MAPS_PATH + "mostRecentMap.csmap");
		int spawnIndexBegin = data.indexOf("SPAWN\n") + 6; // XXX this is all temporary until we get format classes done
		int spawnIndexEnd = data.indexOf("MAP\n");
		int pointsIndexBegin = spawnIndexEnd + 4;
		int pointsIndexEnd = data.indexOf("FILLETS\n");
		int filletsIndexBegin = pointsIndexEnd + 8;
		int filletsIndexEnd = data.indexOf("END\n");
		
		List<Spawner> spawners = new ArrayList<>();
		List<Segment> walls = new ArrayList<>();
		List<Arc> fillets = new ArrayList<>();
		
		String spawnString = data.substring(spawnIndexBegin, spawnIndexEnd);
		String[] mapArray = data.substring(pointsIndexBegin, pointsIndexEnd).split("\n");
		String[] filletArray = data.substring(filletsIndexBegin, filletsIndexEnd).split("\n");
		
		String[] spawnCoordsString = spawnString.split(",");
		
		assert spawnCoordsString.length == 2;
		
		int initialSpawnX = Integer.valueOf(spawnCoordsString[0]);
		int initialSpawnY = Integer.valueOf(spawnCoordsString[1]); // TODO make functions to load and retrieve formats of data. e.g. saveData(%.1f, %.1f), loadData(%f, %f)
		
		for(String s : mapArray)
			if(!s.equals(""))
				walls.add(Segment.fromString(s));
		
		for(String s : filletArray)
			if(!s.equals(""))
				fillets.add(Arc.fromString(s, walls));
		
		return new Map(new Texture("some name in the file", false), spawners, walls, fillets, initialSpawnX, initialSpawnY);
	}
	
	public void saveMap() {
		String fileName = "mostRecentMap.csmap";
		StringBuilder fileData = new StringBuilder();
		fileData.append("SPAWN\n");
		fileData.append(String.format("%d, %d\n", initialSpawnX, initialSpawnY));
		fileData.append("MAP\n");
		for(Segment seg : walls)
			fileData.append(seg.toString() + "\n");
		fileData.append("FILLETS\n");
		for(Arc arc : fillets) {
			String arcString = arc.toString(walls);
			if(arcString != null)
				fileData.append(arcString + "\n");
			else {
				Sounds.LEMON.play(); // XXX different sound for saving
				return;
			}
		}
		fileData.append("END\n");
		
		FileUtil.writeTo(Resources.MAPS_PATH + fileName, fileData.toString());
		Sounds.SPRAY.forcePlay(); // XXX this sound needs to change
		Log.log("Saved map as " + fileName);
	}
	
	public void useMap() {
		bg.loadImageToGL();
	}
	
	public void stopUseMap() {
		bg.delete();
	}
	
	public Texture getBackground() {
		return bg;
	}
	
}
