package com.tudianersha.service;

import com.tudianersha.entity.SharedDocument;
import com.tudianersha.repository.SharedDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SharedDocumentService {
    
    @Autowired
    private SharedDocumentRepository sharedDocumentRepository;
    
    public List<SharedDocument> getAllSharedDocuments() {
        return sharedDocumentRepository.findAll();
    }
    
    public Optional<SharedDocument> getSharedDocumentById(Long id) {
        return sharedDocumentRepository.findById(id);
    }
    
    public SharedDocument saveSharedDocument(SharedDocument sharedDocument) {
        return sharedDocumentRepository.save(sharedDocument);
    }
    
    public void deleteSharedDocument(Long id) {
        sharedDocumentRepository.deleteById(id);
    }
    
    public List<SharedDocument> getSharedDocumentsByProjectId(Long projectId) {
        return sharedDocumentRepository.findByProjectId(projectId);
    }
    
    public List<SharedDocument> getSharedDocumentsByCreatorId(Long creatorId) {
        return sharedDocumentRepository.findByCreatorId(creatorId);
    }
}