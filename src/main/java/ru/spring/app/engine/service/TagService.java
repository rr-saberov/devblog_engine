package ru.spring.app.engine.service;

import org.springframework.stereotype.Service;
import ru.spring.app.engine.api.response.SingleTagResponse;
import ru.spring.app.engine.api.response.TagWithCount;
import ru.spring.app.engine.api.response.TagsResponse;
import ru.spring.app.engine.entity.Tag;
import ru.spring.app.engine.repository.TagRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public TagsResponse getTags(String query) {
        return new TagsResponse(getSingleTagsResponses(query).stream()
                .sorted(Comparator.comparing(SingleTagResponse::getWeight).reversed())
                .filter(t -> t.getWeight() != 0)
                .collect(Collectors.toList()));
    }

    private List<SingleTagResponse> getSingleTagsResponses(String query) {
        List<SingleTagResponse> singleTagResponses = new ArrayList<>();
        List<TagWithCount> tagWithCounts;
        if (query != null) {
            tagWithCounts = tagRepository.getTagsWithCount().stream().filter(tag ->
                    tag.getTag().contains(query)).collect(Collectors.toList());
        } else {
            tagWithCounts = tagRepository.getTagsWithCount();
        }
        tagWithCounts.forEach(tag -> singleTagResponses.add(new SingleTagResponse(tag.getTag(),
                getTagWeight(tag.getTag()))));
        return singleTagResponses;
    }

    private Double getTagWeight(String tagName) {
        Tag mostPopularTag = tagRepository.getTagsOrderByPopularity().get(0);
        long postsCount = tagRepository.getPostsCount();
        double coefficient =
                1 / ((double) tagRepository.getPostsCountWithTag(mostPopularTag.getName()) / (double) postsCount);
        return ((double) tagRepository.getPostsCountWithTag(tagName) / postsCount * coefficient);
    }
}
