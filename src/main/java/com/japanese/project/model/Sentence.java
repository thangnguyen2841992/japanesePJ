package com.japanese.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Sentence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int sentenceId;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Grammar grammar;

    private Date dateCreated;
}
