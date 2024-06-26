package yjh.devtoon.comment.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yjh.devtoon.comment.application.CommentService;
import yjh.devtoon.comment.domain.CommentEntity;
import yjh.devtoon.comment.dto.reponse.CommentResponse;
import yjh.devtoon.comment.dto.request.CommentCreateRequest;
import yjh.devtoon.common.response.ApiResponse;

@RequestMapping("/v1/comments")
@RequiredArgsConstructor
@RestController
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse> register(
            @RequestBody @Valid final CommentCreateRequest request
    ) {
        commentService.create(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 댓글 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> retrieve(
            @PathVariable final Long id
    ) {
        CommentEntity comment = commentService.retrieve(id);
        CommentResponse response = CommentResponse.from(comment);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 웹툰의 모든 댓글 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse> retrieveAll(
            @Param("webtoonId") Long webtoonId,
            Pageable pageable
    ) {
        Page<CommentEntity> comments = commentService.retrieveAll(webtoonId, pageable);
        Page<CommentResponse> commentsResponse = comments.map(CommentResponse::from);
        return ResponseEntity.ok(ApiResponse.success(commentsResponse));
    }

}