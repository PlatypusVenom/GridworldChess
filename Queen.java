public class Queen extends ChessPiece
{
  public Queen(char c)
  {
  	super(c, 9);
  }
  
  public Location[] getLegalMoves()
  {
        //List<Location> legalMoves = new ArrayList<Location>(0);
        Location[] legalMovesArray = new Location[27];
        int i = 0;
        for(int d = 0; d < 360; d += 45)
        {
            Location sPos = getLocation();
            while(isLegal(sPos) || sPos.equals(getLocation()))
            {
                if(isLegal(sPos))
                {
                    legalMovesArray[i] = sPos;
                    i++;
                    if(getGrid().get(sPos) != null)
                				break;
                }
                sPos = sPos.getAdjacentLocation(d);
            }
        }
    
        /*
         * This converts ArrayLists into Arrays so that
         * we can have arrays that will always be
         * the right size.
         */
        //Location[] legalMovesArray = new Location[legalMoves.size()];
        //legalMovesArray = legalMoves.toArray(legalMovesArray);
        return legalMovesArray;
  }
  
  public void copyTo(Location loc)
  {
    ChessBoard.add(loc, new Bishop(getColorType()));
  }
}
