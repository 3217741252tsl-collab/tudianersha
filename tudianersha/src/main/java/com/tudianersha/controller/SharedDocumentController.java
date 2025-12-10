package com.tudianersha.controller;

import com.tudianersha.entity.SharedDocument;
import com.tudianersha.service.SharedDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shared-documents")
public class SharedDocumentController {
    
    @Autowired
    private SharedDocumentService sharedDocumentService;
    
    @GetMapping
    public ResponseEntity<List<SharedDocument>> getAllSharedDocuments() {
        List<SharedDocument> sharedDocuments = sharedDocumentService.getAllSharedDocuments();
        return new ResponseEntity<>(sharedDocuments, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SharedDocument> getSharedDocumentById(@PathVariable Long id) {
        Optional<SharedDocument> sharedDocument = sharedDocumentService.getSharedDocumentById(id);
        if (sharedDocument.isPresent()) {
            return new ResponseEntity<>(sharedDocument.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<SharedDocument> createSharedDocument(@RequestBody SharedDocument sharedDocument) {
        SharedDocument savedSharedDocument = sharedDocumentService.saveSharedDocument(sharedDocument);
        return new ResponseEntity<>(savedSharedDocument, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SharedDocument> updateSharedDocument(@PathVariable Long id, @RequestBody SharedDocument sharedDocumentDetails) {
        Optional<SharedDocument> sharedDocument = sharedDocumentService.getSharedDocumentById(id);
        if (sharedDocument.isPresent()) {
            SharedDocument existingSharedDocument = sharedDocument.get();
            existingSharedDocument.setProjectId(sharedDocumentDetails.getProjectId());
            existingSharedDocument.setDocumentUrl(sharedDocumentDetails.getDocumentUrl());
            existingSharedDocument.setFormat(sharedDocumentDetails.getFormat());
            existingSharedDocument.setGeneratedTime(sharedDocumentDetails.getGeneratedTime());
            existingSharedDocument.setShareLink(sharedDocumentDetails.getShareLink());
            existingSharedDocument.setCreatorId(sharedDocumentDetails.getCreatorId());
            
            SharedDocument updatedSharedDocument = sharedDocumentService.saveSharedDocument(existingSharedDocument);
            return new ResponseEntity<>(updatedSharedDocument, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteSharedDocument(@PathVariable Long id) {
        Optional<SharedDocument> sharedDocument = sharedDocumentService.getSharedDocumentById(id);
        if (sharedDocument.isPresent()) {
            sharedDocumentService.deleteSharedDocument(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<SharedDocument>> getSharedDocumentsByProjectId(@PathVariable Long projectId) {
        List<SharedDocument> sharedDocuments = sharedDocumentService.getSharedDocumentsByProjectId(projectId);
        return new ResponseEntity<>(sharedDocuments, HttpStatus.OK);
    }
    
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<SharedDocument>> getSharedDocumentsByCreatorId(@PathVariable Long creatorId) {
        List<SharedDocument> sharedDocuments = sharedDocumentService.getSharedDocumentsByCreatorId(creatorId);
        return new ResponseEntity<>(sharedDocuments, HttpStatus.OK);
    }
}