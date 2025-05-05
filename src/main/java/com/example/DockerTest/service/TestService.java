package com.example.DockerTest.service;

import com.example.DockerTest.entity.Test;
import com.example.DockerTest.repo.TestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {
    private final TestRepo repo ;

    @Autowired
    public TestService(TestRepo repo) {
        this.repo = repo;
    }


    public List<Test> gelAllTest(){
        return repo.findAll();
    }
}
