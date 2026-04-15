package com.uth.mobileBE.Utils;

public class GenerateBarcode {

    public static String generateBarcode(String categoryName,Long bookId, int sequence) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return "BK";
        }

        StringBuilder acronym = new StringBuilder();
        String[] words = categoryName.trim().split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                acronym.append(word.charAt(0));
            }
        }
        if(sequence<=0){
            return acronym.toString().toUpperCase() + "-" + bookId + "-" + 1;
        }
        return acronym.toString().toUpperCase() + "-" + bookId + "-" + sequence;
    }
}