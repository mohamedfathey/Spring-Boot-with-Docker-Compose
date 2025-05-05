package com.example.DockerTest.controller;

import com.example.DockerTest.entity.Test;
import com.example.DockerTest.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("test")
public class TestController {
    private final TestService service ;

    @Autowired
    public TestController(TestService service) {
        this.service = service;
    }

    @GetMapping()
    public List<Test>get (){
        return service.gelAllTest();
    }

//    @GetMapping()
//    public String get (){
//        return "service.gelAllTest()";
//    }
//


}
