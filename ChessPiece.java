public abstract class ChessPiece extends Actor
{
    private char colorType;

    public ChessPiece(char c)
    {
        colorType = c;
    }
	
    public char getColorType()
    {
        return colorType;
    }
    
    public boolean isLegal(Location loc)
    {
    	if(loc == null || !getGrid().isValid(loc))
    		return false;
    		
    	if(loc.getCol() >= 8)
    		return false;

    	ChessPiece C = (ChessPiece)getGrid().get(loc);
    		
    	if(C != null && C.getColorType() == colorType)
    		return false;
    		
    	return true;
    }
    public ArrayList<Location> getLocations(char c)
    {
    	ArrayList<Location> Locs = getOccupiedLocations();
    	for(Location l : Locs)
    	{
      	  ChessPiece C = (ChessPiece)(getGrid().get(l));
      	  if(C.getColorType() != c)
	    Locs.remove(l);
	}
	return Locs;
    }

    public abstract Location[] getLegalMoves();
}
