package org.quebee.com.qpart;

import com.intellij.util.ui.ListTableModel;
import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.quebee.com.model.LinkElement;
import org.quebee.com.model.QBTreeNode;
import org.quebee.com.model.TableElement;

import java.util.Objects;

@Getter
@Setter
public class Union implements Orderable {
    private Integer order;

    // from tables
    private QBTreeNode selectedTablesRoot = new QBTreeNode(new TableElement("empty"));
    private ListTableModel<TableElement> selectedFieldsModel = new ListTableModel<>();

    // joins
    private ListTableModel<LinkElement> joinTableModel = new ListTableModel<>();

    // grouping
    private QBTreeNode groupingRoot;
    private ListTableModel<TableElement> groupingTableModel = new ListTableModel<>();
    private ListTableModel<TableElement> aggregateTableModel = new ListTableModel<>();

    public Union(SelectBody selectBody, Integer order) {
        this.order = order;
        if (Objects.isNull(selectBody)) {
            return;
        }
        var select = (PlainSelect) selectBody;
        fillFromTablesModels(select);
        fillSelectedFieldsModels(select);
        fillJoinTableModel(select);
    }

    private void fillJoinTableModel(PlainSelect select) {
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

    private void fillSelectedFieldsModels(PlainSelect select) {
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
            }
        }
    }

    private void fillFromTablesModels(PlainSelect select) {
        var fromItem = select.getFromItem(Table.class);
        selectedTablesRoot.add(new QBTreeNode(new TableElement(fromItem)));
        var joins = select.getJoins();
        if (Objects.isNull(joins)) {
            return;
        }
        for (var join : joins) {
            var rightItem = join.getRightItem();
            if (rightItem instanceof Table) {
                selectedTablesRoot.add(new QBTreeNode(new TableElement(rightItem)));
            }
        }
    }
}
