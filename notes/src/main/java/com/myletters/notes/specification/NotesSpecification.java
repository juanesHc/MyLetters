package com.myletters.notes.specification;

import com.myletters.notes.dto.request.RetrieveNotesFilterRequestDto;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.UUID;
import java.util.regex.Pattern;


public final class NotesSpecification {

    private NotesSpecification() {
    }

    public static Query build(UUID ownerId, RetrieveNotesFilterRequestDto filter) {

        Criteria criteria = Criteria.where("ownerId").is(ownerId);

        if (hasText(filter.title())) {
            criteria.and("title").regex(Pattern.quote(filter.title().trim()), "i");
        }

        return new Query(criteria);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
