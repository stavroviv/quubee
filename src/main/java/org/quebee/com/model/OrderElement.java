package org.quebee.com.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderElement extends IconableElement {
    private String field;
    private String sorting;
}
