package org.quebee.com.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkElement {
//    private String name;
    private boolean allTable1;
    private boolean allTable2;
    private boolean custom;

    private String table1;
    private String table2;
//    private UUID table1Id;
//    private UUID table2ID;

    private String condition;

    private String field1;
    private String comparison;
    private String field2;

    public LinkElement() {
//        this.name = "Test";
        this.table1 = "";
        this.table2 = "";
    }

    public LinkElement(String table1, String table2, boolean allTable1, boolean allTable2, boolean custom) {
        setTable1(table1);
        setTable2(table2);
        setAllTable1(allTable1);
        setAllTable2(allTable2);
        setCustom(custom);
        setComparison("=");
    }
}
