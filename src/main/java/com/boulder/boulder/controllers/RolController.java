package com.boulder.boulder.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.entities.Rol;
import com.boulder.boulder.repositories.RolRepository;

@RestController
@RequestMapping("/rol")
public class RolController {

    @Autowired
    private RolRepository rolRepository;

    // Este método responderá al entrar a http://localhost:8080/rol
    @GetMapping
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }
}