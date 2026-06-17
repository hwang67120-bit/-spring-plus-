package org.example.expert.domain.log.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long requesterId;
    private Long todoId;
    private Long managerUserId;
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Log(Long requesterId, Long todoId, Long managerUserId, String message) {
        this.requesterId = requesterId;
        this.todoId = todoId;
        this.managerUserId = managerUserId;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}
