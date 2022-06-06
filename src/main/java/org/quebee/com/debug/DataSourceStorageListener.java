package org.quebee.com.debug;

import com.intellij.database.dataSource.DataSourceStorage;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.model.DasModel;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.ObjectKind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataSourceStorageListener implements DataSourceStorage.Listener {

    @Override
    public void dataSourceModelLoaded(@NotNull LocalDataSource dataSource, @NotNull DasModel model) {
        DataSourceStorage.Listener.super.dataSourceModelLoaded(dataSource, model);
        for (DasObject modelRoot : model.getModelRoots()) {
          //  System.out.println(modelRoot);
            var dasChildren = modelRoot.getDasChildren(ObjectKind.TABLE);
         //   System.out.println(dasChildren);
            var dasObjects = dasChildren.toList();

            for (var dasObject : dasObjects) {
                var columns = dasObject.getDasChildren(ObjectKind.COLUMN);
                for (var column : columns) {
                    System.out.println(column);
                }
            }

        }
//        ((MysqlImplModel) model).getRoot().getSchemas().get("heroku_f07f0a62be2b400").getTables();
    }

    @Override
    public void dataSourceAdded(@NotNull LocalDataSource dataSource) {
        DataSourceStorage.Listener.super.dataSourceAdded(dataSource);
    }

    @Override
    public void dataSourceRemoved(@NotNull LocalDataSource dataSource) {
        DataSourceStorage.Listener.super.dataSourceRemoved(dataSource);
    }

    @Override
    public void dataSourceChanged(@Nullable LocalDataSource dataSource) {
        DataSourceStorage.Listener.super.dataSourceChanged(dataSource);
    }
}
