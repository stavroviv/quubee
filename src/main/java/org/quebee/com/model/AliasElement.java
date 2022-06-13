package org.quebee.com.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class AliasElement {
    private String aliasName;
    private String tableName;
    private Map<String, String> alias = new HashMap<>();

    public AliasElement() {
    }

    public void putAlias(String union, String value) {
        alias.put(union, value);
    }
}
