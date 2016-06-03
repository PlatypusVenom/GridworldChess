/* 
 * AP(r) Computer Science GridWorld Case Study:
 * Copyright(c) 2002-2006 College Entrance Examination Board 
 * (http://www.collegeboard.com).
 *
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * @author Julie Zelenski
 * @author Cay Horstmann
 */

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.*;

/**
 * The GUIController controls the behavior in a WorldFrame. <br />
 * This code is not tested on the AP CS A and AB exams. It contains GUI
 * implementation details that are not intended to be understood by AP CS
 * students.
 */

public class GUIController<T>
{
    public static final int INDEFINITE = 0, FIXED_STEPS = 1, PROMPT_STEPS = 2;

    private static final int MIN_DELAY_MSECS = 10, MAX_DELAY_MSECS = 1000;
    private static final int INITIAL_DELAY = MIN_DELAY_MSECS
            + (MAX_DELAY_MSECS - MIN_DELAY_MSECS) / 2;

    private Timer timer;
    private JComponent controlPanel;
    private GridPanel display;
    private WorldFrame<T> parentFrame;
    private int numStepsToPlay, numStepsSoFar;
    private ResourceBundle resources;
    private DisplayMap displayMap;
    private boolean menuOpen;
    private Set<Class> occupantClasses;
    private ChessPiece other = null;
    private Location prevLoc0;

    /**
     * Creates a new controller tied to the specified display and gui
     * frame.
     * @param parent the frame for the world window
     * @param disp the panel that displays the grid
     * @param displayMap the map for occupant displays
     * @param res the resource bundle for message display
     */
    public GUIController(WorldFrame<T> parent, GridPanel disp,
            DisplayMap displayMap, ResourceBundle res)
    {
    	menuOpen = false;
        resources = res;
        display = disp;
        parentFrame = parent;
        this.displayMap = displayMap;
        makeControls();

        occupantClasses = new TreeSet<Class>(new Comparator<Class>()
        {
            public int compare(Class a, Class b)
            {
                return a.getName().compareTo(b.getName());
            }
        });

        World<T> world = parentFrame.getWorld();
        Grid<T> gr = world.getGrid();
        for (Location loc : gr.getOccupiedLocations())
            addOccupant(gr.get(loc));
        for (String name : world.getOccupantClasses())
            try
            {
                occupantClasses.add(Class.forName(name));
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

        display.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent evt)
            {
                Grid<T> gr = parentFrame.getWorld().getGrid();
                Location loc = display.locationForPoint(evt.getPoint());
                if (loc != null && gr.isValid(loc))
                {
                    display.setCurrentLocation(loc);
                    locationClicked();
                }
            }
        });
    }
    
    public GridPanel getDisplay()
    {
    	return display;
    }

    private void addOccupant(T occupant)
    {
        Class cl = occupant.getClass();
        do
        {
            if ((cl.getModifiers() & Modifier.ABSTRACT) == 0)
                occupantClasses.add(cl);
            cl = cl.getSuperclass();
        }
        while (cl != Object.class);
    }

    /**
     * Builds the panel with the various controls (buttons and
     * slider).
     */
    private void makeControls()
    {
        controlPanel = new JPanel();
        //sampleButton = new JButton("");
        
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.setBorder(BorderFactory.createEtchedBorder());
        /*
        Dimension spacer = new Dimension(5, sampleButton.getPreferredSize().height + 10);
        
        controlPanel.add(Box.createRigidArea(spacer));

        controlPanel.add(sampleButton);
        controlPanel.add(Box.createRigidArea(spacer));
        
        sampleButton.setEnabled(true);

        controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        sampleButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                sampleMethod();
            }
        });*/
    }

    /**
     * Returns the panel containing the controls.
     * @return the control panel
     */
    public JComponent controlPanel()
    {
        return controlPanel;
    }

    /**
     * Callback on mousePressed when editing a grid.
     */
    public void locationClicked()
    {
        World<T> world = parentFrame.getWorld();
        Location loc = display.getCurrentLocation();
        if (loc != null && !world.locationClicked(loc))
            editLocation();
        parentFrame.repaint();
    }
    
    public void setMenuOpen(boolean b)
    {
    	menuOpen = b;
    }

    /**
     * Edits the contents of the current location, by displaying the constructor
     * or method menu.
     */
    public void editLocation()
    {
        World<T> world = parentFrame.getWorld();
        
        Location loc = display.getCurrentLocation();
        Location loc0 = display.getPreviousLocation();
        if (loc.isOnBoard())
        {
            T occupant = world.getGrid().get(loc);
            T occupant0 = null;
            if (loc0 != null && loc0.isOnBoard())
                occupant0 = world.getGrid().get(loc0);
            Location[] locSelection = display.getCurrentSelection();
            
        
         	// Mouse event handling
            if(occupant0 != null && loc0.isOnBoard())
            {
                display.clearSelection();
                ChessPiece C = (ChessPiece)occupant0;
                
                if(locArrayContains(locSelection, loc)) // Piece -> legal move
                {                        
                    if(C instanceof Pawn && loc.getRow() == 0)
                    {
                    	setMenuOpen(true);
						MenuMaker<T> maker = new MenuMaker<T>(this, parentFrame, resources, displayMap);
            			JPopupMenu popup = maker.makePromoteMenu(occupant0, loc);
            			Point p = display.pointForLocation(loc);
            			popup.show(display, p.x, p.y);
                    }
                    else
                    {
                        C.moveTo(loc);
                        ChessBoard.takeTurn();
                        checkStatus(C);
                        display.repaint();
                    }
                }
                else if(occupant != null)
                {
                    ChessPiece C2 = (ChessPiece)occupant;
                    if(loc.isOnBoard()) // Piece -> piece
                    	display.setSelection(C2.getLegalMoves(true));
                }
            }
            
            else if(occupant != null) // Blank -> piece
            {
                ChessPiece C = (ChessPiece)occupant;
                display.setSelection(C.getLegalMoves(true));
            }
        }
        prevLoc0 = loc0;
        parentFrame.repaint();
    }

    /**
     * Edits the contents of the current location, by displaying the constructor
     * or method menu.
     */
    public void deleteLocation()
    {
        World<T> world = parentFrame.getWorld();
        Location loc = display.getCurrentLocation();
        if (loc != null)
        {
            world.remove(loc);
            parentFrame.repaint();
        }
    }
    
    public boolean locArrayContains(Location[] locs, Location loc)
    {
        for(Location L : locs)
        		if(L != null && loc != null)
            		if(L.equals(loc))
                		return true;
        return false;
    }
    
    public void checkStatus(ChessPiece C)
    {
    	if(ChessBoard.getEndMessage(C) != null)
        {
        	System.out.println(ChessBoard.getEndMessage(C));
        	if(ChessBoard.getEndMessage(C).endsWith("Wins!"))
        	{
        		String s = parentFrame.getHistMessage();
        		parentFrame.setHistMessage(s + "#");
        	}
        	display.createDialogue(ChessBoard.getEndMessage(C));
        }
        else if(ChessBoard.check(C))
        {
        	String s = parentFrame.getHistMessage();
        	parentFrame.setHistMessage(s + "+");
        	display.createDialogue("Check!");
        }
    }
}
