package com.tuos.Collab.document;

import com.tuos.Collab.collabuser.CollabUser;
import com.tuos.Collab.operation.Operation;
import com.tuos.Collab.operation.OperationKey;
import com.tuos.Collab.styletree.Element;
import com.tuos.Collab.styletree.Leaf;
import com.tuos.Collab.styletree.StyleTree;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import javax.persistence.*;
import java.lang.reflect.Array;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.time.*;


@Entity(name = "Document")
@Table(name = "DOCUMENT")
//@NoArgsConstructor
@Getter
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false)
    Long id;

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    String name;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

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
    List<String> textArray = new ArrayList<String>();

    @Transient
    ArrayList<Operation> historyBuffer = new ArrayList<Operation>();
    @Transient
    HashMap<OperationKey, OperationKey> effectsRelation = new HashMap<OperationKey, OperationKey>();
    @Transient
    int state = 0;
    @Transient
    StyleTree styleTree = new StyleTree();


    //Date lastEdited;

    public Document() {

    }

    public Document(String name) {
        this.name = name;
        this.text = "<p><span></span></p>";
        dateCreated = updateTime();

    }

    public Document(String name, String text) {
        this.name = name;
        this.text = text;
        dateCreated = updateTime();

    }


    public Calendar updateTime() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneId.of("UTC"));
        lastModified = GregorianCalendar.from(zdt);
        return lastModified;
    }

    public int incrementState() {
        return state++;
    }

    public void setText(String text) {
        this.text = text;
        //Initialize style tree and arraylist to work on
        //toStringArraylist(text);
    }

    public StyleTree generateTree(String s) {
        ArrayList<String> textArray = new ArrayList<String>();
        StyleTree styleTree = new StyleTree();
        Element element = new Element();
        Leaf leaf;
        Set<String> formats = Set.of(
                "<strong>", "<em>", "<span>"
        );

        int leafStart = 0;
        for (int i = 0; i < s.length(); i++) {
            String character = "";

//            element = new ArrayList<Leaf>();
            character += s.charAt(i);

            if (character.equals("\\")) {
                i++;
                character += s.charAt(i);
            }

            if (character.equals("<")) {
                i++;
                while (s.charAt(i) != '>') {
                    character += s.charAt(i);
                    i++;
                }
                character += s.charAt(i);


                if (character.equals("<p>")) {
                    element = new Element();
                } else if (character.equals("</p>")) {
                    styleTree.addElement(element);


                } else if (character.charAt(1) != '/') { //Opening tag, since it's <spa.. <str.. <e...
                    leafStart = textArray.size();
                } else { // </strong> or </em>
                    TreeSet<String> types = parseTypes(character);
                    leaf = new Leaf(textArray.size() - leafStart, types);
                    element.addLeaf(leaf);
                }


            } else {
                textArray.add(character);
            }


        }

        this.textArray = textArray;
        this.styleTree = styleTree;
        //return textArray;
        return styleTree;
    }

    private TreeSet<String> parseTypes(String text) {
        text = text.substring(2,text.length()-1);
        TreeSet<String> set = new TreeSet<String>();
        set.addAll(Arrays.asList(text.split("\\+")));
        set.remove("span");
        return set;
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
