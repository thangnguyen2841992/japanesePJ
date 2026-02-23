package com.japanese.project.controller;


import com.japanese.project.model.dto.SentenceForm;
import com.japanese.project.service.grammar.IGrammarService;
import com.japanese.project.service.lesson.ILessonService;
import com.japanese.project.service.sentence.ISentenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nihongo")
public class SentenceRestController {

    private final ILessonService lessonService;

    private final IGrammarService grammarService;

    private final ISentenceService sentenceService;

    public SentenceRestController(ILessonService lessonService, IGrammarService grammarService, ISentenceService sentenceService) {
        this.lessonService = lessonService;
        this.grammarService = grammarService;
        this.sentenceService = sentenceService;
    }

    @PostMapping("/createSentence")
    public ResponseEntity<?> createSentence(SentenceForm sentenceForm) {
        return new ResponseEntity<>("sentence", HttpStatus.CREATED);
    }

    @PostMapping("/createLesson")
    public ResponseEntity<?> createLesson(SentenceForm lessonForm) {
        return new ResponseEntity<>("lesson", HttpStatus.CREATED);
    }

}
