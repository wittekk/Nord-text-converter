package com.nordea.textconverter;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sentence {
    private String sentenceText;
    private List<String> words = new LinkedList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sentence sentence = (Sentence) o;
        return Objects.equals(words, sentence.words);
    }

    @Override
    public int hashCode() {
        return Objects.hash(words);
    }
}
