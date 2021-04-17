package com.tuos.Collab.operation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StyleOperationDTO {
    String type;
    int[] path;
    int[] endPath;
    int offset;
    int endOffset;
    String text;
    long siteID;
    int stateID;

    @JsonIgnore
    public Operation getStartingTag() {
        int startingOffset = Math.min(offset, endOffset);
        String tagText = text;
        if(text.equals("italic")){
            tagText = "em";
        }
        if(text.equals("bold")){
            tagText = "strong";
        }
        return new Operation(tagText, startingOffset, siteID, stateID, type);
    }

    @JsonIgnore
    public Operation getEndingTag() {
        int endingOffset = Math.max(offset, endOffset);
        String tagText = text;
        if(text.equals("italic")){
            tagText = "em";
        }
        if(text.equals("bold")){
            tagText = "strong";
        }
        return new Operation(tagText, endingOffset, siteID, stateID, type);
    }

    public void setText(String text) {
        this.text = text;
    }
}
