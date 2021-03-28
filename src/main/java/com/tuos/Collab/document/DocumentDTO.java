package com.tuos.Collab.document;

import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

@Getter
@Setter
public class DocumentDTO {
    private Long id;
    private String name;
    private String owner;
    private Calendar lastModified;

    public DocumentDTO(Long id, String name, String owner, Calendar lastModified) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.lastModified = lastModified;
    }

//    public DocumentDTO(long id) {
//        this.id = id;
//        this.name = "";
//        this.owner = "";
//    }

//    public Long getId() {
//        return id;
//    }
//
//    public String getName() {
//        return name;
//    }
}
