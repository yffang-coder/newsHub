package com.newshub.backend.interfaces.rest;

import com.newshub.backend.domain.model.Comment;
import com.newshub.backend.application.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/comments")
@Tag(name = "Comment API", description = "Endpoints for managing comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Operation(summary = "Get comments by article", description = "Returns list of comments for a specific article")
    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<Comment>> getCommentsByArticle(@PathVariable Long articleId) {
        return ResponseEntity.ok(commentService.getCommentsByArticleId(articleId));
    }

    @Operation(summary = "Add comment", description = "Adds a new comment to an article")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Comment> addComment(@RequestBody Comment comment) {
        return ResponseEntity.ok(commentService.addComment(comment));
    }
}

