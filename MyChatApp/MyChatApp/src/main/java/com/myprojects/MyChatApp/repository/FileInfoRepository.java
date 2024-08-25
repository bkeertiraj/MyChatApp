package com.myprojects.MyChatApp.repository;

import com.myprojects.MyChatApp.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
}
