package com.tudianersha.service;

import com.tudianersha.entity.ProjectTask;
import com.tudianersha.entity.User;
import com.tudianersha.repository.ProjectTaskRepository;
import com.tudianersha.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectTaskService {

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ProjectTask> getTasksByProjectId(Long projectId) {
        List<ProjectTask> tasks = projectTaskRepository.findByProjectIdOrderByCreatedTimeDesc(projectId);
        // 填充用户信息
        for (ProjectTask task : tasks) {
            userRepository.findById(task.getAssigneeId()).ifPresent(task::setAssigneeInfo);
        }
        return tasks;
    }

    public ProjectTask createTask(ProjectTask task) {
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdatedTime(LocalDateTime.now());
        task.setStatus("PENDING");
        ProjectTask saved = projectTaskRepository.save(task);
        userRepository.findById(saved.getAssigneeId()).ifPresent(saved::setAssigneeInfo);
        return saved;
    }

    public ProjectTask updateTask(Long id, String taskName) {
        Optional<ProjectTask> optTask = projectTaskRepository.findById(id);
        if (optTask.isPresent()) {
            ProjectTask task = optTask.get();
            task.setTaskName(taskName);
            task.setUpdatedTime(LocalDateTime.now());
            ProjectTask saved = projectTaskRepository.save(task);
            userRepository.findById(saved.getAssigneeId()).ifPresent(saved::setAssigneeInfo);
            return saved;
        }
        return null;
    }

    public ProjectTask updateTaskStatus(Long id, String status) {
        Optional<ProjectTask> optTask = projectTaskRepository.findById(id);
        if (optTask.isPresent()) {
            ProjectTask task = optTask.get();
            task.setStatus(status);
            task.setUpdatedTime(LocalDateTime.now());
            return projectTaskRepository.save(task);
        }
        return null;
    }

    public ProjectTask addImage(Long id, String imageUrl) {
        Optional<ProjectTask> optTask = projectTaskRepository.findById(id);
        if (optTask.isPresent()) {
            ProjectTask task = optTask.get();
            String urls = task.getImageUrls();
            if (urls == null || urls.isEmpty()) {
                urls = imageUrl;
            } else {
                urls = urls + "," + imageUrl;
            }
            task.setImageUrls(urls);
            task.setUpdatedTime(LocalDateTime.now());
            return projectTaskRepository.save(task);
        }
        return null;
    }

    public ProjectTask removeImage(Long id, int index) {
        Optional<ProjectTask> optTask = projectTaskRepository.findById(id);
        if (optTask.isPresent()) {
            ProjectTask task = optTask.get();
            String urls = task.getImageUrls();
            if (urls != null && !urls.isEmpty()) {
                String[] arr = urls.split(",");
                if (index >= 0 && index < arr.length) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < arr.length; i++) {
                        if (i != index) {
                            if (sb.length() > 0) sb.append(",");
                            sb.append(arr[i]);
                        }
                    }
                    task.setImageUrls(sb.length() > 0 ? sb.toString() : null);
                    task.setUpdatedTime(LocalDateTime.now());
                    return projectTaskRepository.save(task);
                }
            }
        }
        return null;
    }

    public void deleteTask(Long id) {
        projectTaskRepository.deleteById(id);
    }

    public Optional<ProjectTask> getTaskById(Long id) {
        Optional<ProjectTask> optTask = projectTaskRepository.findById(id);
        optTask.ifPresent(task -> 
            userRepository.findById(task.getAssigneeId()).ifPresent(task::setAssigneeInfo)
        );
        return optTask;
    }
}
