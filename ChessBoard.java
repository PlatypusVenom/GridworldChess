import java.util.ArrayList;

public class ChessBoard
{
	private static ActorWorld world;
	private static Grid grid;
	private static String currentGame;
	private static boolean isWhiteTurn = true;
	private static King blackKing = null, whiteKing = null;
	private static int turn;
	
	public ChessBoard(Grid g, ActorWorld w)
	{
		grid = g;
		world = w;
		currentGame = "";
		turn = 1;
	}
	
	public static World getWorld()
	{
		return world;
	}
	
	public static void newGame(String s)
	{
		reset();
		currentGame = s;
		switch(currentGame)
		{
			case "Standard":
				defaultSetup();
				break;
			case "Hunter Chess":
				hunterSetup();
				break;
		}
	}
	
	public static King getKing(char c)
	{
		if(c == 'w')
			return whiteKing;
		return blackKing;
	}
	
	public static int getTurn()
	{
		return turn;
	}
	
	public static void takeTurn()
	{
    	isWhiteTurn = !isWhiteTurn;
		flipBoard();
		if(isWhiteTurn)
			turn++;
	}
	
	public static boolean isTurn(char c)
	{
		if(isWhiteTurn && c == 'w')
			return true;
		else if(!isWhiteTurn && c == 'b')
			return true;
		return false;
	}
	
    //This method flips the board after every turn	
    public static void flipBoard()
    {
        for(int y = 0; y < 4; y++) //Loop through the top half of the board
    	    for(int x = 0; x < 8; x++)
    	    {
    		    ChessPiece C;
    		    Location loc0 = new Location(y, x);
    		    Location loc1 = new Location(7-y, 7-x);
    		    if(grid.get(loc0) != null)
  			    {
    			    C = (ChessPiece)(grid.get(loc0));
    			    C.swapTo(loc1);
    		    }
    		    else if(grid.get(loc1) != null)
    		    {
    			    C = (ChessPiece)(grid.get(loc1));
    			    C.swapTo(loc0);
  			    }
    	    } 
    }
    
	public static void customSetup()
	{
		clearAll();
		char[] colors = new char[2];
		colors[0] = 'b';
		colors[1] = 'w';
		whiteKing = new King('w');
		blackKing = new King('b');
		
		for(int i = 0; i < 2; i++)
		{
    		world.add(new Location(0, 8 + 2*i), getKing(colors[i]));
    		world.add(new Location(1, 8 + 2*i), new Queen(colors[i]));
    		world.add(new Location(2, 8 + 2*i), new Rook(colors[i]));
    		world.add(new Location(3, 8 + 2*i), new Bishop(colors[i]));
    		world.add(new Location(4, 8 + 2*i), new Knight(colors[i]));
			world.add(new Location(5, 8 + 2*i), new Pawn(colors[i]));
		}
	}
        //This is a default chess game
	public static void defaultSetup()
	{
		clearAll();
		char[] colors = new char[2];
		colors[0] = 'b';
		colors[1] = 'w';
		whiteKing = new King('w');
		blackKing = new King('b');
		
		for(int i = 0; i < 2; i++)
		{
			for(int j = 0; j < 8; j++)
				world.add(new Location(6 - i*5, j), new Pawn(colors[1-i]));
    		
    		world.add(new Location(i*7, 0), new Rook(colors[i]));
    		world.add(new Location(i*7, 1), new Knight(colors[i]));
    		world.add(new Location(i*7, 2), new Bishop(colors[i]));
    		world.add(new Location(i*7, 3), getKing(colors[i]));
    		world.add(new Location(i*7, 4), new Queen(colors[i]));
    		world.add(new Location(i*7, 5), new Bishop(colors[i]));
    		world.add(new Location(i*7, 6), new Knight(colors[i]));
    		world.add(new Location(i*7, 7), new Rook(colors[i]));
		}
	}
	
	public static void hunterSetup()
	{
		clearAll();
		char[] colors = new char[2];
		colors[0] = 'b';
		colors[1] = 'w';
		whiteKing = new King('w');
		blackKing = new King('b');
		
		for(int i = 0; i < 2; i++)
		{
			for(int j = 0; j < 8; j++)
				world.add(new Location(6 - i*5, j), new Pawn(colors[1-i]));
    		
    		world.add(new Location(i*7, 0), new Rook(colors[i]));
    		world.add(new Location(i*7, 1), new Hound(colors[i]));
    		world.add(new Location(i*7, 2), new Archer(colors[i]));
    		world.add(new Location(i*7, 3), getKing(colors[i]));
    		world.add(new Location(i*7, 4), new Queen(colors[i]));
    		world.add(new Location(i*7, 5), new Archer(colors[i]));
    		world.add(new Location(i*7, 6), new Hound(colors[i]));
    		world.add(new Location(i*7, 7), new Rook(colors[i]));
		}
	}
    
    //This method returns the locations of all the pieces of color c
    public static ArrayList<Location> getLocations(char c)
    {
    	ArrayList<Location> Locs = grid.getOccupiedLocations();

    	for(int i = 0; i < Locs.size(); i++)
    	{
      	  	ChessPiece C = (ChessPiece)(grid.get(Locs.get(i)));
      	  	if(C.getColorType() != c || !Locs.get(i).isOnBoard())
      	  	{
	    		Locs.remove(i);
	    		i--;
      	  	}
		}
		return Locs;
    }
	
	public static String getEndMessage(ChessPiece C)
	{
		if(checkmate(C))
		{
			if(C.getColorType() == 'w')
				return "White Wins!";
			return "Black Wins!";
		}
		
		if(stalemate(C))
			return "Draw by stalemate!";
		
		if(insufficientMat(C))
			return "Draw by insufficient material.";
			
		return null;
	}
	
	public static boolean stalemate(ChessPiece C)
	{
		ArrayList<Location> enemyLocs;
		enemyLocs = getLocations('w');
		if(C.getColorType() == 'w')
			enemyLocs = getLocations('b');
		
		for(Location enemyLoc : enemyLocs)
		{
			ChessPiece enemy = (ChessPiece)(grid.get(enemyLoc));
			if(enemy.getKing().isInCheck())
				return false;
					
			for(Location loc : enemy.getLegalMoves(true))
				if(loc != null)
					return false;
		}
		return true;
	}
	
	public static boolean insufficientMat(ChessPiece C)
	{
		boolean wInsufficient = true, bInsufficient = true;
		if(getLocations('w').size() > 2)
		    wInsufficient = false;
		if(getLocations('b').size() > 2)
		    bInsufficient = false;
		for(Location wLoc : getLocations('w'))
		{
		    ChessPiece wPiece = (ChessPiece)(grid.get(wLoc));
		    if(!(wPiece instanceof Bishop || wPiece instanceof King || wPiece instanceof Knight))
		    {
		        wInsufficient = false;
		        break;
		    }
		}
		for(Location bLoc : getLocations('b'))
		{
		    ChessPiece bPiece = (ChessPiece)(grid.get(bLoc));
		    if(!(bPiece instanceof Bishop || bPiece instanceof King || bPiece instanceof Knight))
		    {
		        bInsufficient = false;
		        break;
		    }
		}
		return wInsufficient && bInsufficient;

	}
	
	public static boolean checkmate(ChessPiece C)
	{
		ArrayList<Location> enemyLocs;
		enemyLocs = getLocations('w');
		if(C.getColorType() == 'w')
			enemyLocs = getLocations('b');
		
		for(Location enemyLoc : enemyLocs)
		{
			ChessPiece enemy = (ChessPiece)(grid.get(enemyLoc));
			if(!enemy.getKing().isInCheck())
				return false;
					
			for(Location loc : enemy.getLegalMoves(true))
				if(loc != null)
					return false;
		}
		return true;
	}
	
	public static boolean check(ChessPiece C)
	{
		ArrayList<Location> enemyLocs;
		char targetColor = 'w';
		if(C.getColorType() == 'w')
			targetColor = 'b';
		
		return getKing(targetColor).isInCheck();
	}
	
	public static void reset()
	{
		WorldFrame frame = (WorldFrame)(world.getFrame());
		
		frame.setHistMessage("");
		frame.getDisplay().clearSelection();
		turn = 1;
		clearAll();
		isWhiteTurn = true;
	}
	
	public static void clearAll()
	{
		for(int y = 0; y < 8; y++)
			for(int x = 0; x < 12; x++)
				world.remove(new Location(y, x));
	}
	
	public static void remove(Location loc)
	{
		world.remove(loc);
	}
	
	public static void add(Location loc, ChessPiece occupant)
	{
		world.add(loc, occupant);
	}
}
