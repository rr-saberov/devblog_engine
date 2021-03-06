package ru.spring.app.engine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.spring.app.engine.api.response.TagsResponse;
import ru.spring.app.engine.service.TagService;

@RestController
@RequiredArgsConstructor
@Tag(name = "tag controller")
public class TagController {

    private final TagService tagService;

    @GetMapping("/api/tag")
    @Operation(summary= "get tags by user response")
    public ResponseEntity<TagsResponse> tags(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(tagService.getTags(query));
    }
}
