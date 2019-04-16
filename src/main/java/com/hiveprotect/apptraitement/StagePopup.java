/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hiveprotect.apptraitement;

import javafx.stage.Stage;

/**
 *
 * @author Choutzi
 */
public class StagePopup extends Stage {

    private final FXMLController fx;
    private final String propertie;

    public StagePopup(FXMLController f, String prop) {
        super();
        this.fx = f;
        this.propertie = prop;

        switch (prop) {
            case "darknetPath":
                setTitle("Configuration - Dossier de l'algorithme");
                break;
            case "videoFolder":
                setTitle("Configuration - Dossier des Vidéos résultats");
                break;
            case "tabFolder":
                setTitle("Configuration - Dossier des Données résultats");
                break;
            default:
                throw new UnsupportedOperationException("Mauvaise propriété");
        }
    }

    public void changeProp(String newPropertie) {
        fx.changeProp(propertie, newPropertie);
    }
}
