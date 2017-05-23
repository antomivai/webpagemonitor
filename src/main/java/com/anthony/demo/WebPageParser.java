package com.anthony.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Created by anthony on 5/19/17.
 */
@Service
public class WebPageParser {
    private static String DOCUMENT_ROOT= "/Users/anthony/Downloads/html_webpage";
    private static String LATEST_FILE_VERSION = DOCUMENT_ROOT+"/temp.html";
    private static String PREVIOUS_FILE_VERSION = DOCUMENT_ROOT+"/page.html";

    @Value("${webpagemonitor.change_threshold}")
    private static double SIGNIFICANT_CHANGE_THRESHOLD;

    public WebPageParser() {


    }

    public boolean evaluateChange(String pageUrl) {
        double changePercentage = 0;

        try {
            //Get webpage content and write to a local temp file
            File latestVersion = new File(LATEST_FILE_VERSION);
            saveWebpageToLocalFile(pageUrl, latestVersion);

            File previousVersion = new File(PREVIOUS_FILE_VERSION);
            if(previousVersion.exists()) {
                changePercentage = evaluateChangePercentage(LATEST_FILE_VERSION,PREVIOUS_FILE_VERSION);
            }

            //after change analysis is completed
            //delete the PREVIOUS_FILE_VERSION and rename the LATEST_FILE_VERSION to PREVIOUS_FILE_VERSION
            if(previousVersion.delete()) {
                latestVersion.renameTo(previousVersion);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return changePercentage > SIGNIFICANT_CHANGE_THRESHOLD;
    }

    //WARNING: this algo is not smart enough to recognize that the two files are identical,
    // the only difference is that in 1 file all content is in one single line.
    public double evaluateChangePercentage(String latestFileVersion, String previousFileVersion) throws IOException {
        //analyze difference if any
        String latestContent, previousContent = null;
        int charDiffCount = 0;
        int totalWebpageChars = 0;

        BufferedReader latestFileVersionReader = new BufferedReader(new FileReader(latestFileVersion));
        BufferedReader previousFileVersionReader = new BufferedReader(new FileReader(previousFileVersion));

        while((latestContent=latestFileVersionReader.readLine()) != null &&
                (previousContent=previousFileVersionReader.readLine()) != null) {

                //remove leading and trailing whitespace
                latestContent = latestContent.trim();
                previousContent = previousContent.trim();

                //count total characters in the previousContent
                totalWebpageChars += previousContent.length();

                if(!latestContent.equals(previousContent)) {
                    charDiffCount += countCharacterDifference(latestContent,previousContent);
                }
        }


        //when previousFileVersion end and the latestFileVersion still has content
        //the remaining characters in the latestFileVersion will add to the charDiffCount
        if(latestContent != null && previousContent == null) {
            do {
                //remove leading and trailing whitespace
                latestContent = latestContent.trim();
                charDiffCount += latestContent.length();
            } while((latestContent=latestFileVersionReader.readLine()) != null);

        }

        //when latestFileVersion end and the previousFileVersion still has content
        //the remaining characters in the previousFileVersion will add to the charDiffCount
        while((previousContent=previousFileVersionReader.readLine()) != null) {
            //remove leading and trailing whitespace
            previousContent = previousContent.trim();
            charDiffCount += previousContent.length();
            totalWebpageChars += previousContent.length();
        }

        if(totalWebpageChars==0) {
            throw new RuntimeException("Unable to evaluate the changed percentage: the TotalChars is zero.");
        } else {
            return ((double)charDiffCount)/totalWebpageChars;
        }
    }

    private int countCharacterDifference(String webLine, String fileLine) {
        int shorterLength = 0;

        if(webLine.length() >= fileLine.length()) {
            shorterLength = fileLine.length();
        } else{
            shorterLength = webLine.length();
        }

        int count = Math.abs(webLine.length() - fileLine.length());

        //handle index out of bound when one line is longer than another
        for(int i=0; i<shorterLength; i++) {
            if(webLine.charAt(i) != fileLine.charAt(i)) {
                count++;
            }
        }

        return count;
    }

    private void saveWebpageToLocalFile(String pageUrl, File file) throws IOException {
        URL url = new URL(pageUrl);
        URLConnection uc = url.openConnection();
        InputStreamReader input = new InputStreamReader(uc.getInputStream());
        BufferedReader webReader = new BufferedReader(input);

        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        String line;
        while ((line= webReader.readLine()) != null){
            writer.write(line);
            writer.newLine();
        }
        writer.close();
    }
}
