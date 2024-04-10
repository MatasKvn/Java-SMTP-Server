package org.mataskvn.smtp.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static void main(String[] args) {
        String input = "This is a <test> string with <multiple> occurrences of <words> between <> brackets.";
        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(input);

//        while (matcher.find()) {
            matcher.find();
            String match = matcher.group(1); // Get the content inside the angle brackets
            System.out.println("Match: " + match);
//        }
    }
}
