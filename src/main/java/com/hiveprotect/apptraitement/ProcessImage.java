/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hiveprotect.apptraitement;

import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Choutzi
 */
public class ProcessImage implements Runnable {

    private final FXMLController cont;
    private final ArrayList<File> videos;

    public ProcessImage(FXMLController cont, ArrayList<File> videos) {
        this.cont = cont;
        this.videos = videos;
    }

    @Override
    public void run() {
        double nVideo = 0;
        this.cont.setPregress(nVideo);
        Map<String, Boolean> iconList = this.cont.getIconList();
        iconList.clear();
        String pathDarknet = this.cont.getProp().getProperty("darknetPath") + "/";
        String callDarknet = "./darknet detect cfg/yolov3-tiny.cfg yolov3-tiny.weights ";
        String pathSave = this.cont.getProp().getProperty("videoFolder") + "/";

        //traitement des vidéos
        for (File f : this.videos) {
            String name = f.getName().substring(0, f.getName().lastIndexOf('.'))+"_result.jpg";
            try {
                ProcessBuilder pb = new ProcessBuilder("sh", "-c", "cd " + pathDarknet + " ; " + callDarknet + f.getAbsolutePath());
                Process p = pb.start();     // Start the process.
                p.waitFor();            // Wait for the process to finish.

                InputStream is = p.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                outToString(br, f);
                
                ProcessBuilder pbCp = new ProcessBuilder("sh", "-c", "cp " + pathDarknet + "predictions.jpg " + pathSave + name);
                Process pCp = pbCp.start();     // Start the process.
                pCp.waitFor();            // Wait for the process to finish.

                this.cont.iconDisplay(f, true);
            } catch (IOException | InterruptedException e) {
                this.cont.iconDisplay(f, false);
            } finally {
                nVideo++;
                this.cont.setPregress(nVideo / this.videos.size());
            }
        }
        //changement d'état
        if (this.cont.getVideoSelected() != null) {
            this.cont.goToState(Etat.videoSelected);
        } else {
            this.cont.goToState(Etat.Fill);
        }
    }

    private void outToString(BufferedReader br, File f) {
        try {
            String ligne;
            String pathResult = this.cont.getProp().getProperty("tabFolder")+"/";
            String name = f.getName().substring(0, f.getName().lastIndexOf('.'))+".csv";
            
            FileWriter wr = new FileWriter(new File(pathResult + name));

            CSVWriter writer = new CSVWriter(wr);

            String[] head = {"Classe", "Confiance", "left_x", "top_y", "width", "height"};
            writer.writeNext(head);

            while ((ligne = br.readLine()) != null) {
                String[] don = ligne.split(":");
                if (don.length == 6) {
                    String classe = don[0];
                    String percent = don[1].replaceAll("[^0-9]", "");
                    String left_x = don[2].replaceAll("[^0-9]", "");
                    String top_y = don[3].replaceAll("[^0-9]", "");
                    String width = don[4].replaceAll("[^0-9]", "");
                    String height = don[5].replaceAll("[^0-9]", "");

                    writer.writeNext(new String[]{classe, percent, left_x, top_y, width, height});
                }
            }

            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(ProcessImage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
