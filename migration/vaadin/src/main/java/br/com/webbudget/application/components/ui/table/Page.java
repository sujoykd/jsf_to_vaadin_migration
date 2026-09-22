package br.com.webbudget.application.components.ui.table;

import java.util.List;

public class Page<T> {

    private final List<T> content;
    private final int totalResults;

    private Page(List<T> content, int totalResults) {
        this.content = content;
        this.totalResults = totalResults;
    }

    public static <T> Page<T> of(List<T> content, int totalResults) {
        return new Page<>(content, totalResults);
    }

    public List<T> getContent() {
        return content;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
}
