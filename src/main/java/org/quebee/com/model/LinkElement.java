package org.quebee.com.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LinkElement {
    private String name;
    private boolean allTable1;
    private boolean allTable2;
    private boolean custom;

    private String table1;
    private String table2;
    private UUID table1Id;
    private UUID table2ID;

    private String condition;

    private String field1;
    private String comparison;
    private String field2;

    public LinkElement() {
        this.name = "Test";
        this.table1 = "";
        this.table2 = "";
    }
}
