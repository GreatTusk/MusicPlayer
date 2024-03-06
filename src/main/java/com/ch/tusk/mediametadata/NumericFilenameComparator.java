/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ch.tusk.mediametadata;

import com.ch.tusk.mediaplayerutil.StringFormatter;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author f_776
 */
public class NumericFilenameComparator implements Comparator<String> {

    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");

    @Override
    public int compare(String s1, String s2) {
        // Extract the numerical values from the strings

        // Extract the first sequence of digits from the file names
        String numStr1 = extractFirstDigits(StringFormatter.getFileNameFromMrl(s1));
        String numStr2 = extractFirstDigits(StringFormatter.getFileNameFromMrl(s2));
        try {

            int num1 = Integer.parseInt(numStr1);
            int num2 = Integer.parseInt(numStr2);

            // Compare based on numerical values
            return Integer.compare(num1, num2);
        } catch (NumberFormatException e) {
            // If the comparison fails, compare by strings
            return s1.compareTo(s2);
        }
    }

    // Helper method to extract the first sequence of digits from a string
    private String extractFirstDigits(String input) {
        Matcher matcher = DIGIT_PATTERN.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
}
