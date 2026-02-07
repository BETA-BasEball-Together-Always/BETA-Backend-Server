package com.beta.search.domain.service;

import com.beta.search.domain.document.UserDocument;
import com.beta.search.domain.cursor.SearchCursor;
import com.beta.search.infra.repository.SearchUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchUserService {

    private final SearchUserRepository searchUserRepository;

    public List<SearchHit<UserDocument>> search(String keyword, SearchCursor cursor, int size) {
        return searchUserRepository.searchByKeyword(keyword, cursor, size);
    }

    public List<SearchHit<UserDocument>> searchWhileTyping(String keyword, SearchCursor cursor, int size) {
        return searchUserRepository.searchByNicknamePrefix(keyword, cursor, size);
    }
}
