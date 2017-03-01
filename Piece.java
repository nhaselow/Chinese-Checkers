import java.awt.Color;

public class Piece {
	
	private Color color;
	private boolean isHighlighted;
	
	public Piece(Color clr) {
		isHighlighted = false;
		color = clr;
	}
	
	public Color getColor() {
		if(isHighlighted)
			return color;
		else
			return color.darker();
	}
	
	public Color getPlayer() {
		return color;
	}
	
	public boolean isHighlighted() {
		return isHighlighted;
	}
	
	public void highlight(boolean toHighlight) {
		isHighlighted = toHighlight;
	}
}