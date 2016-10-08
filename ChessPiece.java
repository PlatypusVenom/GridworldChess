import java.util.ArrayList;
import java.lang.Comparable;
import java.awt.Color;

public abstract class ChessPiece extends Actor implements Comparable
{
    private char colorType;
    private int value;

    public ChessPiece(char c)
    {
        colorType = c;
        value = 0;
    }

    public ChessPiece(char c, int v)
    {
        colorType = c;
        value = v;
    }
	
    public char getColorType()
    {
        return colorType;
    }
    
    public Location[] getLinearMoves(int len, int start, int step, boolean check)
    {
    	Location[] locArr = new Location[len];
    	int i = 0;
        for(int d = start; d < 360; d += step)
        {
            Location loc = getLocation();
            while((loc != null && loc.isOnBoard()) || loc.equals(getLocation()))
            {
                if(!loc.equals(getLocation()));
                {
                    ChessPiece C = (ChessPiece)(getGrid().get(loc));

                    if(C != null && !loc.equals(getLocation()))
                    {
                      	if(isLegal(check, loc))
                        	locArr[i] = loc;
                      	break;
                    }
                    else if(isLegal(check, loc))
                      locArr[i] = loc;
                    i++;
                }
                loc = loc.getAdjacentLocation(d);
            }
        }
        return locArr;
    }
    
    public Location[] getStepMoves(int len, int start, int step, boolean check)
    {
    	Location[] locArr = new Location[len];
    	int i = 0;
        for(int d = start; d < 360; d += step)
        {
            Location loc = getLocation();
            loc = loc.getAdjacentLocation(d);
            if(loc == null || !loc.isOnBoard())
            	continue;
            ChessPiece C = (ChessPiece)(getGrid().get(loc));

            if(isLegal(check, loc))
            {
	            locArr[i] = loc;
	        	i++;
	        	if(C == null)
	        	{
	        	    for(int k = start; k < 360; k += step)
			        {
			            Location loc0 = loc;
			            loc0 = loc0.getAdjacentLocation(k);
			            if(isLegal(check, loc0))
			            {
				            locArr[i] = loc0;
				        	i++;
			            }
			        }
	        	}
            }
        }
        return locArr;
        
    }
    
  //This method returns any passed pawns next to the current pawn
  public Pawn getPassedPawn()
  {
  	ChessPiece passedEnP = null;
    for(int i = 0; i < 2; i++)
      if(getLocation().getAdjacentLocation(90 + 180*i).isOnBoard())
      {
        passedEnP = (ChessPiece)(getGrid().get(getLocation().getAdjacentLocation(90 + 180*i)));
        if(passedEnP instanceof Pawn && passedEnP.getColorType() != getColorType() && ((Pawn)(passedEnP)).isPassed())
          break;
        else
        	passedEnP = null;
      }
    return (Pawn)passedEnP;
  }
  
  	public String getNotation(Location loc)
  	{
    	String s = "", key = "abcdefgh";
		if(this instanceof Bishop)
		    s = "B";
		else if(this instanceof King)
		    s = "K";
		else if(this instanceof Queen)
		    s = "Q";
		else if(this instanceof Rook)
		    s = "R";
		else if(this instanceof Knight)
		    s = "N";

		ChessPiece other = null;
		if(getGrid().get(loc) != null)
			other = (ChessPiece)(getGrid().get(loc));
		int col;
		if(ChessBoard.isTurn('w'))
		{	
			
			if(other != null || getPassedPawn() != null)
			{
				if(this instanceof Pawn)
				{
					col = getLocation().getCol();
					s += key.substring(col, col+1);
				}
		        s += 'x';
			}
		    s = ChessBoard.getTurn() + ". " + s;
			if(ChessBoard.getTurn() != 1)
		    	s = "\n " + s;
		    col = loc.getCol();
		    if(col == 7)
		        s += 'h';
		    else
		        s += key.substring(col, col+1);
		    s += 8 - loc.getRow() + "";
		}
		else
		{
			String reverseKey = new StringBuilder(key).reverse().toString();
			if(other != null || getPassedPawn() != null)
			{
				if(this instanceof Pawn)
				{
					col = getLocation().getCol();
					s += reverseKey.substring(col, col+1);	
				}
				
		        s += 'x';
			}
		    col = loc.getCol();
		    if(col == 7)
		        s += 'a';
		   	else
		       	s += reverseKey.substring(col, col+1);	
		    s += 1 + loc.getRow() + "";
		    
		}
		
		if(getPassedPawn() != null)
			s += "e.p.";
		
		return s;
  	}
  
    public void moveTo(Location loc)
    {
		WorldFrame frame = (WorldFrame)(ChessBoard.getWorld().getFrame());
		frame.addHistMessage(getNotation(loc));
		
    	if(getPassedPawn() != null && getPassedPawn().getColorType() != getColorType())
    		StorageArea.takePiece(getPassedPawn());
    		
    	ArrayList<Location> enemyLocs;
    	if(getColorType() == 'w')
    		enemyLocs = ChessBoard.getLocations('b');
    	else
    		enemyLocs = ChessBoard.getLocations('w');
    	
    	for(Location L : enemyLocs)
    		if(getGrid().get(L) instanceof Pawn)
    		{
    			Pawn pawn = (Pawn)(getGrid().get(L));
    			pawn.hasPassed();
    		}
    		
    	ChessPiece occupant = (ChessPiece)(getGrid().get(loc));
        if(occupant != null)
            StorageArea.takePiece((ChessPiece)occupant);
    		
    	super.moveTo(loc);
    }
    
    public void swapTo(Location loc)
    {
    	if(loc == getLocation())
    		return;
    	
    	if(getGrid().get(loc) != null)
    	{
    		ChessPiece C = (ChessPiece)(getGrid().get(loc));
    		//if(C.compareTo(this) == 0)
    		//	return;
    		
    		Location loc0 = getLocation();
    		super.moveTo(C.getLocation());
    		C.putSelfInGrid(getGrid(), loc0);
    	}
    	else
    		super.moveTo(loc);
    }
    
    //This method returns the king of the same color as ChessPiece
    public King getKing()
    {
    	for (Location loc : ChessBoard.getLocations(getColorType()))
    	{
    		ChessPiece C = (ChessPiece)(getGrid().get(loc));
    		if (C instanceof King)
    			return (King)C;
    	}
    	return null;
    }
    
    //Check to see if the move at Location loc is legal. 
    //The boolean check serves as a way to end the infinite loop: isInCheck() -> getLegalMoves() -> isLegal() -> isInCheck()...
    public boolean isLegal(boolean check, Location loc)
    {
    	if(loc == null || !getGrid().isValid(loc))
    		return false;
    		
    	if(!loc.isOnBoard())
    		return false;
    		
    	if(loc.equals(getLocation()) || !getLocation().isOnBoard())
    		return false;

    	ChessPiece C = (ChessPiece)getGrid().get(loc);
    	King king = getKing();
    	Location prevLoc = getLocation();
    	
    	if(C != null && C.getColorType() == colorType)
    		return false;
    	
    	if(check)
    	{
	    	ChessPiece other = tryMove(loc);
	    	if(king.isInCheck())
	    	{
	    		super.moveTo(prevLoc);
	    		if(other != null)
	    			ChessBoard.add(loc, other);
	    		return false;
	    	}
	    	super.moveTo(prevLoc);
	    	if(other != null)
	    		ChessBoard.add(loc, other);
    	}
    		
    	return true;
    }
    
    public ChessPiece tryMove(Location loc)
    {
    	ChessPiece other = (ChessPiece)(getGrid().get(loc));
    	
    	super.moveTo(loc);
    	return other;
    }
    
    public int getValue()
    {
    	return value;
    }
    
    public int compareTo(Object obj)
    {
    	ChessPiece C = (ChessPiece)obj;
    	
    	if(this instanceof Knight && obj instanceof Bishop)
    		return 1;
    	else if(obj instanceof Knight && this instanceof Bishop)
    		return -1;
    	else
    		return getValue() - C.getValue();
    }
    
    public Location[] getAttackingMoves(boolean check)
    {
    	return getLegalMoves(check);
    }
    
    public String toString()
    {
        return getClass().getName() + "[Location: " + getLocation() + ", colorType: " + getColorType()
        							+ ", value: " + getValue() + "]";
    }

    public abstract Location[] getLegalMoves(boolean check);
    public abstract void copyTo(Location loc);
}
