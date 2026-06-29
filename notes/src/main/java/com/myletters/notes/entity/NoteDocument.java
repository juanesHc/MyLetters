package com.myletters.notes.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;



@Getter
@Setter
@Document(collection = "notes")
public class NoteDocument extends BaseDocument {

    private UUID ownerId;

    private String title;

    private String content;
}
