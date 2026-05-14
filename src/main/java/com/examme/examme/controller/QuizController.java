package com.examme.examme.controller;

import com.examme.examme.dto.request.quiz.QuizStartRequestDto;
import com.examme.examme.dto.request.quiz.QuizSubmitRequestDto;
import com.examme.examme.dto.response.quiz.QuizResultDto;
import com.examme.examme.dto.response.quiz.QuizStartResponseDto;
import com.examme.examme.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Quiz")
@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "*", maxAge = 3600)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "Start quiz session")
    @PostMapping("/start")
    public ResponseEntity<QuizStartResponseDto> start(@Valid @RequestBody QuizStartRequestDto body) {
        return ResponseEntity.ok(quizService.start(body));
    }

    @Operation(summary = "Submit quiz answers")
    @PostMapping("/{sessionId}/submit")
    public ResponseEntity<QuizResultDto> submit(
            @PathVariable("sessionId") Long sessionId,
            @Valid @RequestBody QuizSubmitRequestDto body
    ) {
        return ResponseEntity.ok(quizService.submit(sessionId, body));
    }
}
