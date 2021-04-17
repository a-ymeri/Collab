package com.tuos.Collab.operation;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OperationDTO {
    String type;
    int[] path;
    int offset;
    String text;
    long siteID;
    int stateID;

    public Operation toOperation(){
        return new Operation(text,offset,siteID,stateID,type);
    }
}
