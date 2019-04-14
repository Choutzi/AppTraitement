/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hiveprotect.apptraitement;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Choutzi
 */
public class PopupController implements Initializable {

    @FXML
    private TextField input_Parcourir;

    @FXML
    void annulerParcourir_Clic(ActionEvent event) {
        Stage st = (Stage) input_Parcourir.getScene().getWindow();
        st.close();
    }

    @FXML
    void parcourir_Clic(ActionEvent event) {
        DirectoryChooser fc = new DirectoryChooser();

        File selectedFolder = fc.showDialog(null);
        
        if(selectedFolder != null){
            this.input_Parcourir.setText(selectedFolder.getAbsolutePath());
        }
    }

    @FXML
    void validerParcourir_Clic(ActionEvent event) {
        if (!this.input_Parcourir.getText().equals("")) {
            StagePopup st = (StagePopup) input_Parcourir.getScene().getWindow();
            st.changeProp(this.input_Parcourir.getText());
            st.close();
        } else {

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

}
