package com.japanese.project.repository;

import com.japanese.project.model.Grammar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IGrammarRepository extends JpaRepository<Grammar, Integer> {
}
