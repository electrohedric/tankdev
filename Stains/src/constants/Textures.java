package constants;

import java.util.ArrayList;
import java.util.List;

import gl.Texture;
import gl.Texture.Anchor;
import objects.Surface;
import util.Animation;

public class Textures {
	
	public static List<Surface> allTextures = new ArrayList<>();
	
	public static Texture PLAYER = new Texture("player/alive.png", 98, 107, 1);
	public static Texture KETCHUP_ALIVE = new Texture("stains/ketchup/alive.png", 33, 25, 1);
	public static Animation KETCHUP_DEATH = new Animation("stains/ketchup/frame<4>.png", 24, 1, 33, 25, 1);
	
	
	public static Texture loadButton(String path) {
		return new Texture(path, Anchor.BOTTOM_LEFT); // ALL BUTTONS MUST HAVE TEXTURES ANCHORED BOTTOM LEFT
	}
	
	public static void destroy() {
		for(Surface s : allTextures)
			s.delete();
	}
	
	public static class Title {
		public static Texture BG = new Texture("guis/title/bg.png");
		public static Texture CONTINUE = loadButton("guis/title/continue.png");
		public static Texture NEWGAME = loadButton("guis/title/newgame.png");
		public static Texture SETTINGS = loadButton("guis/title/settings.png");
		public static Texture EDITOR = loadButton("guis/title/editor.png");
		public static Texture QUIT = loadButton("guis/title/quit.png");
	}
	
	public static class Editor {
		public static Texture BG = new Texture("guis/editor/bg.png");
		public static Texture LINE = loadButton("guis/editor/line.png");
		public static Texture FILLET = loadButton("guis/editor/fillet.png");
		public static Texture REMOVE = loadButton("guis/editor/remove.png");
	}
}
