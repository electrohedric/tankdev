package guis;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector4f;

import staindev.Game;
import util.Camera;

public class Arc {

	private List<Segment> segments;
	private Segment tangent1, tangent2;
	private float distance;
	private boolean altered;
	private float radius;
	private float width;
	private Vector4f color;
	
	public Arc(Segment tangent1, Segment tangent2, float distance, float width, int r, int g, int b, int a) {
		this(tangent1, tangent2, distance, width, new Vector4f(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f));
	}
	
	public Arc(Segment tangent1, Segment tangent2, float distance, float width, Vector4f color) {
		this.segments = new ArrayList<>();
		this.tangent1 = tangent1;
		this.tangent2 = tangent2;
		this.distance = distance;
		setTangent2(tangent2);
		setDistance(distance);
		this.width = width;
		this.radius = 0; // we've got no clue till we generate those segments
		this.color = new Vector4f(color);
		generateSegments();
		this.altered = false;
	}
	
	/**
	 * Generate line segments for drawing an arc which is tangent to the two <em>tangent</em> lines 
	 * and passes through a point <em>distance</em> away from the corner. Radius is not guarenteed until
	 * this method is run, whether called explicitly or run by <code>render()</code>
	 */
	public void generateSegments() { // wrote this myself. wanted to die // TODO maybe possibly enforce limit on radius so it cant span further than the smallest line
		segments.clear();
		if(tangent1 == null || tangent2 == null)
			return; // invalid tangents
		Vector2f corner = tangent1.findCorner(tangent2);
		if(corner == null)
			return; // no corner was found
		// otherwise, we have a valid corner and good tangents
		
		// float versions of useful values of pi
		float PI = (float) Math.PI;
		float HALFPI = (float) (Math.PI / 2);
		float TWOPI = (float) (Math.PI * 2);
		
		// calculate absolute angles based on unit circle with corner at the center
		float thetaA = ((tangent1.angleToward(corner) % TWOPI) + TWOPI) % TWOPI;
		float thetaB = ((tangent2.angleToward(corner) % TWOPI) + TWOPI) % TWOPI;
		boolean badWrap = Math.abs(thetaB - thetaA) > PI; // determine if angle wraps through 360 degrees. we'll have to compensate for this later
		
		// determine angle between the two vectors
		float theta;
		if(badWrap)
			theta = (((TWOPI - Math.abs(thetaB - thetaA)) % PI) + PI) % PI;
		else
			theta = ((Math.abs(thetaB - thetaA) % PI) + PI) % PI;
		
		// calculate radius using a bit of basic trig
		float phi = HALFPI - theta / 2;
		radius = (float) (distance / ((1 / Math.cos(phi)) - 1));
		
		// calculate absolute angle from the corner to center of the circle using trig
		float thetaAvg;
		if(badWrap)
			thetaAvg = (TWOPI + thetaA + thetaB) / 2;
		else
			thetaAvg = (thetaA + thetaB) / 2;
		
		// using that angle, calculate the center of the circle
		float cx = (float) ((distance + radius) * Math.cos(thetaAvg) + corner.x);
		float cy = (float) ((distance + radius) * Math.sin(thetaAvg) + corner.y);
		
		// calculate normal from center of the circle to furthest counterclockwise vector
		float phiNorm;
		if(badWrap ^ (thetaA > thetaB))
			phiNorm = thetaA + HALFPI;
		else
			phiNorm = thetaB + HALFPI;
		
		// using that angle, calculate the position where the arc intersects
		float x1 = (float) (radius * Math.cos(phiNorm) + cx);
		float y1 = (float) (radius * Math.sin(phiNorm) + cy);
		
		// determine number of lines and change in angle over n lines
		int n = (int) Math.min(radius / 3, 40); // no more than 40 lines
		float da = (PI - theta) / n;
		float a = phiNorm;
		
		for(int i = 0; i <= n; i++) {
			float x2 = (float) (radius * Math.cos(a) + cx);
			float y2 = (float) (radius * Math.sin(a) + cy);
			if(x1 != x2 || y1 != y2) {
				segments.add(new Segment(x1, y1, x2, y2, width, color));
				x1 = x2;
				y1 = y2;
			}
			a += da;
		}
	}
	
	/**
	 * Renders this arc to the screen using a camera
	 * @param camera
	 */
	public void render(Camera camera) {
		if(altered) {
			generateSegments();
			altered = false;
		}
		for(Segment seg : segments)
			seg.render(camera);
	}
	
	/**
	 * Renders this arc to the screen using absolute positioning
	 */
	public void render() {
		render(Game.nullCamera);
	}
	
	public void setTangent1(Segment tangent1) {
		this.tangent1 = tangent1;
		altered = true;
	}
	
	public Segment getTangent1() {
		return tangent1;
	}
	
	public void setTangent2(Segment tangent2) {
		this.tangent2 = tangent2;
		altered = true;
	}
	
	public Segment getTangent2() {
		return tangent2;
	}
	
	public void setDistance(float distance) {
		this.distance = distance;
		altered = true;
	}
	
	public float getDistance() {
		return distance;
	}
	
	public void setWidth(float width) {
		for(Segment segment : segments)
			segment.setWidth(width);
	}
	
	public float getWidth() {
		return width;
	}
	
	public float getRadius() {
		return radius;
	}
	
	public void setColor(Vector4f color) {
		this.color.set(color);
	}
	
	public void setColor(int r, int g, int b, int a) {
		color.set(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
	}
	
	/**
	 * @param segments
	 * @return the string to be load with <code>fromString(indexBuffer)</code>
	 */
	public String toString(List<Segment> indexBuffer) {
		int index1 = -1, index2 = -1;
		for(int i = 0; i < indexBuffer.size(); i++) {
			if(tangent1.equals(indexBuffer.get(i)))
				index1 = i;
			else if(tangent2.equals(indexBuffer.get(i)))
				index2 = i;
		}
		if(index1 == -1 || index2 == -1)
			return null; // index buffer does not contain needed tangents
		return String.format("%d,%d,%.2f", index1, index2, distance);
	}
	
	public static Arc fromString(String s, List<Segment> indexBuffer) {
		String[] args = s.split(",");
		if(args.length != 3)
			throw new IllegalArgumentException("String must have 2 integers followed by 1 float separated by commas");
		int t1, t2;
		float dist;
		try {
			t1 = Integer.parseInt(args[0]);
			t2 = Integer.parseInt(args[1]);
			dist = Float.parseFloat(args[2]);
		} catch(NumberFormatException nfe) {
			nfe.printStackTrace();
			return null;
		}
		if(t1 >= indexBuffer.size() || t2 >= indexBuffer.size())
			throw new IllegalArgumentException("Index buffer does not contain correct tangents");
		return new Arc(indexBuffer.get(t1), indexBuffer.get(t2), dist, EditorScreen.getInstance().WALL_WIDTH, 250, 250, 250, 255);
	}
	
}
