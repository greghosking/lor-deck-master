/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LoRDeckMaster;

import no.stelar7.api.r4j.pojo.lor.staticdata.StaticLoRCard;

/**
 *
 * @author hoski
 */
public class DeckCard {
    
    private StaticLoRCard card;
    private int quantity;
    
    // CONSTRUCTORS
    // ------------
    public DeckCard(StaticLoRCard card, int quantity) {
        this.card = card;
        this.quantity = quantity;
    }
    
    // GETTERS AND SETTERS
    // -------------------
    public StaticLoRCard getCard() {
        return this.card;
    }
    
    public int getQuantity() {
        return this.quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
