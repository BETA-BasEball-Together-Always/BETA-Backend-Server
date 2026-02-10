package com.beta.search.domain.cursor;

import com.beta.core.exception.search.InvalidCursorException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchCursor {

    private Float score; // ES score
    private Long id; // tiebreaker

    public static SearchCursor of(Float score, Long id) {
        if ((score == null) != (id == null)) {
            throw new InvalidCursorException();
        }
        return new SearchCursor(score, id);
    }

    public static SearchCursor first() {
        return new SearchCursor(null, null);
    }

    public boolean isFirst() {
        return score == null && id == null;
    }
}
