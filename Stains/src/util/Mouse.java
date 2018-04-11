package util;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;

import staindev.Game;

public class Mouse {
	
	public static int x;
	public static int y;
	private static double[] xbuf = new double[1];
	private static double[] ybuf = new double[1];
	
	public static void getUpdate() {
		glfwGetCursorPos(Game.window, xbuf, ybuf);
		x = (int) xbuf[0];
		y = (int) (Game.HEIGHT - ybuf[0]);
	}
}
