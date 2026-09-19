package rw.ac.auca.Ibimina_api.controller;

import rw.ac.auca.Ibimina_api.entity.Group;
import rw.ac.auca.Ibimina_api.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public ResponseEntity<Group> create(@Valid @RequestBody Group group) {
        return ResponseEntity.ok(groupService.create(group));
    }

    @GetMapping
    public ResponseEntity<List<Group>> getAll() {
        return ResponseEntity.ok(groupService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Group> update(@PathVariable Long id, @Valid @RequestBody Group group) {
        return ResponseEntity.ok(groupService.update(id, group));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        groupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}