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
public class ProcessVideo extends ProcessAbs implements Runnable {

    private final FXMLController cont;
    private final ArrayList<File> videos;
    private boolean work;

    public ProcessVideo(FXMLController cont, ArrayList<File> videos) {
        this.cont = cont;
        this.videos = videos;
        this.work = true;
    }

    @Override
    public void run() {
        double nVideo = 0;
        this.cont.setPregress(nVideo);
        Map<String, Boolean> iconList = this.cont.getIconList();
        iconList.clear();
        String pathDarknet = this.cont.getProp().getProperty("darknetPath") + "/";
        String callDarknet = "./darknet detector demo data/obj.data cfg/yolov3-tiny-hornet.cfg backup/yolov3-tiny-hornet_9000.weights -dont_show ";
        String pathSave = this.cont.getProp().getProperty("videoFolder") + "/";

        //traitement des vidéos
        for (File f : this.videos) {
            if (!work) {
                return;
            }

            String name = f.getName().substring(0, f.getName().lastIndexOf('.')) + "_result.mp4";
            try {
                String ligne = "";
                ProcessBuilder pb = new ProcessBuilder("sh", "-c", "cd "+pathDarknet+" ; "+callDarknet+" "+f.getAbsolutePath()+" -out_filename result.avi");
                Process p = pb.start();     // Start the process.
                // Wait for the process to finish.

                BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                outToString(output, f);

                while ((ligne = error.readLine()) != null) {
                    System.out.println(ligne + " - error");
                }

                p.waitFor();
                

                ProcessBuilder pbCp = new ProcessBuilder("sh", "-c", "cp " + pathDarknet + "result.avi " + pathSave + name);
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
            String pathResult = this.cont.getProp().getProperty("tabFolder") + "/";
            String name = f.getName().substring(0, f.getName().lastIndexOf('.')) + ".csv";
            int frame = 0;

            FileWriter wr = new FileWriter(new File(pathResult + name));

            CSVWriter writer = new CSVWriter(wr);

            String[] head = {"Classe", "Confiance", "left_x", "top_y", "width", "height", "frame"};
            writer.writeNext(head);

            while ((ligne = br.readLine()) != null) {
                System.out.println(ligne);
                if (ligne.contains("Objects:")) {
                    frame++;
                }

                String[] don = ligne.split(":");
                if (don.length == 6) {
                    String classe = don[0];
                    String percent = don[1].replaceAll("[^0-9]", "");
                    String left_x = don[2].replaceAll("[^0-9]", "");
                    String top_y = don[3].replaceAll("[^0-9]", "");
                    String width = don[4].replaceAll("[^0-9]", "");
                    String height = don[5].replaceAll("[^0-9]", "");

                    writer.writeNext(new String[]{classe, percent, left_x, top_y, width, height, Integer.toString(frame)});
                }
            }
            writer.writeNext(new String[]{Integer.toString(frame)});
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(ProcessVideo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setWork(boolean work) {
        this.work = work;
    }

}
