/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LoRDeckMaster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import no.stelar7.api.r4j.impl.lor.LoRStaticAPI;
import no.stelar7.api.r4j.pojo.lor.offline.card.LoRCard;
import no.stelar7.api.r4j.pojo.lor.staticdata.StaticLoRCard;

/**
 *
 * @author hoski
 */
public class DeckEditorWindow extends javax.swing.JFrame {
    
    DeckManagerWindow deckManagerWindow;
    Map<String, StaticLoRCard> cards = new HashMap<String, StaticLoRCard>();
    DefaultListModel<StaticLoRCard> dlmCards = new DefaultListModel<StaticLoRCard>();
    DefaultListModel<StaticLoRCard> dlmCardsFiltered = new DefaultListModel<StaticLoRCard>();
    DefaultListModel<DeckCard> dlmDeckCards = new DefaultListModel<DeckCard>();
    Deck currentDeck;
    boolean hasBeenFiltered = false;
    
    /**
     * Creates new form DeckEditorWindow
     */
    public DeckEditorWindow(DeckManagerWindow deckManagerWindow) {
        initComponents();
        this.deckManagerWindow = deckManagerWindow;
        
        // Get every set of cards to add to the list of cards.
        int i = 1;
        while (true) {
            LoRStaticAPI set = new LoRStaticAPI("set" + String.valueOf(i));
            if (set.getCards() == null)
                break;
            
            // If the set exists, add all cards from it into the hashmap of cards 
            // and the list of cards in ascending order by mana cost and name.
            List<StaticLoRCard> cardsInSet = set.getCards();
            for (int j = 0; j < cardsInSet.size(); j++) {
                StaticLoRCard card = cardsInSet.get(j);
                if (card.isCollectible()) {
                    cards.put(card.getCardCode(), card);
                    insertInOrder(dlmCards, card);
                }
            }
            i++;
        }
        
        jlstCards.setCellRenderer(new CardCellRenderer(dlmCards));
        jlstDeckCards.setCellRenderer(new DeckCardCellRenderer(dlmDeckCards));
    }

    public void setCurrentDeck(Deck currentDeck) {
        this.currentDeck = currentDeck;
        jtfDeckName.setText(currentDeck.getName());
        
        dlmDeckCards.clear();
        // Add the cards from the deck into deck cards list.
        for (Map.Entry<LoRCard, Integer> card : currentDeck.getDeck().getDeck().entrySet()) {
            DeckCard deckCard = new DeckCard(cards.get(card.getKey().getCardCode()), card.getValue());
            insertInOrder(dlmDeckCards, deckCard, false);
        }
        
        // If the deck has two regions and has not been filtered yet,
        // automatically filter cards to all in the two current regions.
        String region1 = currentDeck.getRegion1();
        String region2 = currentDeck.getRegion2();
        if (!("".equals(region1) || "".equals(region2)) && !hasBeenFiltered) {
            boolean inclBC = "BC".equals(region1) || "BC".equals(region2);
            boolean inclBW = "BW".equals(region1) || "BW".equals(region2);
            boolean inclDE = "DE".equals(region1) || "DE".equals(region2);
            boolean inclFR = "FR".equals(region1) || "FR".equals(region2);
            boolean inclIO = "IO".equals(region1) || "IO".equals(region2);
            boolean inclNX = "NX".equals(region1) || "NX".equals(region2);
            boolean inclPZ = "PZ".equals(region1) || "PZ".equals(region2);
            boolean inclSI = "SI".equals(region1) || "SI".equals(region2);
            boolean inclSH = "SH".equals(region1) || "SH".equals(region2);
            boolean inclMT = "MT".equals(region1) || "MT".equals(region2);
            filterCards(inclBC, inclBW, inclDE, inclFR, inclIO, inclNX, inclPZ, inclSI, inclSH, inclMT,
                        true, true, true, true, true, true, true);
        }
    }
    
    public void insertInOrder(DefaultListModel<StaticLoRCard> dlmCards, StaticLoRCard card) {
        // If there are no cards in the list, simply add the card.
        if (dlmCards.size() == 0) {
            dlmCards.addElement(card);
            return;
        }
        
        // If the cost of the card to be added exceeds the greatest cost card,
        // add the card to the end of the list.
        // (Assumes card list is already sorted in ascending order by card cost.)
        if (card.getCost() > dlmCards.get(dlmCards.size() - 1).getCost() ||
           (card.getCost() == dlmCards.get(dlmCards.size() - 1).getCost() &&
            card.getName().compareTo(dlmCards.get(dlmCards.size() - 1).getName()) > 0)) {
            dlmCards.add(dlmCards.size(), card);
            return;
        }
        
        for (int i = 0; i < dlmCards.size(); i++) {
            // Insert the card in order by its cost.
            if (card.getCost() < dlmCards.get(i).getCost()) {
                dlmCards.add(i, card);
                return;
            }
            // If there are multiple cards of the same cost, insert the card
            // in order by its cost and THEN its name.
            else if (card.getCost() == dlmCards.get(i).getCost() &&
                     card.getName().compareTo(dlmCards.get(i).getName()) < 0) {
                dlmCards.add(i, card);
                return;
            }
        }
    }
    
    public void insertInOrder(DefaultListModel<DeckCard> dlmDeckCards, DeckCard deckCard, boolean addToDeck) {
        // If there are no cards in the list, simply add the card(s).
        if (dlmDeckCards.size() == 0) {
            dlmDeckCards.addElement(deckCard);
            if (addToDeck)
                currentDeck.getDeck().addCard(LoRCard.create(deckCard.getCard().getCardCode()), deckCard.getQuantity());
            lblDeckSize.setText(String.valueOf(currentDeck.getDeck().deckSize()) + " / 40 Cards");
            return;
        }

        // Prevent the user from having more than six champions in the deck.
        if ("Champion".equals(deckCard.getCard().getRarity())) {
            int champCount = 0;
            for (int i = 0; i < dlmDeckCards.size(); i++) {
                if ("Champion".equals(dlmDeckCards.get(i).getCard().getRarity())) {
                    champCount += dlmDeckCards.get(i).getQuantity();
                }
            }
            if (champCount + deckCard.getQuantity() > 6) {
                JOptionPane.showMessageDialog(rootPane, "You cannot have more than six champions in a deck!",
                                              "Deck Editor", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        // If the cost of the deck card to be added exceeds the greatest cost card,
        // add the card to the end of the list.
        // (Assumes deck card list is already sorted in ascending order by card cost.)
        if (deckCard.getCard().getCost() > dlmDeckCards.get(dlmDeckCards.size() - 1).getCard().getCost() ||
           (deckCard.getCard().getCost() == dlmDeckCards.get(dlmDeckCards.size() - 1).getCard().getCost() &&
            deckCard.getCard().getName().compareTo(dlmDeckCards.get(dlmDeckCards.size() - 1).getCard().getName()) > 0)) {
            dlmDeckCards.add(dlmDeckCards.size(), deckCard);
            if (addToDeck)
                currentDeck.getDeck().addCard(LoRCard.create(deckCard.getCard().getCardCode()), deckCard.getQuantity());
            lblDeckSize.setText(String.valueOf(currentDeck.getDeck().deckSize()) + " / 40 Cards");
            return;
        }
        
        // If the deck card to be added already exists in the list,
        // attempt to increment the quantity of the existing element.
        for (int i = 0; i < dlmDeckCards.size(); i++) {
            if (deckCard.getCard().getName().equals(dlmDeckCards.get(i).getCard().getName())) {
                // Prevent the user from having more than three copies of a card in the deck.
                if (dlmDeckCards.get(i).getQuantity() + deckCard.getQuantity() > 3) {
                    JOptionPane.showMessageDialog(rootPane, "You cannot have more than three copies of a card in a deck!", 
                                                  "Deck Editor", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                StaticLoRCard card = dlmDeckCards.get(i).getCard();
                int quantity = dlmDeckCards.get(i).getQuantity();
                dlmDeckCards.remove(i);
                dlmDeckCards.add(i, new DeckCard(card, quantity + deckCard.getQuantity()));
                if (addToDeck) {
                    currentDeck.getDeck().removeCard(LoRCard.create(deckCard.getCard().getCardCode()));
                    currentDeck.getDeck().addCard(LoRCard.create(deckCard.getCard().getCardCode()), quantity + deckCard.getQuantity());
                }
                lblDeckSize.setText(String.valueOf(currentDeck.getDeck().deckSize()) + " / 40 Cards");
                return;
            }
        }
        
        // Otherwise, insert the deck card in order by its cost (then in order by name if costs are equal).
        for (int i = 0; i < dlmDeckCards.size(); i++) {
            if (deckCard.getCard().getCost() < dlmDeckCards.get(i).getCard().getCost()) {
                dlmDeckCards.add(i, deckCard);
                if (addToDeck)
                    currentDeck.getDeck().addCard(LoRCard.create(deckCard.getCard().getCardCode()), deckCard.getQuantity());
                lblDeckSize.setText(String.valueOf(currentDeck.getDeck().deckSize()) + " / 40 Cards");
                return;
            }
            if (deckCard.getCard().getCost() == dlmDeckCards.get(i).getCard().getCost() &&
                     deckCard.getCard().getName().compareTo(dlmDeckCards.get(i).getCard().getName()) < 0) {
                dlmDeckCards.add(i, deckCard);
                if (addToDeck)
                    currentDeck.getDeck().addCard(LoRCard.create(deckCard.getCard().getCardCode()), deckCard.getQuantity());
                lblDeckSize.setText(String.valueOf(currentDeck.getDeck().deckSize()) + " / 40 cards");
                return;
            }
        }
    }
    
    public void filterCards(boolean inclBC, boolean inclBW, boolean inclDE, boolean inclFR, boolean inclIO, 
                            boolean inclNX, boolean inclPZ, boolean inclSI, boolean inclSH, boolean inclMT,
                            boolean inclUnits, boolean inclSpells, boolean inclLandmarks, 
                            boolean inclCommons, boolean inclRares, boolean inclEpics, boolean inclChampions) {
        dlmCardsFiltered.clear();
        
        for (int i = 0; i < dlmCards.size(); i++) {
            String region = dlmCards.get(i).getCardCode().substring(2, 4);
            String type = dlmCards.get(i).getType();
            String rarity = dlmCards.get(i).getRarity();
            
            if (((inclBC && "BC".equals(region)) || (inclBW && "BW".equals(region)) || (inclDE && "DE".equals(region)) ||
                 (inclFR && "FR".equals(region)) || (inclIO && "IO".equals(region)) || (inclNX && "NX".equals(region)) ||
                 (inclPZ && "PZ".equals(region)) || (inclSI && "SI".equals(region)) || (inclSH && "SH".equals(region)) ||
                 (inclMT && "MT".equals(region))) &&
                ((inclUnits && "Unit".equals(type)) || (inclSpells && "Spell".equals(type)) || (inclLandmarks && "Landmark".equals(type))) &&
                ((inclCommons && "COMMON".equals(rarity)) || (inclRares && "RARE".equals(rarity)) || 
                 (inclEpics && "EPIC".equals(rarity)) || (inclChampions && "Champion".equals(rarity)))) {
                dlmCardsFiltered.addElement(dlmCards.get(i));
            }
        }
        
        hasBeenFiltered = true;
        jlstCards.setModel(dlmCardsFiltered);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jtfDeckName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jlstCards = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        jlstDeckCards = new javax.swing.JList<>();
        btnAddCard = new javax.swing.JButton();
        btnRemoveCard = new javax.swing.JButton();
        btnDone = new javax.swing.JButton();
        lblDeckSize = new javax.swing.JLabel();
        btnFilter = new javax.swing.JButton();

        setTitle("Deck Editor");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jtfDeckName.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtfDeckName.setText("DECK NAME");

        jlstCards.setModel(dlmCards);
        jlstCards.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jlstCards.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jlstCardsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jlstCards);

        jlstDeckCards.setModel(dlmDeckCards);
        jlstDeckCards.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jlstDeckCards.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jlstDeckCardsValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jlstDeckCards);

        btnAddCard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/left_arrow_icon.png"))); // NOI18N
        btnAddCard.setToolTipText("Add a copy of selected card to deck.");
        btnAddCard.setContentAreaFilled(false);
        btnAddCard.setEnabled(false);
        btnAddCard.setFocusPainted(false);
        btnAddCard.setFocusable(false);
        btnAddCard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCardActionPerformed(evt);
            }
        });

        btnRemoveCard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/right_arrow_icon.png"))); // NOI18N
        btnRemoveCard.setToolTipText("Remove a copy of selected card from deck.");
        btnRemoveCard.setContentAreaFilled(false);
        btnRemoveCard.setEnabled(false);
        btnRemoveCard.setFocusable(false);
        btnRemoveCard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveCardActionPerformed(evt);
            }
        });

        btnDone.setText("Done");
        btnDone.setToolTipText("Done editing deck.");
        btnDone.setContentAreaFilled(false);
        btnDone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDoneActionPerformed(evt);
            }
        });

        lblDeckSize.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDeckSize.setText("0 / 40  Cards");

        btnFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/filter_icon.png"))); // NOI18N
        btnFilter.setToolTipText("Filter cards by region, type, and rarity.");
        btnFilter.setContentAreaFilled(false);
        btnFilter.setFocusable(false);
        btnFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnDone, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDeckSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane2)
                    .addComponent(jtfDeckName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnRemoveCard, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAddCard, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jtfDeckName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnAddCard, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnRemoveCard, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE))
                            .addComponent(jScrollPane2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnFilter)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnDone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblDeckSize, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jScrollPane1))
                .addContainerGap(47, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRemoveCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveCardActionPerformed
        int index = jlstDeckCards.getSelectedIndex();
        if (index == -1)
            return;
        
        StaticLoRCard card = dlmDeckCards.get(index).getCard();
        int quantity = dlmDeckCards.get(index).getQuantity();

        // Remove the deckCard at the selected index, and if there should still be a deckCard
        // left, add the deckCard back at the same index with a decremented quantity.
        dlmDeckCards.remove(index);
        currentDeck.getDeck().removeCard(LoRCard.create(card.getCardCode()));
        if (quantity > 1) {
            dlmDeckCards.add(index, new DeckCard(card, quantity - 1));
            currentDeck.getDeck().addCard(LoRCard.create(card.getCardCode()), quantity - 1);
        } 
        // Set the selected index for user convenience.
        if (index < dlmDeckCards.size()) {
            jlstDeckCards.setSelectedIndex(index);
        } else if (index > 0) {
            jlstDeckCards.setSelectedIndex(index - 1);
        }
        
        currentDeck.updateRegions();
        // If the deck has recently lost its second region, reset the hasBeenAutoFiltered flag
        // and restore cards list to the original.
        if (!"".equals(currentDeck.getRegion1()) && "".equals(currentDeck.getRegion2()) && hasBeenFiltered) {
            hasBeenFiltered = false;
            jlstCards.setModel(dlmCards);
        }
        
        // Update the deck size label after everything is done.
        lblDeckSize.setText(String.valueOf(currentDeck.getDeck().deckSize()) + " / 40 Cards");
    }//GEN-LAST:event_btnRemoveCardActionPerformed

    private void btnDoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDoneActionPerformed
        String deckName = jtfDeckName.getText();
        if ("".equals(deckName))
            deckName = "New Deck";
        currentDeck.setName(deckName);
        
        // Notify the user if the deck is incomplete.
        if (currentDeck.getDeck().deckSize() < 40) {
            int n = JOptionPane.showConfirmDialog(rootPane, "Your deck is incomplete! Save changes anyway?" + "\n" +
                                                  "(You will not be able to export an incomplete deck.)", "Incomplete Deck",
                                                  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (n == JOptionPane.NO_OPTION)
                return;
        }
        
        this.setVisible(false);
        deckManagerWindow.setVisible(true);
    }//GEN-LAST:event_btnDoneActionPerformed

    private void btnAddCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCardActionPerformed
        int index = jlstCards.getSelectedIndex();
        if (index == -1)
            return;
        
        // Prevent the user from having more than 40 cards in the deck.
        if (currentDeck.getDeck().deckSize() >= 40) {
            JOptionPane.showMessageDialog(rootPane, "You cannot have more than 40 cards in a deck!", 
                                          "Deck Editor", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Add the selected card to the deck.
        if (hasBeenFiltered)
            insertInOrder(dlmDeckCards, new DeckCard(dlmCardsFiltered.get(index), 1), true);
        else
            insertInOrder(dlmDeckCards, new DeckCard(dlmCards.get(index), 1), true);
        currentDeck.updateRegions();
        
        // If the deck has two regions and has not been filtered yet,
        // automatically filter cards to all in the two current regions.
        String region1 = currentDeck.getRegion1();
        String region2 = currentDeck.getRegion2();
        if (!("".equals(region1) || "".equals(region2)) && !hasBeenFiltered) {
            boolean inclBC = "BC".equals(region1) || "BC".equals(region2);
            boolean inclBW = "BW".equals(region1) || "BW".equals(region2);
            boolean inclDE = "DE".equals(region1) || "DE".equals(region2);
            boolean inclFR = "FR".equals(region1) || "FR".equals(region2);
            boolean inclIO = "IO".equals(region1) || "IO".equals(region2);
            boolean inclNX = "NX".equals(region1) || "NX".equals(region2);
            boolean inclPZ = "PZ".equals(region1) || "PZ".equals(region2);
            boolean inclSI = "SI".equals(region1) || "SI".equals(region2);
            boolean inclSH = "SH".equals(region1) || "SH".equals(region2);
            boolean inclMT = "MT".equals(region1) || "MT".equals(region2);
            filterCards(inclBC, inclBW, inclDE, inclFR, inclIO, inclNX, inclPZ, inclSI, inclSH, inclMT,
                        true, true, true, true, true, true, true);
            // Once the cards have been filtered, select the card that the user
            // had previously been selecting for convenience.
            for (int i = 0; i < dlmCardsFiltered.size(); i++) {
                if (dlmCards.get(index).getName().equals(dlmCardsFiltered.get(i).getName())) {
                    jlstCards.setSelectedIndex(i);
                    break;
                }
            }
        }
    }//GEN-LAST:event_btnAddCardActionPerformed

    private void jlstCardsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jlstCardsValueChanged
        if (jlstCards.getValueIsAdjusting())
            return;
        
        int index = jlstCards.getSelectedIndex();
        btnAddCard.setEnabled(index != -1);
    }//GEN-LAST:event_jlstCardsValueChanged

    private void jlstDeckCardsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jlstDeckCardsValueChanged
        if (jlstDeckCards.getValueIsAdjusting())
            return;
        
        int index = jlstDeckCards.getSelectedIndex();
        btnRemoveCard.setEnabled(index != -1);
    }//GEN-LAST:event_jlstDeckCardsValueChanged

    private void btnFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterActionPerformed
        FilterWindow filterWindow = new FilterWindow(this, currentDeck);
        filterWindow.setVisible(true);
    }//GEN-LAST:event_btnFilterActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        String deckName = jtfDeckName.getText();
        if ("".equals(deckName))
            deckName = "New Deck";
        currentDeck.setName(deckName);
        
        // Notify the user if the deck is incomplete.
        if (currentDeck.getDeck().deckSize() < 40) {
            int n = JOptionPane.showConfirmDialog(rootPane, "Your deck is incomplete! Save changes anyway?" + "\n" +
                                                  "(You will not be able to export an incomplete deck.)", "Incomplete Deck",
                                                  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (n == JOptionPane.NO_OPTION)
                return;
        }
        
        this.setVisible(false);
        deckManagerWindow.setVisible(true);       
    }//GEN-LAST:event_formWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DeckEditorWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DeckEditorWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DeckEditorWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DeckEditorWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
//                new DeckEditorWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCard;
    private javax.swing.JButton btnDone;
    private javax.swing.JButton btnFilter;
    private javax.swing.JButton btnRemoveCard;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    /*
    private javax.swing.JList<String> jlstCards;
    */
    private javax.swing.JList<StaticLoRCard> jlstCards;
    /*
    private javax.swing.JList<String> jlstDeckCards;
    */
    private javax.swing.JList<DeckCard> jlstDeckCards;
    private javax.swing.JTextField jtfDeckName;
    private javax.swing.JLabel lblDeckSize;
    // End of variables declaration//GEN-END:variables
}
