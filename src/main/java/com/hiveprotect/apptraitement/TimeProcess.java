/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hiveprotect.apptraitement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.TimerTask;
import javafx.application.Platform;

/**
 *
 * @author hiveprotect
 */
public class TimeProcess extends TimerTask {

    private Long time;
    private final FXMLController fx;

    public TimeProcess(Long time, FXMLController fx) {
        this.time = time;
        this.fx = fx;
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            this.time -= 1000;
            Date d = new Date(this.time);

            // formattter
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            // Pass date object
            String sRest = formatter.format(d);

            this.fx.setClock(sRest);

        }
        );
    }

    public void setTime(Long time) {
        this.time = time;
    }

}
