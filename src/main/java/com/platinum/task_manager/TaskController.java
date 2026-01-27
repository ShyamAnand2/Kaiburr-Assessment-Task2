package com.platinum.task_manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "http://localhost:3000")
public class TaskController {
    
    @Autowired
    private TaskRepository repo;

    @GetMapping
    public List<Task> allTasks(@RequestParam(required = false) String owner) { 
        if (owner != null && !owner.isEmpty()) {
            return repo.findByOwner(owner);
        }
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Task get(@PathVariable String id) { 
        return repo.findById(id).orElse(null); 
    }

    @PostMapping
    public Task create(@RequestBody Task t) { 
        if (t.getTaskExecutions() == null) {
            t.setTaskExecutions(new ArrayList<>());
        }
        return repo.save(t); 
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable String id, @RequestBody Task t) {
        return repo.findById(id).map(task -> {
            task.setName(t.getName());
            task.setOwner(t.getOwner());
            task.setCommand(t.getCommand());
            return repo.save(task);
        }).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Task not found: " + id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) { 
        repo.deleteById(id); 
    }

    @GetMapping("/search")
    public List<Task> search(@RequestParam String name) {
        List<Task> all = repo.findAll();
        List<Task> result = new ArrayList<>();
        for (Task t : all) {
            if (t.getName() != null && t.getName().contains(name)) {
                result.add(t);
            }
        }
        return result;
    }

    @PutMapping("/{id}/execute")
    public Task execute(@PathVariable String id) {
        Task t = repo.findById(id).orElse(null);
        if (t == null) {
            System.err.println("Task not found: " + id);
            return null;
        }

        try {
            String cmd = t.getCommand();
            System.out.println("EXECUTE - Command: " + cmd);
            
            if (cmd != null && cmd.startsWith("echo ")) {
                long start = System.currentTimeMillis();
                
                Process process = Runtime.getRuntime().exec(cmd);
                process.waitFor();
                
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
                );
                
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                
                long end = System.currentTimeMillis();

                TaskExecution exec = new TaskExecution();
                exec.setStartTime(new Date(start));
                exec.setEndTime(new Date(end));
                exec.setOutput(output.toString());

                if (t.getTaskExecutions() == null) {
                    t.setTaskExecutions(new ArrayList<>());
                }
                t.getTaskExecutions().add(exec);
                
                repo.save(t);
                System.out.println("Task execution saved: " + output.toString());
            } else {
                System.err.println("Command does not start with echo");
            }
        } catch (Exception e) {
            System.err.println("ERROR during execution:");
            e.printStackTrace();
        }

        return repo.findById(id).orElse(null);
    }
}
