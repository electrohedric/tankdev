package guis;

import static org.lwjgl.opengl.GL11.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import constants.Shaders;
import gl.Shader;
import objects.Line;
import staindev.Game;

//TODO make this implement a Renderable class which has the matrices, program, slot, x, and y set up, and render() abstract method
public class Segment {
	
	private float x;
	private float y;
	private float x2;
	private float y2;
	private float length;
	private float width;
	private float rot;
	private Vector4f defaultColor;
	private Vector4f color;
	
	private Shader program;
	
	// preallocations
	private Matrix4f proj = new Matrix4f();
	private Matrix4f model = new Matrix4f();
	private Matrix4f mvp = new Matrix4f();
	
	/**
	 * Creates a Line between 2 points
	 * @param x1 X of point 1
	 * @param y1 Y of point 1
	 * @param x2 X of point 2
	 * @param y2 Y of point 2
	 * @param color Color to color the line in normalized RGBA
	 */
	public Segment(float x1, float y1, float x2, float y2, float width, int r, int g, int b, int a) {
		setStartPoint(x1, y1);
		setEndPoint(x2, y2);
		this.width = width;
		this.color = new Vector4f();
		setColor(r, g, b, a);
		this.defaultColor = new Vector4f(color); // duplicate color
		this.program = Shaders.COLOR;
	}

	public void render() {
		program.bind();
		Line.bind(); // binds the VAO
		program.set("u_Color", color);
		// scale to length, rotate, then translate
		proj.set(Game.proj);
		model = model.translation(x, y, 0).rotate(rot, 0.0f, 0.0f, 1.0f).scale(length, length, 1.0f);
		mvp = proj.mul(model); // M x V x P
		program.set("u_MVP", mvp);
		glLineWidth(width);
		glDrawElements(GL_LINES, Line.ibo.length, GL_UNSIGNED_INT, 0);
	}
	
	public void setStartPoint(float x1, float y1) {
		x = x1;
		y = y1;
	}
	
	public void setEndPoint(float x2, float y2) {
		this.x2 = x2;
		this.y2 = y2;
		this.length = (float) Math.sqrt(Math.pow(y2 - y, 2) + Math.pow(x2 - x, 2));  // distance formala between 2 points
		this.rot = (float) Math.atan2(y2 - y, x2 - x);
	}
	
	public void setColor(int r, int g, int b, int a) {
		color.set(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
	}
	
	/**
	 * Method to determine the best guess for which line a point is hovering over
	 * @param px Point x
	 * @param py Point y
	 * @return percent error as a decimal of "closeness" of the point to the line
	 */
	public float intersectsPoint(float px, float py) {
		Vector2f vec1 = new Vector2f(px - x, py - y);
		Vector2f vec2 = new Vector2f(px - x2, py - y2);
		float vecLen = vec1.length() + vec2.length();
		return Math.abs(length - vecLen) / Math.max((2000 - length), 500);
	}
	
	public void resetColor() {
		color.set(defaultColor);
	}
	
}
