package com.tuos.Collab.document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentDTO {
    private Long id;
    private String name;
    private String owner;

    public DocumentDTO(Long id, String name, String owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }

//    public Long getId() {
//        return id;
//    }
//
//    public String getName() {
//        return name;
//    }
}
