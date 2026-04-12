package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {

}