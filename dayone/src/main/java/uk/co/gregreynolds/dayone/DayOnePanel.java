package uk.co.gregreynolds.dayone;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;


public class DayOnePanel extends JPanel implements ListSelectionListener
{
  private JSplitPane splitPane;
  private JList list;
  private JTextArea text;
  private List<Entry> entries;
  private JPanel contentPanel;
  private JLabel photoLabel = null;
  private UndoManager undoManager = new UndoManager();
  private JButton undoButton = new JButton("Undo");
  private JButton redoButton = new JButton("Redo");
  private JButton saveButton = new JButton("Save");
  private JButton newButton = new JButton("New");
  
  private UndoableEditListener undoListener = new UndoableEditListener() {

    public void undoableEditHappened(UndoableEditEvent e)
    {
      undoManager.addEdit(e.getEdit());
      updateButtons();

    }
  };

  public DayOnePanel(List<Entry> entries)
  {
    this.entries = entries;
    EntryDataModel model = new EntryDataModel();
    for (Entry entry : entries) {
      model.add(entry);
    }
    Collections.sort(model,Collections.reverseOrder());
    list = new JList(model);

    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setCellRenderer(new EntryCellRenderer(model));

    list.addListSelectionListener(this);

    JScrollPane listScrollPane = new JScrollPane(list);

    contentPanel = new JPanel(new BorderLayout());
    JPanel textPanel = new JPanel(new BorderLayout());
    text = new JTextArea();
    text.setWrapStyleWord(true);
    text.setLineWrap(true);
    JScrollPane textScrollPane = new JScrollPane(text);
    photoLabel = new JLabel();
    contentPanel.add(photoLabel,BorderLayout.NORTH);
    textPanel.add(textScrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    undoButton.setEnabled(false);
    redoButton.setEnabled(false);
    saveButton.setEnabled(true);
    newButton.setEnabled(true);
    buttonPanel.add(newButton);
    buttonPanel.add(saveButton);
    buttonPanel.add(undoButton);
    buttonPanel.add(redoButton);
    textPanel.add(buttonPanel,BorderLayout.NORTH);

    text.getDocument().addUndoableEditListener(undoListener);
    text.getDocument().addDocumentListener(new DocumentListener()
    {
      
      @Override
      public void removeUpdate(DocumentEvent e)
      {
        updateEntry();
      }


      private void updateEntry()
      {
        Entry entry = getCurrentEntry();
        String entryText = text.getText();
        entry.setEntryText(entryText);
      }
      
      
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        updateEntry();
      }
      
      
      @Override
      public void changedUpdate(DocumentEvent e)
      {
        updateEntry();
      }
    });
        
    undoButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          undoManager.undo();
        } catch (CannotRedoException cre) {
          cre.printStackTrace();
        }
        updateButtons();
      }
    });

    redoButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          undoManager.redo();
        } catch (CannotRedoException cre) {
          cre.printStackTrace();
        }
        updateButtons();
      }
    });
    
    saveButton.addActionListener(new ActionListener()
    {
      
      @Override
      public void actionPerformed(ActionEvent e)
      {
        Entry entry = getCurrentEntry();
        try
        {
          entry.save();
        }
        catch (IOException e1)
        {
          JOptionPane.showMessageDialog(contentPanel, 
              "Could not save entry.");
        }
        
      }

    });

    contentPanel.add(textPanel,BorderLayout.CENTER);

    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, contentPanel);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(250);

    //Provide minimum sizes for the two components in the split pane.
    Dimension minimumSize = new Dimension(150, 50);
    listScrollPane.setMinimumSize(minimumSize);
    textScrollPane.setMinimumSize(minimumSize);

    //Provide a preferred size for the split pane.
    splitPane.setPreferredSize(new Dimension(800, 600));    
    list.setSelectedIndex(0);
  }


  protected void updateButtons()
  {
    undoButton.setText(undoManager.getUndoPresentationName());
    redoButton.setText(undoManager.getRedoPresentationName());
    undoButton.setEnabled(undoManager.canUndo());
    redoButton.setEnabled(undoManager.canRedo());
  }


  public void valueChanged(ListSelectionEvent e)
  {
    JList list = (JList)e.getSource();
    EntryDataModel entries = (EntryDataModel)list.getModel();
    Entry entry = entries.get(list.getSelectedIndex());
    undoManager.end();
    text.getDocument().removeUndoableEditListener(undoListener);
    text.setText(entry.getEntryText());
    text.getDocument().addUndoableEditListener(undoListener);
    undoManager = new UndoManager();
    updateButtons();

    Image photo = null;
    try
    {
      photo = entry.getPhotoScaledToHeight(300);
    }
    catch (IOException e1)
    {
      // do nothing
    }
    
    if (photo == null)
    {
      photo = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
    }
    

    if (photo != null)
    {
      photoLabel.setIcon(new ImageIcon(photo));      
    }
    
  }




  public Component getSplitPane()
  {
    return splitPane;
  }


  public Component getInterfacePane()
  {
    return getSplitPane();
  }

  private Entry getCurrentEntry()
  {
    EntryDataModel entries = (EntryDataModel)list.getModel();
    Entry entry = entries.get(list.getSelectedIndex());
    return entry;
  }
}
