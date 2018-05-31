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
		PLAYER = new Texture("player/alive.png", 98, 107, 1);                            
		KETCHUP_ALIVE = new Texture("stains/ketchup/alive.png", 33, 25, 1);              
		KETCHUP_DEATH = new Animation("stains/ketchup/frame<4>.png", 24, 1, 33, 25, 1);
		
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
			  BG = new Texture("guis/title/bg.png");
			  CONTINUE = new Texture("guis/title/continue.png");
			  NEWGAME = new Texture("guis/title/newgame.png");
			  SETTINGS = new Texture("guis/title/settings.png");
			  EDITOR = new Texture("guis/title/editor.png");
			  QUIT = new Texture("guis/title/quit.png");
		}
	}
	
	public static class Editor {
		public static Texture BG, LINE, FILLET, REMOVE, SAVE, LOAD;
		
		private static void init() {
			BG = new Texture("guis/editor/bg.png");
			LINE = new Texture("guis/editor/line.png");
			FILLET = new Texture("guis/editor/fillet.png");
			REMOVE = new Texture("guis/editor/remove.png");
			SAVE = new Texture("guis/editor/save.png");
			LOAD = new Texture("guis/editor/load.png");
		}
	}
}
