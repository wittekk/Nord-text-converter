package com.nordea.textconverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class TextConverterTest {
    private final String inputFile = "src/test/resources/input/test.in";
    private final String outputXmlFile = "src/test/resources/output/small.xml";
    private final String outputCsvFile = "src/test/resources/output/small.csv";
    TextConverter textConverter = new TextConverter(inputFile, outputXmlFile, outputCsvFile);

    @Test
    void readFileToSentences_Test() throws IOException {
        //given
        File expectedXmlFile = new File("src/test/resources/input/small.xml");
        File expectedCsvFile = new File("src/test/resources/input/small.csv");

        //when
        textConverter.readFileToSentences();
        File actualXmlFile = new File(outputXmlFile);
        File actualCsvFile = new File(outputCsvFile);

        //then
        assertEquals(FileUtils.readFileToString(expectedXmlFile, "utf-8"), FileUtils.readFileToString(actualXmlFile, "utf-8"));
        assertEquals(FileUtils.readFileToString(expectedCsvFile, "utf-8"), FileUtils.readFileToString(actualCsvFile, "utf-8"));
    }

    @Test
    void getFile_noFileTest() {
        assertThrows(RuntimeException.class, () -> textConverter.getFile(new File("no_file")));
    }

    @Test
    void getFile_toBigFileTest() throws IOException {
        //given
        String inputFileSize = "src/test/resources/input/testSize.in";
        TextConverter textConverterForSizeCheck = new TextConverter(inputFileSize, outputXmlFile, outputCsvFile);

        //when
        File fileToBig = new File(inputFileSize);
        RandomAccessFile oversizeFile = new RandomAccessFile(fileToBig, "rw");
        oversizeFile.setLength(10500000);

        //then
        assertThrows(RuntimeException.class, () -> textConverterForSizeCheck.getFile(fileToBig));
        oversizeFile.setLength(20);
    }
}