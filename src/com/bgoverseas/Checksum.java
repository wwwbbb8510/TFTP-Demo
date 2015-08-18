package com.bgoverseas;

import java.io.*;
import java.util.Scanner;
import java.util.zip.CRC32;

/**
 * Created by binwang on 18/08/15.
 * checksum used to check the file identification between server and client
 */
public class Checksum {
    public static void main(String[] args){
        while (true){
            Scanner scanner = new Scanner(System.in);
            String fileName = scanner.next();
            if(fileName.equals("exit")){
                System.out.println("Exiting");
                System.exit(0);
            }
            File file = new File(fileName);
            if(file.exists()){
                try {
                    InputStream input = new FileInputStream(file);
                    byte[]  content = new byte[(int)file.length()];
                    if(input.read(content) > 0){
                        CRC32 ck = new CRC32();
                        ck.update(content, 0, content.length);
                        long value = ck.getValue();
                        System.out.println("Checksum for file:" + fileName + " is :" +value);
                    }else {
                        System.out.println("Empty file");
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Fail to open the file");
                } catch (IOException e) {
                    System.out.println("Fail to read the file");
                }
            }else{
                System.out.println("File not found");
            }

        }
    }
}
