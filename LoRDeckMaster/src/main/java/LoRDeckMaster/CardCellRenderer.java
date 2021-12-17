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

import no.stelar7.api.r4j.pojo.lor.staticdata.StaticLoRCard;

/**
 *
 * @author hoski
 */
public class CardCellRenderer implements ListCellRenderer<StaticLoRCard> {
    
    private CardPanel cardPanel;
    
    public CardCellRenderer(DefaultListModel<StaticLoRCard> dlmCards) {
        cardPanel = new CardPanel();
    }
    
    public Component getListCellRendererComponent(JList<? extends StaticLoRCard> list, StaticLoRCard card, int index, boolean isSelected, boolean cellHasFocus) {
        cardPanel.setCard(card);
        if (isSelected) {
            cardPanel.setBackground(list.getSelectionBackground());
            cardPanel.setForeground(list.getSelectionForeground());
        } else {
            cardPanel.setBackground(list.getBackground());
            cardPanel.setForeground(list.getForeground());
        }
        return cardPanel;
    }
}
