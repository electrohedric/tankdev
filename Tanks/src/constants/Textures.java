package constants;

import java.util.ArrayList;
import java.util.List;

import gl.Texture;
import gl.Texture.Anchor;
import objects.Surface;
import util.Animation;

public class Textures {
	
	public static List<Surface> allTextures = new ArrayList<>();
	
	public static Texture BASIC, WALL, BREAK;
	
	public static void init() {
		Texture.setLocalPath("");
			BASIC = new Texture("basictank.png", 0.5f, 0.5f, 1);
			WALL = new Texture("basicwall.png", Anchor.TOP_LEFT);
			BREAK = new Texture("basicbroken.png", Anchor.TOP_LEFT);
		
	}
	
	public static void destroy() {
		for(Surface s : allTextures)
			s.delete();
	}
	
	public static class Title {
		private static void init() {
			
		}
	}
	
}
