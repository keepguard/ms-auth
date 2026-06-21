package com.keepguard.ms_auth.application.dto.common;

import java.util.List;

public record PageResultView<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last,
    boolean hasNext,
    boolean hasPrevious
) {

    public List<T> getContent() {
        return content;
    }

    public int getPageNumber() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public boolean hasPrevious() {
        return hasPrevious;
    }

    public int getNumberOfElements() {
        return content != null ? content.size() : 0;
    }
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;

        public Builder<T> content(List<T> content) {
            this.content = content;
            return this;
        }

        public Builder<T> page(int page) {
            this.page = page;
            return this;
        }

        public Builder<T> pageNumber(int pageNumber) {
            this.page = pageNumber;
            return this;
        }

        public Builder<T> size(int size) {
            this.size = size;
            return this;
        }

        public Builder<T> totalElements(long totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public Builder<T> totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public Builder<T> first(boolean first) {
            this.first = first;
            return this;
        }

        public Builder<T> last(boolean last) {
            this.last = last;
            return this;
        }

        public Builder<T> hasNext(boolean hasNext) {
            this.hasNext = hasNext;
            return this;
        }

        public Builder<T> hasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
            return this;
        }

        public Builder<T> numberOfElements(int numberOfElements) {

            return this;
        }

        public PageResultView<T> build() {
            return new PageResultView<>(content, page, size, totalElements, totalPages,
                first, last, hasNext, hasPrevious);
        }
    }
}
