/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LoRDeckMaster;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;

/**
 *
 * @author hoski
 */
public class DeckCellRenderer implements ListCellRenderer<Deck> {
    
    private DeckPanel deckPanel;
    
    public DeckCellRenderer(DefaultListModel<Deck> dlmDecks) {
        deckPanel = new DeckPanel();
    }
    
    public Component getListCellRendererComponent(JList<? extends Deck> list, Deck deck, int index, boolean isSelected, boolean cellHasFocus) {
        deckPanel.setDeck(deck);
        if (isSelected) {
            deckPanel.setBackground(list.getSelectionBackground());
            deckPanel.setForeground(list.getSelectionForeground());
        } else {
            deckPanel.setBackground(list.getBackground());
            deckPanel.setForeground(list.getForeground());
        }
        return deckPanel;
    }
}
