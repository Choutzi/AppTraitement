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
    private int totalTime = 0;
    private int framePasse = 0;
    private Process p;

    public ProcessVideo(FXMLController cont, ArrayList<File> videos) {
        this.cont = cont;
        this.videos = videos;
        this.work = true;
        init();

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
                ProcessBuilder pb = new ProcessBuilder("sh", "-c","export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/cuda/lib64 ; cd " + pathDarknet + " ; " + callDarknet + " " + f.getAbsolutePath() + " -out_filename result.avi");
                this.p = pb.start();     // Start the process.
                // Wait for the process to finish.

                BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
                //BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                outToString(output, f);

                //while ((ligne = error.readLine()) != null) {
                //    System.out.println(ligne + " - error");
                //}
                p.waitFor();

                ProcessBuilder pbCp = new ProcessBuilder("sh", "-c", "cp " + pathDarknet + "result.avi " + pathSave + name);
                Process pCp = pbCp.start();     // Start the process.
                pCp.waitFor();            // Wait for the process to finish.

                if (this.work) {
                    this.cont.iconDisplay(f, true);
                }
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
        this.cont.finished();
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

            while (this.work && (ligne = br.readLine()) != null) {
                if (ligne.contains("Objects:")) {
                    frame++;
                }

                if (ligne.contains("FPS:")) {
                    double fps = Double.parseDouble(ligne.replaceAll("[^\\d.]", ""));
                    setTime(fps, frame);
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
            this.framePasse = this.framePasse + frame;
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(ProcessVideo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setWork(boolean work) {
        this.work = work;
    }

    private void setTime(double fps, int frame) {
        Double tempTotal = (this.totalTime / fps) * 1000;
        Double tempRestant = ((this.totalTime - this.framePasse - frame) / fps) * 1000;

        System.out.println(fps);
        this.cont.timer(tempRestant.longValue());
    }

    private void init() {
        for (File f : this.videos) {
            try {
                String ligne = "";
                int nbFrame = 0;
                ProcessBuilder pb = new ProcessBuilder("sh", "-c", "mediainfo --fullscreen " + f.getAbsolutePath() + " | grep 'Frame count'");
                Process pr = pb.start();
                pr.waitFor();

                BufferedReader output = new BufferedReader(new InputStreamReader(pr.getInputStream()));

                while ((ligne = output.readLine()) != null && this.work) {
                    nbFrame = Integer.parseInt(ligne.replaceAll("[^0-9]", ""));
                }

                this.totalTime = this.totalTime + nbFrame;
                output.close();
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(ProcessVideo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Double tempTotal = (this.totalTime / 1.0) * 1000;
        this.cont.initTp(tempTotal.longValue());
    }

}
