package com.example.DockerTest.repo;

import com.example.DockerTest.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepo extends JpaRepository<Test,Long> {
}
