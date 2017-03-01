import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

class Surface extends JPanel {
	private static final long serialVersionUID = 1L;

	/** Frame that the surface draws to */
	private App app;
	/** List of all possible moves of piece last clicked on */
	private ArrayList<Point<HexNode<Piece>>> possibleMoves;
	/** Index of the current player's color in the App.PLAYERS field */
	private int currPlayerIndex;
	/** Index of the winning player */
	private int winPlayerIndex;

	/**
	 * Constructs a surface to draw to the screen
	 * @param app frame that surface draws to
	 */
	public Surface(App app) {
		this.app = app;
		currPlayerIndex = 0;
		winPlayerIndex = -1;
		possibleMoves = new ArrayList<Point<HexNode<Piece>>>();
		addMouseListener(new HitTestAdapter());
	}

	/**
	 * Helper method. Draws game to screen
	 * @param g graphics object
	 */
	private void draw(Graphics g) {
		/** Draws Win Sequence */
		if(winPlayerIndex >= 0) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawString("The " + App.PLAYER_NAMES[winPlayerIndex] + " player won!", App.SCREEN_SIZE / 20, App.SCREEN_SIZE / 20);

			for(Point<HexNode<Piece>> p : app.getBoard().getPoints()) {

				/** If there is a piece at this position, draw it */
				if(p.getKey().getKey() != null) {
					if(p.getKey().getKey().getPlayer().equals(App.PLAYERS[winPlayerIndex]))
						g.setColor(p.getKey().getKey().getColor().brighter());
					else g.setColor(p.getKey().getKey().getColor());
					g2d.fill(p.getEllipse());
					g.setColor(Color.BLACK);
					g2d.draw(p.getEllipse());
				}
				/** If there is no piece here and it isn't a possible move, draw an empty space */
				else {
					g.setColor(Color.BLACK);
					g2d.draw(p.getEllipse());
				}
			}
		}
		/** Draws Non-Win Sequence */
		else {
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawString("It's the " + App.PLAYER_NAMES[currPlayerIndex] + " player's turn", App.SCREEN_SIZE / 20, App.SCREEN_SIZE / 20);
			for(Point<HexNode<Piece>> p : app.getBoard().getPoints()) {

				/** If there is a piece at this position, draw it */
				if(p.getKey().getKey() != null) {
					g.setColor(p.getKey().getKey().getColor());
					g2d.fill(p.getEllipse());
					g.setColor(Color.BLACK);
					g2d.draw(p.getEllipse());
				}
				/** Draws move-assistance mechanism */
				else if(App.MOVE_ASSISTANCE && isPossibleMove(p)) {
					g.setColor(TRANSPARENT_GRAY);
					g2d.fill(p.getEllipse());
					g.setColor(Color.BLACK);
					g2d.draw(p.getEllipse());
				}
				/** If there is no piece here and it isn't a possible move, draw an empty space */
				else {
					g.setColor(Color.BLACK);
					g2d.draw(p.getEllipse());
				}
			}
		}
	}

	@Override
	/**
	 * Paints to screen. Calls draw method to draw game
	 * @param g graphics object
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	/**
	 * Detects user interaction with surface
	 * @author Noah Haselow
	 */
	class HitTestAdapter extends MouseAdapter implements Runnable {

		/** Thread that runs application */
		private Thread runner;
		/** Holds whether the program is running */
		private boolean isRunning;
		/** Last HexNode that the user clicked on */
		private HexNode<Piece> toMove;

		/**
		 * Constructs object that detects user interaction
		 * @param surface surface that user is interacting with
		 */
		public HitTestAdapter() {
			isRunning = true;
			runner = new Thread(this);
			runner.start();
		}

		/**
		 * Runs when mouse is clicked
		 * @param e mouse event
		 */
		public void mouseClicked(MouseEvent e) {
			/** Resets possible moves list */
			updatePossibleMoves(null);
			/** For all positions on the board ... */
			for(Point<HexNode<Piece>> p : app.getBoard().getPoints()) {

				/** If there is a piece at this position and it's the player's piece ... */
				if(p.getKey().getKey() != null && isPlayer(p)) {
					/** Highlight it if the user clicked on it */
					p.getKey().getKey().highlight(p.getEllipse().contains(e.getX(), e.getY()));
					/** If the user clicked on it, update possible moves and update toMove field */
					if(p.getEllipse().contains(e.getX(), e.getY())) {
						toMove = p.getKey();
						updatePossibleMoves(toMove);
					}
				}
				/** If there is a piece here and it's NOT the player's piece ... */
				else if(p.getKey().getKey() != null) {
					/** If the user clicked on it ... */
					if(p.getEllipse().contains(e.getX(), e.getY())) {
						/** reset move-assistance */
						toMove = null;
						unhighlight();
					}
				}
				/** If there is not a piece at this position ... */
				else {
					/** Move the toMove piece here if possible and reset move-assistance */
					if(toMove != null && p.getEllipse().contains(e.getX(), e.getY())) {
						if(app.getBoard().move(toMove, p.getKey())) {
							/** Test for winner and run win sequence */
							if(app.getBoard().won() >= 0)
								runWinSequence(app.getBoard().won());
							/** Move to next player and run Computer Player */
							nextPlayer();
							while(currPlayerIndex >= App.NUM_HUMAN_PLAYERS) {
								runComputerPlayer();
								if(app.getBoard().won() >= 0) {
									runWinSequence(app.getBoard().won());
									break;
								}
							}
						}
						toMove = null;
						unhighlight();
					}
				}
			}
		}

		/** Stop running program if the user stops the program or if the window closes */
		public void stop() { isRunning = false; }
		public void destroy() { isRunning = false; }

		@Override
		/**
		 * Paints the screen while the program is running
		 */
		public void run() {
			while(isRunning) {

				/** If a player won, wait 5 seconds, then create a new game */
				if(winPlayerIndex >= 0) {
					repaint();

					try {
						Thread.sleep(5000);
					} catch(InterruptedException e) {
						Logger.getLogger(Surface.class.getName()).log(Level.SEVERE, null, e);
					}

					winPlayerIndex = -1;
					app.newGame();
				}

				/** Repaint */
				try {
					Thread.sleep(50);
				} catch(InterruptedException e) {
					Logger.getLogger(Surface.class.getName()).log(Level.SEVERE, null, e);
				}
				repaint();
			}
		}
	}//end HitTestAdapter class

	/**
	 * Unhighlights selected piece and resets move-assistance mechanism
	 */
	private void unhighlight() {
		for(Point<HexNode<Piece>> p : app.getBoard().getPoints()) {
			if(p.getKey().getKey() != null) p.getKey().getKey().highlight(false);
			possibleMoves.clear();
		}
	}

	/**
	 * Determines which spots on board are possible moves given a piece to move.
	 * Holds these moves in the private field possibleMoves.
	 * 
	 * @param src node to move
	 */
	private void updatePossibleMoves(HexNode<Piece> src) {

		possibleMoves.clear();
		if(src == null || src.getKey() == null) return;

		for(Point<HexNode<Piece>> p : app.getBoard().getPoints()) {
			if(p.getKey() != null && app.getBoard().isValidMove(src, p.getKey())) {
				possibleMoves.add(p);
			}
		}
	}

	/**
	 * Determines if a position is in the possibleMoves list
	 * @param p position to check
	 * @return true if the point is in the list, false otherwise
	 */
	private boolean isPossibleMove(Point<HexNode<Piece>> p) {
		for(Point<HexNode<Piece>> poss : possibleMoves) {
			if(p.equals(poss)) return true;
		}
		return false;
	}

	/**
	 * Determines if there is a player at a given point
	 * 
	 * @param p point to check
	 * @return true if there is a player at the point, false otherwise.
	 */
	private boolean isPlayer(Point<HexNode<Piece>> p) {
		if(p == null || p.getKey().getKey() == null) return false;
		return (p.getKey().getKey().getPlayer().equals(App.PLAYERS[currPlayerIndex]));
	}

	/**
	 * Sets the currPlayerIndex field to the next player's index
	 */
	private void nextPlayer() {
		if(currPlayerIndex == App.PLAYERS.length - 1)
			currPlayerIndex = 0;
		else
			currPlayerIndex++;
	}

	/**
	 * Runs the AI for the current computer player and moves their piece.
	 * 
	 * BUG : Computer Players won't win; they'll only get close
	 */
	private void runComputerPlayer() {
		Point<HexNode<Piece>> cornerPiece = findCornerPiece(currPlayerIndex).pointConversion();
		Point<HexNode<Piece>> destPiece = app.getBoard().getNearestOpenPoint(cornerPiece);

		Point<HexNode<Piece>> bestPiece = null;
		Point<HexNode<Piece>> bestMove = null;
		double bestScore = Math.pow(App.BOARD_RADIUS - 1, 4);

		/** For each of the current player's pieces... */
		for(Point<HexNode<Piece>> p : app.getBoard().getPlayerPoints(currPlayerIndex)) {
			/** For each possible move from the current piece... */
			updatePossibleMoves(p.getKey());
			for(Point<HexNode<Piece>> poss : possibleMoves) {
				/** Record the move if it is better than the best previous one */
				if(calculateScore(p.getKey(), poss.getKey(), destPiece.getKey()) < bestScore) {
					bestPiece = p;
					bestMove = poss;
					bestScore = calculateScore(p.getKey(), poss.getKey(), destPiece.getKey());
				}
			}
		}

		if(bestPiece != null && bestMove != null)
			app.getBoard().move(bestPiece.getKey(), bestMove.getKey());
		nextPlayer();
	}

	/**
	 * Helper Method for AI. Finds the nearest open location from the farthest winLoc
	 * @param playerIndex computer player running
	 * @return nearest open location from farthest winLoc
	 */
	private HexNode<Piece> findCornerPiece(int playerIndex) {
		ArrayList<HexNode<Piece>> winLocs = app.getBoard().getWinLocs(currPlayerIndex);
		HexNode<Piece> cornerPiece = null;
		for(HexNode<Piece> n : winLocs) {
			if(Math.abs(n.getX()) >= (App.BOARD_RADIUS-1)*2 ||
					Math.abs(n.getY()) >= (App.BOARD_RADIUS-1)*2 ||
					Math.abs(n.getZ()) >= (App.BOARD_RADIUS-1)*2) {
				cornerPiece = n;
			}
		}
		return cornerPiece;
	}

	/**
	 * Helper Method for AI. Calculates the score for a possible move. A score is
	 * the total (mean-square) distance of each piece from the open location closest 
	 * to the corner winLoc.
	 * @param src possible source node
	 * @param dst possible destination node
	 * @param goal open location closest to the corner winLoc
	 * @return score
	 */
	private double calculateScore(HexNode<Piece> src, HexNode<Piece> dst, HexNode<Piece> goal) {
		double score = 0;
		app.getBoard().move(src, dst);

		for(Point<HexNode<Piece>> p : app.getBoard().getPlayerPoints(currPlayerIndex)) {
			score += Math.pow(app.getBoard().getDistance(p.getKey(), goal), 2);
		}

		app.getBoard().move(dst, src);
		return Math.sqrt(score);
	}

	/**
	 * Runs the Win Sequence after a player won the game.
	 */
	private void runWinSequence(int playerIndex) {
		winPlayerIndex = playerIndex;
	}

	/** Color for move-assistance mechanism */
	private static final Color TRANSPARENT_GRAY = new Color(128, 128, 128, 100);
} //end surface class

/**
 * Runs application and holds application frame
 * @author Noah Haselow
 */
public class App extends JFrame {
	private static final long serialVersionUID = 1L;

	/** OFFICIAL BOARD */
	private Board board;

	/**
	 * Constructs application by initializing the board and the screen
	 */
	public App() {
		initBoard();
		initScreen();
	}

	/**
	 * Initializes the board given a radius and array of players
	 */
	private void initBoard() {
		board = new Board(BOARD_RADIUS, PLAYERS);
	}

	/**
	 * Creates the screen and adds a surface to it to draw on
	 */
	private void initScreen() {
		add(new Surface(this));

		pack();
		setTitle("Chinese Checkers");
		setSize(SCREEN_SIZE, SCREEN_SIZE);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * Gets the board
	 * @return board
	 */
	public Board getBoard() {
		return board;
	}

	/**
	 * Creates a new game
	 */
	public void newGame() {
		initBoard();
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				App app = new App();
				app.setVisible(true);
			}
		});
	}

	/** SCREEN AND BOARD INFORMATION */
	public static final int SCREEN_SIZE = 700;
	public static final int BOARD_RADIUS = 5;
	public static final int HEX_DIAMETER = (SCREEN_SIZE*2/3)/(BOARD_RADIUS + 2*(BOARD_RADIUS-1));
	public static final int PIECE_DIAMETER = HEX_DIAMETER*4/5;
	public static final double Y_OFFSET = ((double) HEX_DIAMETER/2)*(2-Math.sqrt(3));
	public static final double VISUAL_OFFSET = ((double) HEX_DIAMETER/2) * Math.sqrt(2)/9;

	/** GAME INFORMATION */
	public static final boolean MOVE_ASSISTANCE = true;
	public static final int NUM_HUMAN_PLAYERS = 1;
	public static final Color[] PLAYERS = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PINK, Color.GRAY};
	public static final String[] PLAYER_NAMES = {"Red", "Blue", "Green", "Yellow", "Pink", "Gray"};
}