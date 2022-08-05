package org.quebee.com.qpart;

import com.intellij.util.ui.ListTableModel;
import icons.DatabaseIcons;
import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.quebee.com.model.*;

import java.util.Objects;

@Getter
@Setter
public class Union implements Orderable {
    private Integer order;

    // from tables
    private TreeNode selectedTablesRoot = new TreeNode(new TableElement("empty"));
    private ListTableModel<TableElement> selectedFieldsModel = new ListTableModel<>();

    // joins
    private ListTableModel<LinkElement> joinTableModel = new ListTableModel<>();

    // grouping
    private TreeNode groupingRoot;
    private ListTableModel<TableElement> groupingTableModel = new ListTableModel<>();
    private ListTableModel<AggregateElement> aggregateTableModel = new ListTableModel<>();

    // conditions
    private ListTableModel<ConditionElement> conditionTableModel = new ListTableModel<>();

    public Union(SelectBody selectBody, Integer order) {
        this.order = order;
        if (Objects.isNull(selectBody)) {
            return;
        }
        var select = (PlainSelect) selectBody;
        fillFromTables(select);
        fillSelectedFieldsAndAggregates(select);
        fillJoinTables(select);
        fillGroupBy(select);
        fillConditions(select);
    }

    private void fillGroupBy(PlainSelect pSelect) {
        var groupBy = pSelect.getGroupBy();
        if (groupBy == null) {
            return;
        }
        groupBy.getGroupByExpressionList().getExpressions().forEach(x -> {
            if (x instanceof Column) {
                setTableName(pSelect, (Column) x);
            }
            var row = new TableElement(x.toString());
            groupingTableModel.addRow(row);
        });
    }

    private void setTableName(PlainSelect pSelect, Column expression) {
        if (expression.getTable() == null && pSelect.getJoins() == null) {
            expression.setTable((Table) pSelect.getFromItem());
        }
    }

    private void fillConditions(PlainSelect pSelect) {
        var where = pSelect.getWhere();
        if (where == null) {
            return;
        }
        if (where instanceof AndExpression) {
            parseAndExpression((AndExpression) where);
        } else {
            conditionTableModel.addRow(getConditionFromExpression(where));
        }
    }

    private ConditionElement getConditionFromExpression(Expression where) {
        var conditionElement = new ConditionElement();
        if (where instanceof ComparisonOperator || where instanceof LikeExpression) {
            var where1 = (BinaryExpression) where;
            conditionElement.setConditionLeft(where1.getLeftExpression().toString());
            conditionElement.setConditionComparison(where1.getStringExpression());
            conditionElement.setConditionRight(where1.getRightExpression().toString());
        } else {
            conditionElement.setCondition(where.toString());
            conditionElement.setCustom(true);
        }
        return conditionElement;
    }

    private void parseAndExpression(AndExpression where) {
        var conditionElement = getConditionFromExpression(where.getRightExpression());
        conditionTableModel.insertRow(0, conditionElement);
        var leftExpression = where.getLeftExpression();
        while (leftExpression instanceof AndExpression) {
            var left = (AndExpression) leftExpression;
            var condition = getConditionFromExpression(left.getRightExpression());
            conditionTableModel.insertRow(0, condition);
            leftExpression = left.getLeftExpression();
        }
        var condition = getConditionFromExpression(leftExpression);
        conditionTableModel.insertRow(0, condition);
    }

    private void fillJoinTables(PlainSelect select) {
        var joins = select.getJoins();
        if (Objects.isNull(joins) || joins.stream().allMatch(Join::isSimple)) {
            return;
        }

        var fromItem = select.getFromItem();
        for (var join : joins) {
            var rightItem = join.getRightItem();
            if (rightItem instanceof Table) {
                addLinkRow(fromItem, join);
            } else if (rightItem instanceof SubSelect) {
//                SubSelect sSelect = (SubSelect) rightItem;
//                rightItemName = sSelect.getAlias().getName();
//                TableRow tableRow = new TableRow(rightItemName);
//                tableRow.setNested(true);
//                tableRow.setRoot(true);
//                String queryText = sSelect.toString().replace(sSelect.getAlias().toString(), "");
//                queryText = queryText.substring(1, queryText.length() - 1);
//                tableRow.setQuery(queryText);
//                TreeItem<TableRow> tableRowTreeItem = new TreeItem<>(tableRow);
//                tablesView.getRoot().getChildren().add(tableRowTreeItem);
//
//                PlainSelect plainSelect = (PlainSelect) sSelect.getSelectBody();
//                plainSelect.getSelectItems().forEach((sItem) -> {
//                    TableRow nestedItem = new TableRow(sItem.toString());
//                    TreeItem<TableRow> nestedRow = new TreeItem<>(nestedItem);
//                    tableRowTreeItem.getChildren().add(nestedRow);
//                });
            }
        }
    }

    private void addLinkRow(FromItem table, Join join) {
        if (join.getOnExpressions() == null) {
            return;
        }

        var onExpression = join.getOnExpression();
        if (onExpression instanceof AndExpression) {
            var expression = (AndExpression) onExpression;
            while (true) {
                var rightExpression = expression.getRightExpression();
                var linkElement = new LinkElement(
                        getTableName(table), getTableName(join.getRightItem()),
                        isLeft(join), isRight(join), isCustom(rightExpression)
                );
                setSimpleCondition(linkElement, rightExpression);
                joinTableModel.addRow(linkElement);
                if (!(expression.getLeftExpression() instanceof AndExpression)) {
                    var lExpression = expression.getLeftExpression();
                    var linkElement2 = new LinkElement(
                            getTableName(table), getTableName(join.getRightItem()),
                            isLeft(join), isRight(join), isCustom(lExpression)
                    );
                    setSimpleCondition(linkElement2, lExpression);
                    joinTableModel.addRow(linkElement2);
                    break;
                }
                expression = (AndExpression) expression.getLeftExpression();
            }
        } else {
            var expression = join.getOnExpression();
            var linkElement = new LinkElement(
                    getTableName(table), getTableName(join.getRightItem()),
                    isLeft(join), isRight(join), isCustom(expression)
            );
            setSimpleCondition(linkElement, expression);
            joinTableModel.addRow(linkElement);
        }
    }

    private String getTableName(FromItem join) {
        return Objects.nonNull(join.getAlias()) ? join.getAlias().getName() : join.toString();
    }

    private static void setSimpleCondition(LinkElement linkElement, Expression expression) {
        var cond = expression.toString();
        if (expression instanceof ComparisonOperator) {
            var expr = (ComparisonOperator) expression;
            var leftColumn = (Column) expr.getLeftExpression();
            var rightColumn = (Column) expr.getRightExpression();
            cond = leftColumn.getColumnName() + expr.getStringExpression() + rightColumn.getColumnName();
            linkElement.setField1(leftColumn.getColumnName());
            linkElement.setField2(rightColumn.getColumnName());
            linkElement.setCondition(expr.getStringExpression());
        }
        linkElement.setCondition(cond);
    }

    private static boolean isLeft(Join join) {
        if (join.isInner()) {
            return false;
        }
        return join.isFull() || join.isLeft();
    }

    private static boolean isRight(Join join) {
        if (join.isInner()) {
            return false;
        }
        return join.isFull() || join.isRight();
    }

    private static boolean isCustom(Expression expression) {
        return !(expression instanceof ComparisonOperator);
    }

    private void fillSelectedFieldsAndAggregates(PlainSelect select) {
        for (var selectItem : select.getSelectItems()) {
            if (!(selectItem instanceof SelectExpressionItem)) {
                continue;
            }
            var selectExpressionItem = (SelectExpressionItem) selectItem;
            var expression = selectExpressionItem.getExpression();
            if (expression instanceof Column) {
                var column = (Column) expression;
                var item = new TableElement(column.getTable().getName(), column.getColumnName());
                if (Objects.nonNull(selectExpressionItem.getAlias())) {
                    item.setAlias(selectExpressionItem.getAlias().getName());
                }
                selectedFieldsModel.addRow(item);
            } else if (expression instanceof Function) {
                var function = (Function) expression;
                var expressions = function.getParameters().getExpressions();
                if (expressions.size() == 1 && expressions.get(0) instanceof Column) {
                    var column = (Column) expressions.get(0);
                    var item = new TableElement(column.getTable().getName(), column.getColumnName());
                    if (Objects.nonNull(selectExpressionItem.getAlias())) {
                        item.setAlias(selectExpressionItem.getAlias().getName());
                    }
                    selectedFieldsModel.addRow(item);
                }
            } else {
                var item = new TableElement(expression.toString(), DatabaseIcons.Function);
                if (Objects.nonNull(selectExpressionItem.getAlias())) {
                    item.setAlias(selectExpressionItem.getAlias().getName());
                }
                selectedFieldsModel.addRow(item);
            }
        }
    }

    private void fillFromTables(PlainSelect select) {
        var fromItem = select.getFromItem(Table.class);
        selectedTablesRoot.add(new TreeNode(new TableElement(fromItem)));
        var joins = select.getJoins();
        if (Objects.isNull(joins)) {
            return;
        }
        for (var join : joins) {
            var rightItem = join.getRightItem();
            if (rightItem instanceof Table) {
                selectedTablesRoot.add(new TreeNode(new TableElement(rightItem)));
            }
        }
    }
}
