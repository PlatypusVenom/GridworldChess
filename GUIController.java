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
    private JButton playButton, resetButton;
    private JComponent controlPanel;
    private GridPanel display;
    private WorldFrame<T> parentFrame;
    private int numStepsToPlay, numStepsSoFar;
    private ResourceBundle resources;
    private DisplayMap displayMap;
    private boolean playing;
    private Set<Class> occupantClasses;

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
        reset();
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

    public void play()
    {
        playButton.setEnabled(false);
        ChessRunner.defaultSetup();
        playing = true;
    }

    /**
     * Stops any existing timer currently carrying out steps.
     */
    public void reset()
    {
        playButton.setEnabled(true);
        playing = false;
    }

    public boolean isPlaying()
    {
        return playing;
    }

    /**
     * Builds the panel with the various controls (buttons and
     * slider).
     */
    private void makeControls()
    {
        controlPanel = new JPanel();
        playButton = new JButton(resources.getString("button.gui.play"));
        resetButton = new JButton(resources.getString("button.gui.reset"));
        
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.setBorder(BorderFactory.createEtchedBorder());
        
        Dimension spacer = new Dimension(5, resetButton.getPreferredSize().height + 10);
        
        controlPanel.add(Box.createRigidArea(spacer));

        controlPanel.add(resetButton);
        controlPanel.add(Box.createRigidArea(spacer));
        controlPanel.add(playButton);
        playButton.setEnabled(true);
        resetButton.setEnabled(true);

        controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        playButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                play();
            }
        });
        resetButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                reset();
            }
        });
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
    private void locationClicked()
    {
        World<T> world = parentFrame.getWorld();
        Location loc = display.getCurrentLocation();
        if (loc != null && !world.locationClicked(loc))
            editLocation();
        parentFrame.repaint();
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
            if (loc0.isOnBoard())
                occupant0 = world.getGrid().get(loc0);
            Location[] locSelection = display.getCurrentSelection();
            
            
            if(playing)
            {
                if(occupant0 != null && loc0.isOnBoard())
                {
                    display.clearSelection();
                    ChessPiece C = (ChessPiece)occupant0;
                    if(locArrayContains(locSelection, loc)) // Piece -> legal move
                    {
                        if(occupant != null)
                            StorageArea.takePiece((ChessPiece)occupant);
                        C.moveTo(loc);
                        //display.setOneSelection(loc);
                        //ChessRunner.flipBoard();
                    }
                    else if(occupant != null)
                    {
                        ChessPiece C2 = (ChessPiece)occupant;
                        if(C.getColorType() == C2.getColorType() && loc.isOnBoard()) // Piece -> piece
                        	display.setSelection(C2.getLegalMoves());
                    }
                }
                
                else if(occupant != null) // Blank -> piece
                {
                    ChessPiece C = (ChessPiece)occupant;
                    display.setSelection(C.getLegalMoves());
                }
            }
            
            else
            {
                if(occupant0 != null)
                {
                    ChessPiece C = (ChessPiece)occupant0;
                    
                    if(loc0.isOnBoard())
                    {
                        if(occupant == null) // Piece -> blank
                        {
                            C.moveTo(loc);
                            display.clearSelection();
                        }
                        else
                        {
                            ChessPiece C2 = (ChessPiece)occupant;
                            if(loc.isOnBoard()) // Piece -> piece
                                display.setOneSelection(loc);
                            else // Piece -> out
                            {
                                C.removeSelfFromGrid();
                                display.clearSelection();
                            }
                        }
                    }
                    else
                    {
                        display.clearSelection();
                        if(occupant == null) // Out -> blank
                            C.copyTo(loc);
                        else // Out -> piece
                            display.setOneSelection(loc);
                    }
                }
                
                else if(occupant != null) // Blank -> piece or out
                    display.setOneSelection(loc);
            }
        }
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
}
