package com.platinum.task_manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.util.Config;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private TaskRepository repo;

    @GetMapping
    public List<Task> allTasks() { 
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
        t.setId(id);
        return repo.save(t);
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

    @SuppressWarnings("null")
    @PutMapping("/{id}/execute")
    public Task execute(@PathVariable String id) {
        Task t = repo.findById(id).orElse(null);
        if (t == null) {
            System.err.println("Task not found: " + id);
            return null;
        }

        try {
            String cmd = t.getCommand();
            System.out.println("KUBERNETES EXECUTE - Command: " + cmd);
            
            if (cmd != null && cmd.startsWith("echo ")) {
                long start = System.currentTimeMillis();
                
                System.out.println("Initializing Kubernetes client...");
                ApiClient client = Config.defaultClient();
                Configuration.setDefaultApiClient(client);
                CoreV1Api api = new CoreV1Api();
                
                String podName = "command-pod-" + System.currentTimeMillis();
                System.out.println("Creating pod: " + podName);
                
                V1Pod pod = new V1Pod();
                V1ObjectMeta metadata = new V1ObjectMeta();
                metadata.setName(podName);
                pod.setMetadata(metadata);
                
                V1PodSpec spec = new V1PodSpec();
                spec.setRestartPolicy("Never");
                
                V1Container container = new V1Container();
                container.setName("busybox");
                container.setImage("busybox");
                container.setCommand(Arrays.asList("sh", "-c", cmd));
                
                spec.setContainers(Arrays.asList(container));
                pod.setSpec(spec);
                
                api.createNamespacedPod("default", pod, null, null, null, null);
                System.out.println("Pod created successfully");
                
                // Wait for pod to complete
                System.out.println("Waiting for pod to complete...");
                int maxWait = 30;
                for (int i = 0; i < maxWait; i++) {
                    Thread.sleep(1000);
                    V1Pod podStatus = api.readNamespacedPod(podName, "default", null);
                    String phase = podStatus.getStatus().getPhase();
                    System.out.println("Pod status: " + phase);
                    
                    if ("Succeeded".equals(phase) || "Failed".equals(phase)) {
                        break;
                    }
                }
                
                System.out.println("Reading pod logs...");
                String output = api.readNamespacedPodLog(
                    podName, 
                    "default", 
                    null, 
                    Boolean.FALSE, 
                    Boolean.FALSE, 
                    null, 
                    null, 
                    Boolean.FALSE, 
                    null, 
                    null, 
                    Boolean.FALSE
                );
                System.out.println("Pod output: " + output);
                
                System.out.println("Deleting pod...");
                api.deleteNamespacedPod(
                    podName, "default", null, null, null, null, null, null
                );
                
                long end = System.currentTimeMillis();

                TaskExecution exec = new TaskExecution();
                exec.setStartTime(new Date(start));
                exec.setEndTime(new Date(end));
                exec.setOutput(output);

                if (t.getTaskExecutions() == null) {
                    t.setTaskExecutions(new ArrayList<>());
                }
                t.getTaskExecutions().add(exec);
                
                repo.save(t);
                System.out.println("Task execution saved");
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
