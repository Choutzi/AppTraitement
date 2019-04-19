/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hiveprotect.apptraitement;

import java.io.BufferedReader;
import java.io.File;

/**
 *
 * @author hiveprotect
 */
abstract class ProcessAbs implements Runnable{
    
    abstract public void setWork(boolean work);
    
}
