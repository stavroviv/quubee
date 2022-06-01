package org.quebee.com.qpart;

import lombok.SneakyThrows;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.quebee.com.model.LinkElement;

import java.util.*;

import static org.quebee.com.util.Constants.*;

public class FullQuery {

    private final Map<String, OneCte> cteMap = new LinkedHashMap<>();

    public FullQuery(Statement statement) {
        if (!(statement instanceof Select)) {
            throw new IllegalStateException("Only select queries supported");
        }

        Select select = (Select) statement;
        List<WithItem> withItemsList = select.getWithItemsList();
        if (Objects.nonNull(withItemsList)) {
            int i = 0;
            for (WithItem x : withItemsList) {
                SubSelect subSelect = x.getSubSelect();
                addCte(x.getName(), subSelect.getSelectBody(), i++);
            }
            addCte(BODY, select.getSelectBody(), i);
        } else {
            addCte(CTE_0, select.getSelectBody(), 0);
        }
    }

    public OneCte getCte(String cte) {
        return cteMap.get(cte);
    }

    public Set<String> getCteNames() {
        return cteMap.keySet();
    }

    public void addCte(String cteName, SelectBody selectBody, int order) {
        cteMap.put(cteName, new OneCte(cteName, selectBody, order));
    }

    public String getFirstCte() {
        return cteMap.entrySet().iterator().next().getKey();
    }

    public static <T extends Orderable> Map<String, T> sortByOrder(Map<String, T> map) {
        List<Map.Entry<String, T>> list = new LinkedList<>(map.entrySet());

        list.sort(Comparator.comparing(o -> o.getValue().getOrder()));

        Map<String, T> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, T> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public String getFullSelectText() {
        Select select = new Select();

        List<WithItem> withItems = new ArrayList<>();
        Map<String, OneCte> cteMapSort = sortByOrder(cteMap);
        Iterator<String> iterator = cteMapSort.keySet().iterator();
        String cte;
        int index = 0;

        while (iterator.hasNext()) {
            cte = iterator.next();
            if (index == cteMap.size() - 1) {
                select.setSelectBody(getSelectBody(cte));
                break;
            }
            WithItem cteBody = new WithItem();
            cteBody.setName(cteMap.get(cte).getCteName());
            SubSelect subSelect = new SubSelect();
            subSelect.setSelectBody(getSelectBody(cte));
            cteBody.setSubSelect(subSelect);
            withItems.add(cteBody);

            index++;
        }
        if (!withItems.isEmpty()) {
            select.setWithItemsList(withItems);
        }

        return select.toString().equals(EMPTY_SELECT) ? "" : select.toString();
    }

    private SelectBody getSelectBody(String cte) {
        SetOperationList selectBody = new SetOperationList();
        OneCte oneCte = cteMap.get(cte);

        Map<String, Union> unionMapSort = sortByOrder(oneCte.getUnionMap());
        Iterator<String> iterator = unionMapSort.keySet().iterator();
        String union;

        List<SetOperation> ops = new ArrayList<>();
        List<Boolean> brackets = new ArrayList<>();
        List<SelectBody> selectBodies = new ArrayList<>();
        boolean first = true;
        while (iterator.hasNext()) {
            union = iterator.next();
            if (oneCte.getUnionMap().size() == 1) {
                return getPlainSelect(oneCte, union, first, true);
            }
            brackets.add(false);
            UnionOp unionOp = new UnionOp();
            unionOp.setAll(!isDistinct(oneCte, union));
            ops.add(unionOp);
            selectBodies.add(getPlainSelect(oneCte, union, first, !iterator.hasNext()));
            first = false;
        }
        ops.remove(ops.size() - 1);
        selectBody.setBracketsOpsAndSelects(brackets, selectBodies, ops);

        return selectBody;
    }

    private boolean isDistinct(OneCte oneCte, String union) {
//        ObservableList<TableRow> items = oneCte.getUnionTable().getItems();
//        if (union.equals(items.get(items.size() - 1).getName())) {
//            return false;
//        }
//        int index = 0;
//        for (TableRow item : items) {
//            if (item.getName().equals(union)) {
//                return items.get(index + 1).isDistinct();
//            }
//            index++;
//        }
        return false;
    }

    private PlainSelect getPlainSelect(OneCte cte, String union, boolean first, boolean last) {
        PlainSelect select = new PlainSelect();

        saveAliases(select, cte, union, first);
        saveFromTables(select, cte, union);
        saveLinks(select, cte, union);
        saveGroupBy(select, cte, union);
        saveConditions(select, cte, union);
        if (last) {
            saveOrderBy(select, cte);
        }

        return select;
    }

    public static void saveLinks(PlainSelect selectBody, OneCte cte, String union) {
        Union union1 = cte.getUnionMap().get(union);
//        TableView<LinkElement> linkTable = union1.getLinkTable();
//        if (linkTable.getItems().isEmpty()) {
//            return;
//        }
//
//        String tableFrom = linkTable.getItems().get(0).getTable1();
//        selectBody.setFromItem(new Table(tableFrom));
//        List<Join> joins = new ArrayList<>();
//        for (LinkElement item : linkTable.getItems()) {
//            Join join = new Join();
//            join.setRightItem(new Table(item.getTable2()));
//            setJoinType(item, join);
//            setCondition(item, join);
//            joins.add(join);
//        }

//        selectBody.setJoins(joins);
    }

    private static void setCondition(LinkElement item, Join join) {
//        String strExpression = item.getCondition();
//        if (!item.isCustom()) {
//            strExpression = item.getTable1() + "." + item.getField1()
//                    + item.getExpression()
//                    + item.getTable2() + "." + item.getField2();
//        }
//        try {
//            join.setOnExpression(getExpression(strExpression));
//        } catch (JSQLParserException e) {
//            e.printStackTrace();
//        }
    }

    private static void setJoinType(LinkElement item, Join join) {
//        if (item.isAllTable1() && item.isAllTable2()) {
//            join.setFull(true);
//        } else if (item.isAllTable1()) {
//            join.setLeft(true);
//        } else if (item.isAllTable2()) {
//            join.setRight(true);
//        } else {
//            join.setInner(true);
//        }
    }

    @SneakyThrows
    private static Expression getExpression(String where) {
        Statement stmt = CCJSqlParserUtil.parse(
                "SELECT * FROM TABLES WHERE " + where
        );
        Select select = (Select) stmt;
        return ((PlainSelect) select.getSelectBody()).getWhere();
    }

    private static void saveConditions(PlainSelect selectBody, OneCte cte, String unionName) {
//        Union union = cte.getUnionMap().get(unionName);
//        TableView<ConditionElement> conditionTableResults = union.getConditionTableResults();
//        if (conditionTableResults.getItems().size() == 0) {
//            selectBody.setWhere(null);
//            return;
//        }
//        StringBuilder where = new StringBuilder();
//        for (ConditionElement item : conditionTableResults.getItems()) {
//            String whereExpr = item.getCondition();
//            if (whereExpr.isEmpty()) {
//                whereExpr = item.getLeftExpression() + item.getExpression() + item.getRightExpression();
//            }
//            where.append(whereExpr).append(" AND ");
//        }
//        Statement stmt = null;
//        try {
//            stmt = CCJSqlParserUtil.parse(
//                    "SELECT * FROM TABLES WHERE " + where.substring(0, where.length() - 4)
//            );
//        } catch (JSQLParserException e) {
//            e.printStackTrace();
//        }
//        Select select = (Select) stmt;
//        Expression whereExpression = ((PlainSelect) select.getSelectBody()).getWhere();
//        selectBody.setWhere(whereExpression);
    }

    private static void saveGroupBy(PlainSelect select, OneCte cte, String union) {
//        Union union1 = cte.getUnionMap().get(union);
//        TableView<TableRow> groupTableResults = union1.getGroupTableResults();
//        if (groupTableResults.getItems().isEmpty()) {
//            return;
//        }
//        //  if (groupTableResults.getItems().isEmpty() && groupTableAggregates.getItems().isEmpty()) {
//        if (groupTableResults.getItems().isEmpty()) {
//            select.setGroupByElement(null);
//            return;
//        }
//        List<Expression> expressions = new ArrayList<>();
//        for (TableRow item : groupTableResults.getItems()) {
//            Column groupByItem = new Column(item.getName());
//            expressions.add(groupByItem);
//        }
//        for (TreeItem<TableRow> child : union1.getGroupFieldsTree().getRoot().getChildren()) {
//            if (child.getValue().getName().equals(ALL_FIELDS)) {
//                break;
//            }
//            Column groupByItem = new Column(child.getValue().getName());
//            expressions.add(groupByItem);
//        }
//
//        if (expressions.isEmpty()) {
//            select.setGroupByElement(null);
//            return;
//        }
//        GroupByElement groupByElement = new GroupByElement();
//        groupByElement.setGroupByExpressions(expressions);
//        select.setGroupByElement(groupByElement);
    }

    private static void setAggregate(SelectExpressionItem sItem, Long id, OneCte cte, String union) {
//        if (id == null) {
//            return;
//        }
//        Union union1 = cte.getUnionMap().get(union);
//        TableView<TableRow> groupTableAggregates = union1.getGroupTableAggregates();
//        if (groupTableAggregates.getItems().isEmpty()) {
//            return;
//        }
//
//        for (TableRow x : groupTableAggregates.getItems()) {
//            if (id != x.getId()) {
//                continue;
//            }
//            Function expression = new Function();
//            expression.setName(x.getComboBoxValue());
//            ExpressionList list = new ExpressionList();
//            Column col = new Column(x.getName());
//            list.setExpressions(Collections.singletonList(col));
//            expression.setParameters(list);
//            sItem.setExpression(expression);
//        }
    }

    private static void saveAliases(PlainSelect select, OneCte cte, String union, boolean first) {
//        List<SelectItem> sItems = new ArrayList<>();
//
//        for (AliasRow item : cte.getAliasTable().getItems()) {
//            String name = item.getValues().get(union);
//            if (name.equals(EMPTY_UNION_VALUE)) {
//                name = "NULL";
//            }
//
//            Expression expression = null;
//            try {
//                expression = new CCJSqlParser(name).Expression();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            if (expression == null) {
//                throw new RuntimeException("Empty expression for " + name);
//            }
//
//            SelectExpressionItem sItem = new SelectExpressionItem();
//            sItem.setExpression(expression);
//
//            Expression expression1 = sItem.getExpression();
//            boolean equals = true;
//            if (expression1 instanceof Column) {
//                Column column = (Column) expression1;
//                equals = !column.getColumnName().equals(item.getAlias());
//            }
//
//            if (first && equals) {
//                sItem.setAlias(new Alias(item.getAlias()));
//            }
//            setAggregate(sItem, item.getIds().get(union), cte, union);
//            sItems.add(sItem);
//        }
//        select.setSelectItems(sItems);
    }

    private static void saveFromTables(PlainSelect selectBody, OneCte oneCte, String unionName) {
        var union = oneCte.getUnionMap().get(unionName);
        if (!union.getJoinTableModel().getItems().isEmpty()) {
            return;
        }
        var joins = new ArrayList<Join>();
        union.getSelectedTablesRoot().nodeToList().forEach(x -> {
            var tableName = x.getUserObject().getName();
            if (selectBody.getFromItem() == null) {
                selectBody.setFromItem(new Table(tableName));
            } else {
                var join = new Join();
                join.setRightItem(new Table(tableName));
                join.setSimple(true);
                joins.add(join);
            }
        });
        selectBody.setJoins(joins);
    }

    public void saveOrderBy(PlainSelect select, OneCte cte) {
//        List<OrderByElement> orderElements = new ArrayList<>();
//
//        cte.getOrderTableResults().getItems().forEach(x -> {
//            OrderByElement orderByElement = new OrderByElement();
//            Column column = new Column(x.getName());
//            orderByElement.setExpression(column);
//            orderByElement.setAsc(x.getComboBoxValue().equals("Ascending"));
//            orderElements.add(orderByElement);
//        });
//
//        select.setOrderByElements(orderElements);
    }

}
