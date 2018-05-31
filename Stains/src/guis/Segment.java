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
	
	/**
	 * Like {@link #Segment(float, float, float, float, float, int, int, int, int) Segment} except takes in parameter color
	 * @param color Color using normalized rgba components
	 */
	public Segment(float x1, float y1, float x2, float y2, float width, Vector4f color) {
		setStartPoint(x1, y1);
		setEndPoint(x2, y2);
		this.width = width;
		this.color = new Vector4f(color);
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
	
	/**
	 * Finds a similarity in the end vertices between this Segment and <strong>other</strong>
	 * @return a Vector2f holding the x,y value of the corner or <code>null</code> if no corner is matched
	 */
	public Vector2f findCorner(Segment other) {
		if((x == other.x && y == other.y) || (x == other.x2 && y == other.y2))
			return new Vector2f(x, y);
		else if((x2 == other.x && y2 == other.y) || (x2 == other.x2 && y2 == other.y2))
			return new Vector2f(x2, y2);
		else
			return null;
	}
	
	public float angleToward(Vector2f corner) {
		if(x == corner.x && y == corner.y)
			return rot;
		else
		return (float) (rot + Math.PI);
	}
	
	/**
	 * @return <code>true</code> if this Segment starts and ends at the same points <strong>other</strong> does
	 */
	public boolean equals(Segment other) {
		return x == other.x && y == other.y && x2 == other.x2 && y2 == other.y2;
	}
	
	public float getAngle() {
		return rot;
	}
	
	/**
	 * @return String to be loaded with <code>fromString()</code>
	 */
	public String toString() {
		return String.format("%.1f,%.1f,%.1f,%.1f", x, y, x2, y2);
	}
	
	/**
	 * @return a new Segment that is intended for collision, not rendering
	 */
	public static Segment fromString(String s) {
		String[] args = s.split(",");
		if(args.length != 4)
			throw new IllegalArgumentException("String must have 4 floats separated by commas");
		float[] values = new float[4];
		try {
			for(int i = 0; i < 4; i++)
				values[i] = Float.parseFloat(args[i]);
		} catch(NumberFormatException nfe) {
			nfe.printStackTrace();
			return null;
		}
		return new Segment(values[0], values[1], values[2], values[3], 3.0f, 250, 250, 250, 255);
	}
	
}
