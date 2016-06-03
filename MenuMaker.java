/* 
 * AP(r) Computer Science GridWorld Case Study:
 * Copyright(c) 2005-2006 Cay S. Horstmann (http://horstmann.com)
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
 * @author Cay Horstmann
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Makes the menus for constructing new occupants and grids, and for invoking
 * methods on existing occupants. <br />
 * This code is not tested on the AP CS A and AB exams. It contains GUI
 * implementation details that are not intended to be understood by AP CS
 * students.
 */
public class MenuMaker<T>
{
    private T occupant;
    private Grid currentGrid;
    private Location currentLocation;
    private WorldFrame<T> parent;
    private DisplayMap displayMap;
    private GUIController master;
    private ResourceBundle resources;
    
    /**
     * Constructs a menu maker for a given world.
     * @param parent the frame in which the world is displayed
     * @param resources the resource bundle
     * @param displayMap the display map
     */
    public MenuMaker(WorldFrame<T> parent, ResourceBundle resources,
            DisplayMap displayMap)
    {
        this.parent = parent;
        this.resources = resources;
        this.displayMap = displayMap;
    }
    
    public MenuMaker(GUIController<T> master, WorldFrame<T> parent, ResourceBundle resources,
            DisplayMap displayMap)
    {
    	this.master = master;
        this.parent = parent;
        this.resources = resources;
        this.displayMap = displayMap;
    }
    
    public JPopupMenu makePromoteMenu(T occupant, Location loc)
    {
        this.occupant = occupant;
        this.currentLocation = loc;
        JPopupMenu menu = new JPopupMenu();
        
        menu.add(new PromoteItem("Queen", (Icon)(new ImageIcon("images/Queen.png"))));
        menu.add(new PromoteItem("Rook", (Icon)(new ImageIcon("images/Rook.png"))));
        menu.add(new PromoteItem("Bishop", (Icon)(new ImageIcon("images/Bishop.png"))));
        menu.add(new PromoteItem("Knight", (Icon)(new ImageIcon("images/Knight.png"))));

        return menu;
    }
    
    public void addNewGames(JMenu menu)
    {
        menu.add(new NGameItem("Standard"));
    }

    private class PromoteItem extends JMenuItem implements ActionListener
    {
        public PromoteItem(String S, Icon I)
        {
        	super(S, I);
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent event)
        {
        	try
        	{
        		ChessPiece cp = (ChessPiece)occupant;
        		Location prevLoc = master.getDisplay().getPreviousLocation();
        		
        		Class[] ClsList = new Class[1];
        		ClsList[0] = Character.TYPE;
        		
        		Class Cls = Class.forName(event.getActionCommand());
        		Constructor Con = Cls.getConstructor(ClsList);
        		Object obj = Con.newInstance(cp.getColorType());
        		ChessPiece C = (ChessPiece)obj;
        		
        		String letter = event.getActionCommand().substring(0, 1);
        		String pawnNote = cp.getNotation(currentLocation);
        		if(letter.equals("K"))
        			letter = "N";
        		String notation = pawnNote + letter;
        		
        		cp.removeSelfFromGrid();
        		ChessBoard.add(currentLocation, C);
        		
        		ChessBoard.takeTurn();
        		master.setMenuOpen(false);
        		master.getDisplay().setCurrentLocation(null);
				parent.addHistMessage(notation);
        		parent.repaint();
        		master.checkStatus(C);
        	}
        	catch(ClassNotFoundException e)
        	{System.out.println("ClassNotFoundException: "+e.getMessage());}
        	catch(InstantiationException e)
        	{System.out.println("InstantiationException: "+e.getMessage());}
        	catch(IllegalAccessException e)
        	{System.out.println("IllegalAccessException: "+e.getMessage());}
        	catch(NoSuchMethodException e)
        	{System.out.println("NoSuchMethodException: "+e.getMessage());}
        	catch(InvocationTargetException e)
        	{System.out.println("InvocationTargetException: "+e.getMessage());}
        }
        	
    }
    
    private class NGameItem extends JMenuItem implements ActionListener
    {
        public NGameItem(String S)
        {
        	super(S);
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent event)
        {
        	ChessBoard.newGame(event.getActionCommand());
        	parent.repaint();
        }
        	
    }
}

class PropertySheet extends JPanel
{
    /**
     * Constructs a property sheet that shows the editable properties of a given
     * object.
     * @param object the object whose properties are being edited
     */
    public PropertySheet(Class[] types, Object[] values)
    {
        this.values = values;
        editors = new PropertyEditor[types.length];
        setLayout(new FormLayout());
        for (int i = 0; i < values.length; i++)
        {
            JLabel label = new JLabel(types[i].getName());
            add(label);
            if (Grid.class.isAssignableFrom(types[i]))
            {
                label.setEnabled(false);
                add(new JPanel());
            }
            else
            {
                editors[i] = getEditor(types[i]);
                if (editors[i] != null)
                {
                    editors[i].setValue(values[i]);
                    add(getEditorComponent(editors[i]));
                }
                else
                    add(new JLabel("?"));
            }
        }
    }

    /**
     * Gets the property editor for a given property, and wires it so that it
     * updates the given object.
     * @param bean the object whose properties are being edited
     * @param descriptor the descriptor of the property to be edited
     * @return a property editor that edits the property with the given
     * descriptor and updates the given object
     */
    public PropertyEditor getEditor(Class type)
    {
        PropertyEditor editor;
        editor = defaultEditors.get(type);
        if (editor != null)
            return editor;
        editor = PropertyEditorManager.findEditor(type);
        return editor;
    }

    /**
     * Wraps a property editor into a component.
     * @param editor the editor to wrap
     * @return a button (if there is a custom editor), combo box (if the editor
     * has tags), or text field (otherwise)
     */
    public Component getEditorComponent(final PropertyEditor editor)
    {
        String[] tags = editor.getTags();
        String text = editor.getAsText();
        if (editor.supportsCustomEditor())
        {
            return editor.getCustomEditor();
        }
        else if (tags != null)
        {
            // make a combo box that shows all tags
            final JComboBox comboBox = new JComboBox(tags);
            comboBox.setSelectedItem(text);
            comboBox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent event)
                {
                    if (event.getStateChange() == ItemEvent.SELECTED)
                        editor.setAsText((String) comboBox.getSelectedItem());
                }
            });
            return comboBox;
        }
        else
        {
            final JTextField textField = new JTextField(text, 10);
            textField.getDocument().addDocumentListener(new DocumentListener()
            {
                public void insertUpdate(DocumentEvent e)
                {
                    try
                    {
                        editor.setAsText(textField.getText());
                    }
                    catch (IllegalArgumentException exception)
                    {
                    }
                }

                public void removeUpdate(DocumentEvent e)
                {
                    try
                    {
                        editor.setAsText(textField.getText());
                    }
                    catch (IllegalArgumentException exception)
                    {
                    }
                }

                public void changedUpdate(DocumentEvent e)
                {
                }
            });
            return textField;
        }
    }

    public Object[] getValues()
    {
        for (int i = 0; i < editors.length; i++)
            if (editors[i] != null)
                values[i] = editors[i].getValue();
        return values;
    }

    private PropertyEditor[] editors;
    private Object[] values;

    private static Map<Class, PropertyEditor> defaultEditors;

    // workaround for Web Start bug
    public static class StringEditor extends PropertyEditorSupport
    {
        public String getAsText()
        {
            return (String) getValue();
        }

        public void setAsText(String s)
        {
            setValue(s);
        }
    }

    static
    {
        defaultEditors = new HashMap<Class, PropertyEditor>();
        defaultEditors.put(String.class, new StringEditor());
        defaultEditors.put(Location.class, new LocationEditor());
        defaultEditors.put(Color.class, new ColorEditor());
    }
}
