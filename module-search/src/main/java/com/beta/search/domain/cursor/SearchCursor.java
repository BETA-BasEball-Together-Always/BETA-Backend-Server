package com.beta.search.domain.cursor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
public class SearchCursor {

    private Float score; // ES score
    private Long id; // tiebreaker

    public static SearchCursor first() {
        return new SearchCursor(null, null);
    }

    public boolean isFirst() {
        return score == null && id == null;
    }
}
