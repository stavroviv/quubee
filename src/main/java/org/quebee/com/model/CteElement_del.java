package org.quebee.com.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CteElement_del  {
    private String name;

    public CteElement_del(String name) {
        this.name = name;
    }
}
