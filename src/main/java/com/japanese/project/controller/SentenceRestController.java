package com.japanese.project.controller;


import com.japanese.project.model.UploadImageHistory;
import com.japanese.project.model.dto.SentenceForm;
import com.japanese.project.service.grammar.IGrammarService;
import com.japanese.project.service.lesson.ILessonService;
import com.japanese.project.service.sentence.ISentenceService;
import com.japanese.project.service.uploadImageHistory.IUploadImageHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/nihongo")
@CrossOrigin("*")
public class SentenceRestController {

    private final ILessonService lessonService;

    private final IGrammarService grammarService;

    private final ISentenceService sentenceService;

    private final IUploadImageHistoryService uploadImageHistoryService;

    public SentenceRestController(ILessonService lessonService, IGrammarService grammarService, ISentenceService sentenceService, IUploadImageHistoryService uploadImageHistoryService) {
        this.lessonService = lessonService;
        this.grammarService = grammarService;
        this.sentenceService = sentenceService;
        this.uploadImageHistoryService = uploadImageHistoryService;
    }

    @PostMapping("/createSentence")
    public ResponseEntity<?> createSentence(@RequestBody SentenceForm form) {
        UploadImageHistory uploadImageHistory = new UploadImageHistory();
        uploadImageHistory.setUrl(form.getImageUrl());
        this.uploadImageHistoryService.save(uploadImageHistory);
        String test = sentenceService.testSentence(form.getImageUrl());
        return new ResponseEntity<>(Collections.singletonMap("result", test), HttpStatus.CREATED);

    }

    @PostMapping("/createLesson")
    public ResponseEntity<?> createLesson(SentenceForm lessonForm) {
        return new ResponseEntity<>("lesson", HttpStatus.CREATED);
    }

}
