/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Copy;

import java.io.*;

/**
 *
 * @author danielquintillanquintillan
 */
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Copy {
    public static void main(String[] args) throws IOException {

        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            inputStream = new FileInputStream(args[0]);
            outputStream = new FileOutputStream(args[1]);

            int c;
            while ((c = inputStream.read()) != -1) {
                outputStream.write(c);
            }                
                }
        catch (ArrayIndexOutOfBoundsException e){
            System.out.println("No files provided.");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
