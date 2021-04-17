package com.tuos.Collab.styletree;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Getter
@Setter
@NoArgsConstructor
public class Leaf {
    private int size;
    private SortedSet<String> types;

    public Leaf(int size, SortedSet<String> types) {
        this.size = size;
        this.types = types;
    }

    public Leaf(int size, String type) {
        this.size = size;
        this.types = new TreeSet<>();
        types.add(type);
    }

    public int getSize() {
        return size;
    }

    public void increaseSize(int increase) {
        size += increase;
    }

    public void decreaseSize(int decrease) {
        size -= decrease;
    }

    public void addType(String type) {
        types.add(type);
    }

    public void removeType(String type) {
        types.remove(type);
    }

    @JsonIgnore
    public String getTypesAsString() {
        if (types.size() == 0) {
            return "span";
        } else {
            String text = "";
            for (String type : types) {
                text += type + "+";
            }
            text = text.substring(0, text.length() - 1);
            return text;
        }
    }

    public void merge(Leaf otherNode) {
        size += otherNode.getSize();
    }

//    @JsonIgnore
//    public String getOpeningTag(){
//        return "<"+types.spliterator()+">";
//    }
//
//    @JsonIgnore
//    public String getClosingTag(){
//        return "</"+type+">";
//    }
}
