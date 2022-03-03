package com.nordea.textconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.CharEncoding;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Slf4j
public class TextConverter {

    private final long FILE_SIZE_LIMIT = 10485760;
    private final String SPECIAL_CHARACTERS_REGEX = "[$&+,:;=?@#|<>^*()%-]|[\t]";
    private final List<String> wordsWithDot = Arrays.asList("Mr.", "Ms.", "Dr.");
    private final Map<Sentence, List<String>> sentenceMap = new LinkedHashMap<>();
    private final StringBuilder sentenceStub = new StringBuilder();
    private Sentence sentence = new Sentence();

    private final String inputFile;
    private final String outputXmlFile;
    private final String outputCsvFile;

    public TextConverter(String inputFile, String outputXmlFile, String outputCsvFile) {
        this.inputFile = inputFile;
        this.outputXmlFile = outputXmlFile;
        this.outputCsvFile = outputCsvFile;
    }

    public void readFileToSentences() {
        File file = getFile(new File(inputFile));

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            LineIterator lineIterator = IOUtils.lineIterator(fileInputStream, CharEncoding.UTF_8);
            log.info("Try to read file");
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                sentenceCreator(line);
            }
            lineIterator.close();
            log.info("File has closed");
        } catch (Exception e) {
            log.error("Error in input/output when reading from a file : {}", file, e);
        }
        log.info("File has read successfully");
        createXml();
        log.info("XML file created successfully");
        createCsv();
        log.info("Csv file created successfully");
    }

    private void createCsv() {
        int maxSentence = 0;
        Set<Sentence> sentences = sentenceMap.keySet();
        for (Sentence sentence : sentences) {
            int size = sentence.getWords().size();
            if (size > maxSentence) {
                maxSentence = size;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Sentence");
        for (int i = 1; i < maxSentence; i++) {
            sb.append(", ").append("Word ").append(i);
        }
        sb.append('\n');

        int sentenceIndex = 1;
        for (Sentence sentence : sentenceMap.keySet()) {
            sb.append("Sentence ").append(sentenceIndex);
            sentenceIndex++;
            for (String word : sentence.getWords()) {
                sb.append(", ").append(word);
            }
            sb.append('\n');
        }

        try (PrintWriter writer = new PrintWriter(outputCsvFile)) {
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createXml() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("text");
            doc.appendChild(rootElement);
            for (Sentence key : sentenceMap.keySet()) {
                Element sentence = doc.createElement("sentence");
                rootElement.appendChild(sentence);
                for (String sentenceWord : key.getWords()) {
                    Element word = doc.createElement("word");
                    sentence.appendChild(word);
                    word.appendChild(doc.createTextNode(sentenceWord));
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(outputXmlFile));
            transformer.transform(source, result);
        } catch (ParserConfigurationException | TransformerException ex) {
            ex.printStackTrace();
        }
    }

    void sentenceCreator(String line) {
        String[] words = line.split(" ");
        for (String word : words) {
            sentenceStub.append(word).append(" ");
            if ((word.contains(".") && !wordsWithDot.contains(word)) || word.contains("!")) {
                String wordWithoutDot = word.replaceAll(SPECIAL_CHARACTERS_REGEX, "");
                if (!wordWithoutDot.isEmpty()) {
                    List<String> sentenceWords = sentence.getWords();
                    sentenceWords.add(wordWithoutDot);
                    sentenceWords.sort(String.CASE_INSENSITIVE_ORDER);
                    sentence.setSentenceText(sentenceStub.toString());
                    sentenceStub.setLength(0);
                    sentenceMap.put(sentence, sentenceWords);
                    sentence = new Sentence();
                }
            } else {
                if (!word.isEmpty()) {
                    String cleanWord = word.replaceAll(SPECIAL_CHARACTERS_REGEX, "");
                    sentence.getWords().add(cleanWord);
                }
            }
        }
    }

    File getFile(File file) {
        log.info("Try to import file");
        if (!file.exists()) {
            log.error("There is no file provided");
            throw new RuntimeException();
        }
        if (FileUtils.sizeOf(file) > FILE_SIZE_LIMIT) {
            log.error("File size is over limit {}", FileUtils.byteCountToDisplaySize(FILE_SIZE_LIMIT));
            throw new RuntimeException();
        }
        return file;
    }
}
