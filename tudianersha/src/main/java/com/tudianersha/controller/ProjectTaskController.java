package com.tudianersha.controller;

import com.tudianersha.entity.ProjectTask;
import com.tudianersha.service.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/project-tasks")
public class ProjectTaskController {

    @Autowired
    private ProjectTaskService projectTaskService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ProjectTask>> getTasksByProject(@PathVariable Long projectId) {
        List<ProjectTask> tasks = projectTaskService.getTasksByProjectId(projectId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<ProjectTask> createTask(@RequestBody ProjectTask task) {
        ProjectTask created = projectTaskService.createTask(task);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectTask> updateTask(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        String taskName = updates.get("taskName");
        if (taskName != null) {
            ProjectTask updated = projectTaskService.updateTask(id, taskName);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ProjectTask> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        String status = updates.get("status");
        if (status != null) {
            ProjectTask updated = projectTaskService.updateTaskStatus(id, status);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        projectTaskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<?> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Optional<ProjectTask> optTask = projectTaskService.getTaskById(id);
        if (optTask.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ProjectTask task = optTask.get();
        
        try {
            // 创建上传目录
            String taskUploadDir = uploadDir + "/tasks/" + task.getProjectId() + "/" + id;
            Path uploadPath = Paths.get(taskUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 保存URL到数据库
            String imageUrl = "/uploads/tasks/" + task.getProjectId() + "/" + id + "/" + newFilename;
            ProjectTask updated = projectTaskService.addImage(id, imageUrl);

            return ResponseEntity.ok(Map.of("imageUrl", imageUrl, "task", updated));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "文件上传失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/images/{index}")
    public ResponseEntity<ProjectTask> deleteImage(@PathVariable Long id, @PathVariable int index) {
        ProjectTask updated = projectTaskService.removeImage(id, index);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectTask> getTask(@PathVariable Long id) {
        Optional<ProjectTask> optTask = projectTaskService.getTaskById(id);
        return optTask.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
