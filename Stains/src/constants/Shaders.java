package constants;

import gl.Shader;

public class Shaders {
	
	public static Shader TEXTURE = new Shader("texture.shader", "u_Texture", "u_MVP");
	public static Shader COLOR = new Shader("color.shader", "u_Color", "u_MVP");
	
	public static void destroy() {
		TEXTURE.delete();
		COLOR.delete();
	}
}
