package com.tuos.Collab.document;

import com.tuos.Collab.collabuser.CollabUser;
import com.tuos.Collab.operation.Operation;
import com.tuos.Collab.operation.OperationKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;


@Entity(name = "Document")
@Table(name = "DOCUMENT")
@NoArgsConstructor
@Getter
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false)
    Long id;

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    String name;

    @Column(name = "text", columnDefinition = "TEXT")
    String text;

    @ManyToOne
    @JoinColumn(name="author_id", nullable = false)
    CollabUser author;

    //Set<CollabUser> editors;

    //TODO: reconsider putting in child class
    @Transient
    ArrayList<Operation> historyBuffer = new ArrayList<Operation>();
    @Transient
    HashMap<OperationKey, OperationKey> effectsRelation = new HashMap<OperationKey,OperationKey>();
    @Transient
    int state = 0;

    //Date creationTime;
    //Date lastEdited;

    public Document(String name, String text) {
        this.name = name;
        this.text = text;
        effectsRelation = new HashMap<OperationKey, OperationKey>();
        historyBuffer = new ArrayList<Operation>();
        state = 0;
    }

    public int incrementState(){
        return state++;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setAuthor(CollabUser author) {
        this.author = author;
    }

    public String getAuthorName(){
        return author.getName();
    }
}
