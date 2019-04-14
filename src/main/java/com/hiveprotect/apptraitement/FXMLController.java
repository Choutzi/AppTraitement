package com.hiveprotect.apptraitement;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class FXMLController implements Initializable {

    @FXML
    private Button ajouterBut;

    @FXML
    private Button suppBut;

    @FXML
    private Button traiterBut;

    @FXML
    private Button annulerBut;

    @FXML
    private ListView listView;

    @FXML
    private ProgressBar progress;

    private Etat etat;

    private Object videoSelected;

    private final ArrayList<File> listVideosPath = new ArrayList<>();

    private final Map<String, Boolean> iconList = new HashMap<>();
    
    private Properties prop;

    @FXML
    private void ajouter_Clic(ActionEvent event) throws Exception {
        switch (etat) {
            case Init:
                goToState(Etat.Fill);
                ajouterSelectedFiles();
                break;
            case Fill:
                goToState(Etat.Fill);
                ajouterSelectedFiles();
                break;
            case Process:
                throw new Exception("Ajout impossible pendant un traitement");
            case videoSelected:
                goToState(Etat.videoSelected);
                ajouterSelectedFiles();
                break;
        }
    }

    @FXML
    private void supp_Clic(ActionEvent event) throws Exception {
        switch (etat) {
            case Init:
                throw new Exception("Suppression impossible sans sélectionner une vidéo");
            case Fill:
                throw new Exception("Suppression impossible sans sélectionner une vidéo");
            case Process:
                throw new Exception("Suppression impossible pendant un traitement");
            case videoSelected:
                goToState(Etat.videoSelected);
                remove(this.videoSelected);
                break;
        }
    }

    @FXML
    private void traiter_Clic(ActionEvent event) throws Exception {
        switch (etat) {
            case Init:
                throw new Exception("Traitement impossible sans vidéos");
            case Fill:
                goToState(Etat.Process);
                traiter();
                break;
            case Process:
                throw new Exception("Traitement impossible pendant un traitement");
            case videoSelected:
                goToState(Etat.Process);
                traiter();
                break;
        }
    }

    @FXML
    private void annuler_Clic(ActionEvent event) throws Exception {
        switch (etat) {
            case Init:
                throw new Exception("Aucun traitement à annuler");
            case Fill:
                throw new Exception("Aucun traitement à annuler");
            case Process:
                goToState(Etat.Fill);
                break;
            case videoSelected:
                throw new Exception("Aucun traitement à annuler");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        goToState(Etat.Init);
        addlistenerListView();
        loadPropeties();
    }

    private void goToState(Etat st) {
        this.etat = st;
        switch (st) {
            case Init:
                this.ajouterBut.setDisable(false);
                this.suppBut.setDisable(true);
                this.traiterBut.setDisable(true);
                this.annulerBut.setDisable(true);
                this.listView.setDisable(true);
                break;
            case Fill:
                this.ajouterBut.setDisable(false);
                this.suppBut.setDisable(true);
                this.traiterBut.setDisable(false);
                this.annulerBut.setDisable(true);
                this.listView.setDisable(false);
                break;
            case Process:
                this.ajouterBut.setDisable(true);
                this.suppBut.setDisable(true);
                this.traiterBut.setDisable(true);
                this.annulerBut.setDisable(false);
                this.listView.setDisable(true);
                break;
            case videoSelected:
                this.ajouterBut.setDisable(false);
                this.suppBut.setDisable(false);
                this.traiterBut.setDisable(false);
                this.annulerBut.setDisable(true);
                this.listView.setDisable(false);
                break;
        }
    }

    private void ajouterSelectedFiles() {
        FileChooser fc = new FileChooser();

        //default folder
        //fc.setInitialDirectory("C:\\...");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MP4 vidéo", "*.mp4"));

        List<File> selectedFiles = fc.showOpenMultipleDialog(null);

        if (selectedFiles != null) {
            for (File f : selectedFiles) {
                if (!this.listView.getItems().contains(f.getAbsolutePath())) {
                    this.listView.getItems().add(f.getAbsolutePath());
                    this.listVideosPath.add(f);
                }
            }
        } else {
            if (this.listView.getItems().isEmpty()) {
                goToState(Etat.Init);
            } else if (this.videoSelected != null) {
                goToState(Etat.videoSelected);
            } else {
                goToState(Etat.Fill);
            }
        }
    }

    private void addlistenerListView() {
        this.listView.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            switch (etat) {
                case Init:
                    throw new UnsupportedOperationException("Aucune vidéo à selectionner");
                case Fill:
                    goToState(Etat.videoSelected);
                    videoSelected = newValue;
                    break;
                case Process:
                    throw new UnsupportedOperationException("Sélection impossible pendant un traitement");
                case videoSelected:
                    if (newValue != null) {
                        goToState(Etat.videoSelected);
                    } else {
                        goToState(Etat.Init);
                    }
                    videoSelected = newValue;
                    break;
            }
        });
    }

    private void remove(Object video) {
        this.listView.getItems().remove(video);
        int index = -1;

        for (File f : this.listVideosPath) {
            if (f.getAbsoluteFile().equals(video)) {
                index = this.listVideosPath.indexOf(f);
            }
        }

        if (index != -1) {
            this.listVideosPath.remove(index);
        }
    }

    private void traiter() {
        double nVideo = 0;
        this.progress.setProgress(nVideo);
        this.iconList.clear();
        String shellDarknet = this.prop.getProperty("darknetPath")+" ";

        //traitement des vidéos
        for (File f : this.listVideosPath) {
            try {
                ProcessBuilder pb = new ProcessBuilder(shellDarknet);
                Process p = pb.start();     // Start the process.
                p.waitFor();            // Wait for the process to finish.

                iconDisplay(f, true);
            } catch (IOException | InterruptedException e) {
                iconDisplay(f, false);
            } finally {
                nVideo++;
                this.progress.setProgress(nVideo / this.listVideosPath.size());
            }

        }
        //changement d'état
        if (this.videoSelected != null) {
            goToState(Etat.videoSelected);
        } else {
            goToState(Etat.Fill);
        }
    }

    public void iconDisplay(File f, boolean res) {
        Image valid = new Image(getClass().getResource("/icon/iconValider.png").toString());
        Image echec = new Image(getClass().getResource("/icon/iconEchec.png").toString());
        this.iconList.put(f.getAbsolutePath(), res);

        listView.setCellFactory(param -> new ListCell<String>() {
            private ImageView imageView = new ImageView();

            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (iconList.containsKey(name)) {
                    if (iconList.get(name)) {
                        imageView.setImage(valid);
                    } else {
                        imageView.setImage(echec);
                    }
                    setText(name);
                    setGraphic(imageView);
                } else {
                    setText(name);
                    setGraphic(null);
                }
            }
        });
    }
    
    private void loadPropeties(){
        this.prop = new Properties();
        InputStream inputStream = getClass().getResourceAsStream("/properties/config.properties");
        try {
            this.prop.load(inputStream);
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
