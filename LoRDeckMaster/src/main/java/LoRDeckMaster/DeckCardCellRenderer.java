/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LoRDeckMaster;

import java.awt.Component;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author hoski
 */
public class DeckCardCellRenderer implements ListCellRenderer<DeckCard> {
    
    private DeckCardPanel deckCardPanel;
    
    public DeckCardCellRenderer(DefaultListModel<DeckCard> dlmDeckCards) {
        deckCardPanel = new DeckCardPanel();
    }
    
    public Component getListCellRendererComponent(JList<? extends DeckCard> list, DeckCard deckCard, int index, boolean isSelected, boolean cellHasFocus) {
        deckCardPanel.setDeckCard(deckCard);
        if (isSelected) {
            deckCardPanel.setBackground(list.getSelectionBackground());
            deckCardPanel.setForeground(list.getSelectionForeground());
        } else {
            deckCardPanel.setBackground(list.getBackground());
            deckCardPanel.setForeground(list.getForeground());
        }
        return deckCardPanel;
    }
}
