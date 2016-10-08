import java.util.List;
import java.util.ArrayList;

public class Archer extends ChessPiece
{
  public Archer(char c)
  {
    super(c, 3);
  }
  
  public Location[] getLegalMoves(boolean check)
  {
        Location[] legalMovesArray = new Location[50];
        
        if(!ChessBoard.isTurn(getColorType()) && check)
        	return legalMovesArray;
        
        int i = 0;
        for(int d = 0; d < 360; d += 45)
        {
        	Location sPos = getLocation();
        	
            sPos = sPos.getAdjacentLocation(d);
            if(isLegal(check, sPos) && getGrid().get(sPos) == null)
            {
                legalMovesArray[i] = sPos;
                i++;
            }
            
            sPos = sPos.getAdjacentLocation(d);
            if(isLegal(check, sPos) && getGrid().get(sPos) != null)
            {
                legalMovesArray[i] = sPos;
                i++;
            }
        }
        
        return legalMovesArray;
    }
  
  public void copyTo(Location loc)
  {
    ChessBoard.add(loc, new Archer(getColorType()));
  }
}
