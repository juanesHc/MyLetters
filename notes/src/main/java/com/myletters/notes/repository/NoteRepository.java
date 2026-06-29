package com.myletters.notes.repository;

import com.myletters.notes.entity.NoteDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends MongoRepository<NoteDocument, UUID> {

    boolean existsByOwnerIdAndTitle(UUID ownerId, String title);

    Optional<NoteDocument> findByIdAndOwnerId(UUID id, UUID ownerId);


    List<NoteDocument> findByOwnerId(UUID ownerId);

    void deleteByIdAndOwnerId(UUID noteId,UUID ownerId);
}
