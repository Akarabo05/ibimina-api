package rw.ac.auca.Ibimina_api.controller;

import rw.ac.auca.Ibimina_api.entity.Contribution;
import rw.ac.auca.Ibimina_api.service.ContributionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contributions")
public class ContributionController {

    @Autowired
    private ContributionService contributionService;

    @PostMapping
    public ResponseEntity<Contribution> create(@Valid @RequestBody Contribution contribution) {
        return ResponseEntity.ok(contributionService.create(contribution));
    }

    @GetMapping
    public ResponseEntity<List<Contribution>> getAll() {
        return ResponseEntity.ok(contributionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contribution> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contributionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contribution> update(@PathVariable Long id, @Valid @RequestBody Contribution contribution) {
        return ResponseEntity.ok(contributionService.update(id, contribution));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contributionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}