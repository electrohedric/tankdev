package objects;

/**
 * Basically just a tie between Animation and Texture
 */
public interface Surface {
	
	public abstract void bind(int slot);
	public abstract void bind();
	public abstract void unbind();
	public abstract void delete();
	
	public abstract int getWidth();
	public abstract int getHeight();
	
	public abstract float getOffsetX();
	public abstract float getOffsetY();
	public abstract float getOffsetRot();
}
