import java.awt.Color;
import java.util.ArrayList;

public class Board {

	/** Board data structure */
	private HexLattice<Piece> board;
	/** Array of Players' Colors */
	private Color[] players;
	/** Board Radius */
	private int radius;
	/** Win Locations */
	private ArrayList<ArrayList<HexNode<Piece>>> winLocs;

	/**
	 * Constructor. Creates a new board with a given radius
	 * @param radius radius of the center area
	 */
	public Board(int radius, Color[] players) {
		/** Error Handling: Radius must be at least 2 */
		if(radius < 2) throw new IllegalArgumentException();
		/** Error Handling: There must be either 2, 4, or 6 players */
		if(players.length != 2 && players.length != 4 && players.length != 6) throw new IllegalArgumentException();

		board = new HexLattice<Piece>();
		this.players = players;
		this.radius = radius;

		winLocs = new ArrayList<ArrayList<HexNode<Piece>>>();
		for(int i = 0; i < players.length; i++) {
			winLocs.add(new ArrayList<HexNode<Piece>>());
		}

		createCenter();
		createHomes();
	}

	/**
	 * Helper method. Creates center area of the board
	 * @param radius radius of the center area
	 */
	private void createCenter() {
		for(int x = -1*(radius-1); x <= radius-1; x++) {
			for(int y = -1*(radius-1); y <= radius-1; y++) {
				if(-1*(x+y) >= -1*(radius-1) && -1*(x+y) <= radius-1)
					board.insert(new HexNode<Piece>(null, x, y, -1*(x+y)));
			}
		}
	}

	/**
	 * Helper method. Creates the triangle areas of the board
	 * @param radius radius of the center area
	 */
	private void createHomes() {
		/** x */
		for(int x = radius; x <= 2*(radius-1); x++) {
			for(int z = -1*(radius-1); z < -1*Math.abs(x-radius); z++) {
				if(players.length == 6) {
					HexNode<Piece> n = new HexNode<Piece>(new Piece(players[5]), x, -1*(x+z), z);
					winLocs.get(4).add(n);
					board.insert(n);
				}
				else
					board.insert(new HexNode<Piece>(null, x, -1*(x+z), z));
			}
		}
		/** -x */
		for(int x = -1*radius; x >= -2*(radius-1); x--) {
			for(int z = radius-1; z > Math.abs(x+radius); z--) {
				if(players.length == 6) {
					HexNode<Piece> n = new HexNode<Piece>(new Piece(players[4]), x, -1*(x+z), z);
					winLocs.get(5).add(n);
					board.insert(n);
				}
				else
					board.insert(new HexNode<Piece>(null, x, -1*(x+z), z));
			}
		}
		/** y */
		for(int y = radius; y <= 2*(radius-1); y++) {
			for(int x = -1*(radius-1); x < -1*Math.abs(y-radius); x++) {
				if(players.length != 2) {
					HexNode<Piece> n = new HexNode<Piece>(new Piece(players[3]), x, y, -1*(x+y));
					winLocs.get(2).add(n);
					board.insert(n);
				}
				else
					board.insert(new HexNode<Piece>(null, x, y, -1*(x+y)));
			}
		}
		/** -y */
		for(int y = -1*radius; y >= -2*(radius-1); y--) {
			for(int x = radius-1; x > Math.abs(y+radius); x--) {
				if(players.length != 2) {
					HexNode<Piece> n = new HexNode<Piece>(new Piece(players[2]), x, y, -1*(x+y));
					winLocs.get(3).add(n);
					board.insert(n);
				}
				else
					board.insert(new HexNode<Piece>(null, x, y, -1*(x+y)));
			}
		}
		/** z */
		for(int z = radius; z <= 2*(radius-1); z++) {
			for(int y = -1*(radius-1); y < -1*Math.abs(z-radius); y++) {
				HexNode<Piece> n = new HexNode<Piece>(new Piece(players[1]), -1*(y+z), y, z);
				winLocs.get(0).add(n);
				board.insert(n);
			}
		}
		/** -z */
		for(int z = -1*radius; z >= -2*(radius-1); z--) {
			for(int y = radius-1; y > Math.abs(z+radius); y--) {
				HexNode<Piece> n = new HexNode<Piece>(new Piece(players[0]), -1*(y+z), y, z);
				winLocs.get(1).add(n);
				board.insert(n);
			}
		}
	}

	/**
	 * Moves a source node to a destination node if the move is valid.
	 * 
	 * @param src Node to move
	 * @param dst Location to move to
	 * @return true if move was valid, false otherwise
	 */
	public boolean move(HexNode<Piece> src, HexNode<Piece> dst) {
		if(!isValidMove(src, dst)) return false;
		board.flipNodes(src, dst);
		return true;
	}

	/**
	 * Determines if the move from Node A to Node B is valid
	 * 
	 * @param src Node A
	 * @param dst Node B
	 * @return true if the move is valid, false otherwise.
	 */
	public boolean isValidMove(HexNode<Piece> src, HexNode<Piece> dst) {
		/** Error handling */
		if(src == null || dst == null) throw new IllegalArgumentException();
		/** Must move to an empty space. Must move a non-empty piece */
		if(src.getKey() == null || dst.getKey() != null) return false;

		/** Searches through valid move list */
		for(HexNode<Piece> n : getValidMoves(src)) {
			if(dst.equals(n)) return true;
		}
		/** Returns false if move not found */
		return false;
	}

	/**
	 * Finds all valid moves on the board for a given node to move to.
	 * 
	 * @param src Node to move
	 * @return List of valid nodes for src node to move to
	 */
	private ArrayList<HexNode<Piece>> getValidMoves(HexNode<Piece> src) {
		ArrayList<HexNode<Piece>> validNodes = new ArrayList<HexNode<Piece>>();
		src.visit(true);

		/** Add valid immediate moves */
		for(HexNode<Piece> p : src.getNeighbors()) {
			if(p.getKey() == null && getDistance(p, src) == 1) 
				validNodes.add(p);
		}
		/** Add valid jumps */
		validNodes.addAll(getValidJumps(src, validNodes));
		/** Return list */
		board.resetVisited();
		return validNodes;
	}

	/**
	 * Determines all valid moves that involve at least one jump
	 * 
	 * @param src Node to move
	 * @param validNodes Arraylist of already valid nodes. This will be added
	 * to in the method.
	 * @return validNodes
	 */
	private ArrayList<HexNode<Piece>> getValidJumps(HexNode<Piece> src, ArrayList<HexNode<Piece>> validNodes) {

		/** Find possible jumps and take them */
		for(HexNode<Piece> nbr : src.getNeighbors()) {
			if(nbr.getKey() != null) {
				/** Possible jump */
				HexNode<Piece> jmp = getJumpNode(src, nbr);
				if(jmp != null && jmp.getKey() == null && !jmp.isVisited()) {
					/** Add to possible jump list */
					validNodes.add(jmp);
					jmp.visit(true);
					/** Recursively check for jumps from the jmp position */
					validNodes.addAll(getValidJumps(jmp, validNodes));
				}
			}
		}
		return validNodes;
	}

	/**
	 * Finds the destination node if the src node were to jump the toJump node
	 * 
	 * @param src Jumping node
	 * @param toJump Node to be jumped
	 * @return Destination node
	 */
	private HexNode<Piece> getJumpNode(HexNode<Piece> src, HexNode<Piece> toJump) {
		/** Error Handling */
		if(src == null || toJump.getKey() == null) throw new IllegalArgumentException();
		if(getDistance(src, toJump) != 1) throw new IllegalArgumentException();


		/** Returns node next in line */
		return board.get(toJump.getX() + (toJump.getX() - src.getX()),
				toJump.getY() + (toJump.getY() - src.getY()),
				toJump.getZ() + (toJump.getZ() - src.getZ()));
	}

	/**
	 * Determines if any player has won. Calls helper method won(int playerIndex)
	 * to check if each individual player has won.
	 * 
	 * @return winning player's index if there is a winner, -1 otherwise
	 */
	public int won() {
		for(int playerIndex = 0; playerIndex < players.length; playerIndex++) {
			if(won(playerIndex)) return playerIndex;
		}
		return -1;
	}
	
	/**
	 * Helper method for won(). Determines if a single player has won the game.
	 * A player has won if both (1) every winLoc contains a piece and (2) over
	 * half of those pieces are the player's.
	 * 
	 * @param playerIndex index of the player from the players array
	 * @return true if the given player has won, false otherwise
	 */
	private boolean won(int playerIndex) {
		/** Number of player's pieces in winLocs */
		double playerCount = 0;
		
		/** For each winLoc... */
		for(HexNode<Piece> n : winLocs.get(playerIndex)) {
			/** If any of the winLocs are empty, the player hasn't won */
			if(n.getKey() == null) return false;
			/** If there is a player's piece in this winLoc, increment count */
			if(n.getKey().getColor().equals(players[playerIndex].darker()))
				playerCount++;
		}
		
		/** If more then 50% of the winLocs are player's pieces, that player wins */
		if((playerCount / (double) winLocs.get(playerIndex).size()) > 0.5) return true;
		else return false;
	}

	/**
	 * Returns the number of nodes on the board
	 * @return size of board
	 */
	public int size() {
		return board.getAllNodes().size();
	}

	/**
	 * Returns the distance between two nodes 
	 * 
	 * @param n1 first node
	 * @param n2 second node
	 * @return distance between n1 and n2
	 */
	public int getDistance(HexNode<Piece> n1, HexNode<Piece> n2) {
		return board.getDistance(n1, n2);
	}
	
	/**
	 * Gets a list of all of the (x,y) coordinates of a given player's pieces
	 * @param playerIndex index of player in player array
	 * @return list of (x,y) coordinates of player's pieces
	 */
	public ArrayList<Point<HexNode<Piece>>> getPlayerPoints(int playerIndex) {
		ArrayList<Point<HexNode<Piece>>> pts = new ArrayList<Point<HexNode<Piece>>>();
		for(HexNode<Piece> n : board.getAllNodes()) {
			if(n.getKey() != null && n.getKey().getPlayer() == players[playerIndex])
				pts.add(n.pointConversion());
		}
		return pts;
	}
	
	/**
	 * Finds the nearest point without a piece in it on the board. 
	 * @param point center point to search around
	 * @return nearest open point, or null if board is full.
	 */
	public Point<HexNode<Piece>> getNearestOpenPoint(Point<HexNode<Piece>> point) {
		
		/** Error Handling: Point and its key must be non-null */
		if(point == null || point.getKey() == null) throw new IllegalArgumentException();
		
		int shortestDistance = radius*2;
		Point<HexNode<Piece>> nearestPoint = null;
		
		for(Point<HexNode<Piece>> p : getPoints()) {
			if(p.getKey().getKey() == null && getDistance(p.getKey(), point.getKey()) < shortestDistance) {
				shortestDistance = getDistance(p.getKey(), point.getKey());
				nearestPoint = p;
			}
		}
		return nearestPoint;
	}

	/**
	 * Gets a list of all of the (x,y) coordinates in the board
	 * @return list of points
	 */
	public ArrayList<Point<HexNode<Piece>>> getPoints() {
		ArrayList<Point<HexNode<Piece>>> pts = new ArrayList<Point<HexNode<Piece>>>();
		for(HexNode<Piece> n : board.getAllNodes()) {
			pts.add(n.pointConversion());
		}
		return pts;
	}
	
	/**
	 * Returns winLocs for a specific player
	 * @param playerIndex index of player in player array
	 * @return list of winLocs for a given player
	 */
	public ArrayList<HexNode<Piece>> getWinLocs(int playerIndex) {
		return winLocs.get(playerIndex);
	}
}