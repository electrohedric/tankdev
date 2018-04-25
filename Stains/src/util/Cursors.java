package util;

import static org.lwjgl.glfw.GLFW.*;

public class Cursors {

	public static long POINTER, CROSS, HAND;
	
	public static void init() {
		POINTER = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
		CROSS = glfwCreateStandardCursor(GLFW_CROSSHAIR_CURSOR);
		HAND = glfwCreateStandardCursor(GLFW_HAND_CURSOR);
	}
}
