package constants;

import java.util.ArrayList;
import java.util.List;

import gl.Texture;
import objects.Surface;
import util.Animation;

public class Textures {
	
	public static List<Surface> allTextures = new ArrayList<>();
	
	public static Texture PLAYER, KETCHUP_ALIVE;
	public static Animation KETCHUP_DEATH;
	
	public static void init() {
		Texture.setLocalPath("player/");
			PLAYER = new Texture("alive.png", 98, 107, 1);
		
		Texture.setLocalPath("stains/ketchup/");
			KETCHUP_ALIVE = new Texture("alive.png", 33, 25, 1);
			KETCHUP_DEATH = new Animation("frame<4>.png", 24, 1, 33, 25, 1);
			
		Title.init();
		Editor.init();
	}
	
	public static void destroy() {
		for(Surface s : allTextures)
			s.delete();
	}
	
	public static class Title {
		public static Texture BG, CONTINUE, NEWGAME, SETTINGS, EDITOR,  QUIT;
		
		private static void init() {
			Texture.setLocalPath("guis/title/");
				BG = new Texture("bg.png");
				CONTINUE = new Texture("continue.png");
				NEWGAME = new Texture("newgame.png");
				SETTINGS = new Texture("settings.png");
				EDITOR = new Texture("editor.png");
				QUIT = new Texture("quit.png");
		}
	}
	
	public static class Editor {
		public static Texture BG, LINE, FILLET, REMOVE, SAVE, LOAD, SPAWNPOINT, PLAYER_SPAWN;
		
		private static void init() {
			Texture.setLocalPath("guis/editor/");
				BG = new Texture("bg.png");
				LINE = new Texture("line.png");
				FILLET = new Texture("fillet.png");
				REMOVE = new Texture("remove.png");
				SAVE = new Texture("save.png");
				LOAD = new Texture("load.png");
				SPAWNPOINT = new Texture("spawnpoint.png");
				PLAYER_SPAWN = new Texture("player_spawn.png");
		}
	}
}
