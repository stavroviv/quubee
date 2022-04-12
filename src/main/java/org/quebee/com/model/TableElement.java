package org.quebee.com.model;

import java.util.UUID;

public class TableElement {

    private UUID id;
    private String name;
    private String alias;

    private boolean root;
    private boolean notSelectable;
    private boolean nested;
    private boolean cte;
    private boolean cteRoot;


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public boolean isNotSelectable() {
        return notSelectable;
    }

    public void setNotSelectable(boolean notSelectable) {
        this.notSelectable = notSelectable;
    }

    public boolean isNested() {
        return nested;
    }

    public void setNested(boolean nested) {
        this.nested = nested;
    }

    public boolean isCte() {
        return cte;
    }

    public void setCte(boolean cte) {
        this.cte = cte;
    }

    public boolean isCteRoot() {
        return cteRoot;
    }

    public void setCteRoot(boolean cteRoot) {
        this.cteRoot = cteRoot;
    }
}
