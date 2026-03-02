package com.japanese.project.service.uploadImageHistory;

import com.japanese.project.model.UploadImageHistory;
import com.japanese.project.repository.IUploadImageHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UploadImageHistoryServiceImpl implements IUploadImageHistoryService {
    private final IUploadImageHistoryRepository uploadImageHistoryRepository;

    public UploadImageHistoryServiceImpl(IUploadImageHistoryRepository uploadImageHistoryRepository) {
        this.uploadImageHistoryRepository = uploadImageHistoryRepository;
    }

    @Override
    public UploadImageHistory save(UploadImageHistory uploadImageHistory) {
        uploadImageHistory.setDateCreated(new Date());
        return this.uploadImageHistoryRepository.save(uploadImageHistory);
    }
}
