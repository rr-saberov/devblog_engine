package ru.spring.app.engine.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagWithCount {
    private String tag;
    private Integer count;
}
