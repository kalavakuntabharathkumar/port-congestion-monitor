package com.example.portmonitor.controller;

import com.example.portmonitor.entity.Port;
import com.example.portmonitor.repository.PortRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ports")
public class PortController {
    private final PortRepository portRepository;
    public PortController(PortRepository portRepository) { this.portRepository = portRepository; }
    @GetMapping
    public List<Port> getAllPorts() { return portRepository.findAllByOrderByPortNameAsc(); }
    @GetMapping("/{id}")
    public Port getPort(@PathVariable Long id) { return portRepository.findById(id).orElseThrow(() -> new PortNotFoundException(id)); }
    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    public static class PortNotFoundException extends RuntimeException { public PortNotFoundException(Long id) { super("Port not found: " + id); } }
}