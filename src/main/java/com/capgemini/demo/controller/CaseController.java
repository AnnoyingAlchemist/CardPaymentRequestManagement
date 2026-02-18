package com.capgemini.demo.controller;

import com.capgemini.demo.casefacade.CaseFacade;
import com.capgemini.demo.service.CaseService;
//import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cases")
public class CaseController {

    private final CaseService service;

    public CaseController(CaseService service) {
        this.service = service;
    }

    @PostMapping
    public CaseFacade createCase(@RequestBody CaseFacade c) {
        return service.createCase(c);
    }

    @GetMapping("/{id}")
    public CaseFacade getCase(@PathVariable Long id) {
        return service.getCase(id);
    }

    @GetMapping
    public List<CaseFacade> all() {
        return service.getAllCases();
    }

    @PutMapping("/{id}")
    public CaseFacade updateCase(
            @PathVariable Long id,
            @RequestBody CaseFacade updatedCase) {
        return service.updateCase(id, updatedCase);
    }

    @DeleteMapping("/{id}")
    public void deleteCase(@PathVariable Long id) {
        service.deleteCase(id);
    }
}
