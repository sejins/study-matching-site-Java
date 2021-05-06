package com.sejin.tag;

import com.sejin.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class TagService {

    private final TagRepository tagRepository;

    public Tag findOrCreateNew(String tagTitle){
        Tag tag = tagRepository.findByTitle(tagTitle);
        if(tag == null){
            tag = tagRepository.save(Tag.builder().title(tagTitle).build());
        }
        // tag가 없으면 DE에 새로 만들어서 account와 관계를 맺고, 이미 존재하면 존재하는 객체로 account와 관계를 맺는다.

        return tag;
    }
}
