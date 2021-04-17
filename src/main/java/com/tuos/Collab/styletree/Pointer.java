package com.tuos.Collab.styletree;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Pointer {
    private int element;
    private int child;
    private int offset;

    public void shiftChildRight(int offset) {
        child += offset;
    }

    public void shiftOffsetLeft(int offset) {
        this.offset -= offset;
    }
}
