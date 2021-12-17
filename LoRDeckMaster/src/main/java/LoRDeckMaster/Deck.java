/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LoRDeckMaster;

import java.util.Map;
import no.stelar7.api.r4j.impl.lor.LoRDeckCode;
import no.stelar7.api.r4j.pojo.lor.offline.card.LoRDeck;
import no.stelar7.api.r4j.pojo.lor.offline.card.LoRCard;

/**
 *
 * @author hoski
 */
public class Deck {
    
    private String name;
    private LoRDeck deck;
    private String region1;
    private String region2;
    
    // CONSTRUCTORS
    // ------------
    // Create an empty deck.
    public Deck() {  
        this.name = "New Deck";
        this.deck = new LoRDeck();
        this.region1 = "";
        this.region2 = "";
    }
    
    // Creates a deck from a given deck code.
    public Deck(String deckCode) { 
        this.name = "Imported Deck";
        this.deck = LoRDeckCode.decode(deckCode);
        
        // Populate the region fields.
        updateRegions();
    }
    
    // GETTERS AND SETTERS
    // -------------------
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public LoRDeck getDeck() {
        return this.deck;
    }
    
    public String getRegion1() {
        return this.region1;
    }
    
    public void setRegion1(String region1) {
        this.region1 = region1;
    }

    public String getRegion2() {
        return this.region2;
    }
    
    public void setRegion2(String region2) {
        this.region2 = region2;
    }
    
    public void updateRegions() {
        region1 = "";
        region2 = "";
        
        for (Map.Entry<LoRCard, Integer> card : deck.getDeck().entrySet()) {
            String region = card.getKey().getCardCode().substring(2, 4);
            if ("".equals(region1))
                region1 = region;
            else if (!region.equals(region1) && "".equals(region2))
                region2 = region;
        }
    }
}
