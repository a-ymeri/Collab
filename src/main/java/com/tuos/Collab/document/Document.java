package com.tuos.Collab.document;

import com.tuos.Collab.collabuser.CollabUser;
import com.tuos.Collab.operation.Operation;
import com.tuos.Collab.operation.OperationKey;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.time.*;


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
    @JoinColumn(name = "author_id", nullable = false)
    CollabUser author;

    @Column(name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    Calendar dateCreated;

    @Column(name = "last_modified")
    @Temporal(TemporalType.TIMESTAMP)
    Calendar lastModified;

    @ManyToMany(mappedBy = "editableDocuments")
    Set<CollabUser> editors;


    //TODO: reconsider putting in child class
    @Transient
    ArrayList<Operation> historyBuffer = new ArrayList<Operation>();
    @Transient
    HashMap<OperationKey, OperationKey> effectsRelation = new HashMap<OperationKey, OperationKey>();
    @Transient
    int state = 0;



    //Date lastEdited;

    public Document(String name, String text) {
        this.name = name;
        this.text = text;
        dateCreated = updateTime();
    }

    public Calendar updateTime(){
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneId.of("UTC"));
        lastModified = GregorianCalendar.from(zdt);
        return lastModified;
    }

    public int incrementState() {
        return state++;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setAuthor(CollabUser author) {
        this.author = author;
    }

    public String getAuthorName() {
        return author.getName();
    }

    public boolean addEditor(CollabUser editor) {
        return editors.add(editor);
    }
}
