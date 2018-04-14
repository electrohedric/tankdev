package objects;

/**
 * Basically just a tie between Animation and Texture
 */
public interface Renderable {
	
	public void bind(int slot);
	public void bind();
	public void unbind();
	public void delete();

	public int getWidth();
	public int getHeight();

	public float getOffsetX();
	public float getOffsetY();
	public float getOffsetRot();
}
