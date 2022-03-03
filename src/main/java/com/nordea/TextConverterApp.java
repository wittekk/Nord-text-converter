package com.nordea;

import com.nordea.textconverter.TextConverter;

public class TextConverterApp {
    public static final TextConverter textConverter = new TextConverter(
            "src/main/resources/input/small.in",
            "src/main/resources/output/small.xml",
            "src/main/resources/output/small.csv");

    public static void main(String[] args) {
        textConverter.readFileToSentences();
    }
}
