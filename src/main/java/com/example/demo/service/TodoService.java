package com.example.demo.service;

import com.example.demo.model.AppUser;
import com.example.demo.model.Todo;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.TodoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final AppUserRepository userRepository;

    public TodoService(TodoRepository todoRepository, AppUserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    private boolean isAdmin(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null)
            return null;
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    public List<Todo> getAllTodos() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && isAdmin(auth.getAuthorities())) {
            return todoRepository.findAll();
        } else {
            AppUser user = currentUser();
            return (user == null) ? List.of() : todoRepository.findByOwner(user);
        }
    }

    public Optional<Todo> getTodoById(Long id) {
        Optional<Todo> todoOpt = todoRepository.findById(id);
        if (todoOpt.isEmpty())
            return Optional.empty();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && isAdmin(auth.getAuthorities())) {
            return todoOpt;
        }
        AppUser user = currentUser();
        return (user != null && todoOpt.get().getOwner() != null
                && todoOpt.get().getOwner().getId().equals(user.getId()))
                        ? todoOpt
                        : Optional.empty();
    }

    public Todo createTodo(Todo todo) {
        AppUser user = currentUser();
        if (user == null)
            throw new RuntimeException("Unauthenticated");
        todo.setOwner(user);
        return todoRepository.save(todo);
    }

    public Optional<Todo> updateTodo(Long id, Todo updates) {
        Optional<Todo> todoOpt = todoRepository.findById(id);
        if (todoOpt.isEmpty())
            return Optional.empty();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Todo existing = todoOpt.get();
        if (!(auth != null && isAdmin(auth.getAuthorities()))) {
            AppUser user = currentUser();
            if (user == null || existing.getOwner() == null || !existing.getOwner().getId().equals(user.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("Forbidden");
            }
        }
        existing.setTitle(updates.getTitle());
        existing.setCompleted(updates.isCompleted());
        return Optional.of(todoRepository.save(existing));
    }

    public boolean deleteTodo(Long id) {
        Optional<Todo> todoOpt = todoRepository.findById(id);
        if (todoOpt.isEmpty())
            return false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Todo existing = todoOpt.get();
        if (!(auth != null && isAdmin(auth.getAuthorities()))) {
            AppUser user = currentUser();
            if (user == null || existing.getOwner() == null || !existing.getOwner().getId().equals(user.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("Forbidden");
            }
        }
        todoRepository.deleteById(id);
        return true;
    }
}
