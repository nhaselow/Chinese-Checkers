import java.awt.geom.Ellipse2D;

public class Point<K> {
	private double px,py;
	private K key;
	
	public Point(K key, double px, double py) {
		this.px = px;
		this.py = py;
		this.key = key;
	}
	
	public double getPx() {
		return px;
	}
	
	public double getPy() {
		return py;
	}
	
	public K getKey() {
		return key;
	}
	
	public void setKey(K key) {
		this.key = key;
	}
	
	public boolean equals(Point<K> p) {
		return (px == p.getPx() && py == p.getPy());
	}
	
	public Ellipse2D getEllipse() {
		return new Ellipse2D.Double(px*App.HEX_DIAMETER + App.SCREEN_SIZE/2, 
				py*(App.HEX_DIAMETER - (App.Y_OFFSET - App.VISUAL_OFFSET)) + App.SCREEN_SIZE/2, 
				App.PIECE_DIAMETER, App.PIECE_DIAMETER);
	}
	
	public String toString() {
		return ("(" + px + "," + py + ")");
	}
}