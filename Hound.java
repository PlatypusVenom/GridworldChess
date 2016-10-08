import java.util.List;
import java.util.ArrayList;

public class Hound extends ChessPiece
{
  	public Hound(char c)
  	{
  	  	super(c, 4);
  	}
  
  	public Location[] getLegalMoves(boolean check)
  	{
        Location[] legalMovesArray = new Location[100];
        
        if(!ChessBoard.isTurn(getColorType()) && check)
        	return legalMovesArray;
        
        return getStepMoves(100, 45, 90, check);
  	}
  
  	public void copyTo(Location loc)
  	{
    	ChessBoard.add(loc, new Hound(getColorType()));
  	}
}
