import java.util.ArrayList;

/**
 * A HexLattice is represented by a Cube-Coordinate Hex board. It is a list of 
 * HexNodes that are given a unique coordinate (x,y,z) to identify location and 
 * determine neighboring nodes.
 * 
 * See "http://www.redblobgames.com/grids/hexagons/" for more explanation
 */
public class HexLattice<K> {
	
	/** List of all nodes in the lattice */
	private ArrayList<HexNode<K>> nodes;
	
	/**
	 * Default constructor. Creates an empty lattice
	 */
	public HexLattice() {
		nodes = new ArrayList<HexNode<K>>();
	}
	
	/**
	 * Constructor. Creates a lattice with a first node
	 * @param n first node in the lattice
	 */
	public HexLattice(HexNode<K> n) {
		nodes = new ArrayList<HexNode<K>>();
		nodes.add(n);
	}
	
	/**
	 * Insert a node into the lattice
	 * @param n node to insert
	 */
	public void insert(HexNode<K> n) {
		/** Error Handling: No duplicate nodes */
		if(containsNode(n.getX(), n.getY(), n.getZ())) 
			throw new IllegalArgumentException();
		
		/** Adds node to list */
		nodes.add(n);
		
		/** Adjusts Neighbor Fields */
		for(HexNode<K> node : nodes) {
			if(getDistance(n, node) == 1) {
				if(node.getX() > n.getX() && node.getY() < n.getY()) {
					n.addNeighbor(node, Dimension.X, Dimension.Y);
					node.addNeighbor(n, Dimension.Y, Dimension.X);
				}
				else if(node.getX() > n.getX() && node.getZ() < n.getZ()) {
					n.addNeighbor(node, Dimension.X, Dimension.Z);
					node.addNeighbor(n, Dimension.Z, Dimension.X);
				}
				else if(node.getY() > n.getY() && node.getX() < n.getX()) {
					n.addNeighbor(node, Dimension.Y, Dimension.X);
					node.addNeighbor(n, Dimension.X, Dimension.Y);
				}
				else if(node.getY() > n.getY() && node.getZ() < n.getZ()) {
					n.addNeighbor(node, Dimension.Y, Dimension.Z);
					node.addNeighbor(n, Dimension.Z, Dimension.Y);
				}
				else if(node.getZ() > n.getZ() && node.getX() < n.getX()) {
					n.addNeighbor(node, Dimension.Z, Dimension.X);
					node.addNeighbor(n, Dimension.X, Dimension.Z);
				}
				else if(node.getZ() > n.getZ() && node.getY() < n.getY()) {
					n.addNeighbor(node, Dimension.Z, Dimension.Y);
					node.addNeighbor(n, Dimension.Y, Dimension.Z);
				}
			}
		}
	}
	
	/**
	 * Insert an empty node into the lattice given a neighbor and a location to place
	 * it in relative to that neighbor.
	 * @param neighbor neighboring node next to new node
	 * @param inc increasing dimension
	 * @param dec decreasing dimension
	 */
	public void insert(HexNode<K> neighbor, Dimension inc, Dimension dec) {
		/** Error Handling: Must have two separate dimensions to locate the new nodes */
		if(inc == dec) throw new IllegalArgumentException();
		
		/** (x,y,z) coordinate of new node */
		int x = 0;
		int y = 0;
		int z = 0;
		
		/** Adjust increasing dimension */
		if(inc == Dimension.X) x = neighbor.getX() + 1;
		else if(inc == Dimension.Y) y = neighbor.getY() + 1;
		else z = neighbor.getZ() + 1;
		
		/** Adjust decreasing dimension */
		if(dec == Dimension.X) x = neighbor.getX() - 1;
		else if(inc == Dimension.Y) y = neighbor.getY() - 1;
		else z = neighbor.getZ() - 1;
		
		/** Insert node */
		insert(new HexNode<K>(null, x, y, z));
	}
	
	/**
	 * Switches the keys in the two nodes
	 * @param n1 first node
	 * @param n2 second node
	 */
	public void flipNodes(HexNode<K> n1, HexNode<K> n2) {
		K key = n1.getKey();
		n1.setKey(n2.getKey());
		n2.setKey(key);
	}
	
	/**
	 * Determines if a given node is in the lattice
	 * @param n node to search for in lattice
	 * @return true if node is in lattice, false otherwise
	 */
	public boolean containsNode(HexNode<K> n) {
		for(HexNode<K> node : nodes) {
			if(n.equals(node)) return true;
		}
		return false;
	}
	
	/**
	 * Determines if a node with a given (x,y,z) coordinate is in the lattice
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 * @return true if node is in lattice, false otherwise
	 */
	public boolean containsNode(int x, int y, int z) {
		for(HexNode<K> node : nodes) {
			if(x == node.getX() && y == node.getY() && z == node.getZ()) return true;
		}
		return false;
	}
	
	/**
	 * Returns the distance between two nodes. Distance is the shortest number
	 * of steps taken to get from node a to node b without barriers.
	 * @param a first node
	 * @param b second node
	 * @return distance between node a and node b
	 */
	public int getDistance(HexNode<K> a, HexNode<K> b) {
		return ((Math.abs(a.getX() - b.getX()) + 
				Math.abs(a.getY() - b.getY()) +
				Math.abs(a.getZ() - b.getZ()))/2);
				
	}
	
	/**
	 * Searches for a node given an (x,y,z) coordinate
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 * @return node with given (x,y,z) coordinate
	 */
	public HexNode<K> get(int x, int y, int z) {
		for(HexNode<K> n : nodes) {
			if(n.getX() == x && n.getY() == y && n.getZ() == z) return n;
		}
		return null;
	}
	
	/**
	 * Sets all nodes to the unvisited state
	 */
	public void resetVisited() {
		for(HexNode<K> n : nodes) n.visit(false);
	}
	
	/**
	 * Returns a list of all nodes in the lattice
	 * @return list of nodes in HexLattice
	 */
	public ArrayList<HexNode<K>> getAllNodes() {
		return nodes;
	}
}