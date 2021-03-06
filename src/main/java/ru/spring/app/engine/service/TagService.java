package ru.spring.app.engine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.spring.app.engine.api.response.SingleTagResponse;
import ru.spring.app.engine.api.response.TagWithCount;
import ru.spring.app.engine.api.response.TagsResponse;
import ru.spring.app.engine.repository.TagRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public TagsResponse getTags(String query) {
        return new TagsResponse(getSingleTagsResponses(query).stream()
                .sorted(Comparator.comparing(SingleTagResponse::getWeight).reversed())
                .filter(t -> t.getWeight() != 0)
                .collect(Collectors.toList()));
    }

    private List<SingleTagResponse> getSingleTagsResponses(String query) {
        List<SingleTagResponse> singleTagResponses = new ArrayList<>();
        Set<TagWithCount> tagWithCounts;
        if (query != null) {
            tagWithCounts = tagRepository.getTagsWithCount().stream().filter(tag ->
                    tag.getTag().contains(query)).collect(Collectors.toSet());
        } else {
            tagWithCounts = tagRepository.getTagsWithCount();
        }
        tagWithCounts.forEach(tag -> singleTagResponses.add(new SingleTagResponse(tag.getTag(),
                getTagWeight(tag.getTag()))));
        return singleTagResponses;
    }

    private Double getTagWeight(String tagName) {
        List<TagWithCount> tags = new ArrayList<>(tagRepository.getTagsWithCount());
        TagWithCount mostPopularTag = tags.get(0);
        TagWithCount currentTag = tags.stream()
                .filter(t -> t.getTag().equalsIgnoreCase(tagName)).findFirst().orElseThrow();
        double tagCount = tags.size();
        long postsCount = tagRepository.getPostsCount();
        double wight = tags.stream().filter(t -> t.getTag().equalsIgnoreCase(currentTag.getTag()))
                .map(TagWithCount::getCount).findFirst().orElseThrow() / tagCount;
        double wightMax = (double) tags.stream().filter(t -> t.getTag().equalsIgnoreCase(mostPopularTag.getTag()))
                .map(TagWithCount::getCount).findFirst().orElseThrow() / postsCount;
        double coefficient = 1 / wightMax;
        return wight * coefficient;
    }
}
