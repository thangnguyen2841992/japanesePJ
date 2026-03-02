package com.japanese.project.repository;

import com.japanese.project.model.UploadImageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUploadImageHistoryRepository extends JpaRepository<UploadImageHistory, Integer> {
}
