package constants;

import java.util.ArrayList;
import java.util.List;

import gl.Texture;
import gl.Texture.Anchor;
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
	
	protected static Texture loadButton(String path) {
		return new Texture(path, Anchor.BOTTOM_LEFT); // ALL BUTTONS MUST HAVE TEXTURES ANCHORED BOTTOM LEFT
	}
	
	public static void destroy() {
		for(Surface s : allTextures)
			s.delete();
	}
	
	public static class Title {
		public static Texture BG, CONTINUE, NEWGAME, SETTINGS, EDITOR,  QUIT;
		
		private static void init() {
			  BG = new Texture("guis/title/bg.png");
			  CONTINUE = loadButton("guis/title/continue.png");
			  NEWGAME = loadButton("guis/title/newgame.png");
			  SETTINGS = loadButton("guis/title/settings.png");
			  EDITOR = loadButton("guis/title/editor.png");
			  QUIT = loadButton("guis/title/quit.png");
		}
	}
	
	public static class Editor {
		public static Texture BG, LINE, FILLET, REMOVE;
		
		private static void init() {
			BG = new Texture("guis/editor/bg.png");
			LINE = loadButton("guis/editor/line.png");
			FILLET = loadButton("guis/editor/fillet.png");
			REMOVE = loadButton("guis/editor/remove.png");
		}
	}
}
