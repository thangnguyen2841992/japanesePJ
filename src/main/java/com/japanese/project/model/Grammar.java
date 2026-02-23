package com.japanese.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Grammar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int grammarId;

    private String grammarName;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lesson lesson;

}
