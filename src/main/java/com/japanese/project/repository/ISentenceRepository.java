package com.japanese.project.repository;

import com.japanese.project.model.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISentenceRepository extends JpaRepository<Sentence,Integer> {
}
