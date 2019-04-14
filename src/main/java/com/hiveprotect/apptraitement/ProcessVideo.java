/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hiveprotect.apptraitement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Choutzi
 */
public class ProcessVideo implements Runnable {

    private final FXMLController cont;
    private final ArrayList<File> videos;

    public ProcessVideo(FXMLController cont, ArrayList<File> videos) {
        this.cont = cont;
        this.videos = videos;
    }

    @Override
    public void run() {
        double nVideo = 0;
        this.cont.setPregress(nVideo);
        Map<String, Boolean> iconList = this.cont.getIconList();
        iconList.clear();
        String shellDarknet = this.cont.getProp().getProperty("darknetPath") + " ";

        //traitement des vidéos
        for (File f : this.videos) {
            try {
                ProcessBuilder pb = new ProcessBuilder(shellDarknet);
                Process p = pb.start();     // Start the process.
                p.waitFor();            // Wait for the process to finish.

                this.cont.iconDisplay(f, true);
            } catch (IOException | InterruptedException e) {
                this.cont.iconDisplay(f, false);
            } finally {
                nVideo++;
                this.cont.setPregress(nVideo / this.videos.size());
            }

            //changement d'état
            if (this.cont.getVideoSelected() != null) {
                this.cont.goToState(Etat.videoSelected);
            } else {
                this.cont.goToState(Etat.Fill);
            }
        }
    }

}