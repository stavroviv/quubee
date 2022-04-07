package org.quebee.com;

import com.intellij.database.model.DasObject;
import com.intellij.database.model.ObjectKind;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DbPsiFacadeListener implements DbPsiFacade.Listener {
    @Override
    public void onChanged(@Nullable DbDataSource dbDataSource) {
        System.out.println(dbDataSource);
        if (Objects.nonNull(dbDataSource)) {
            String name = dbDataSource.getModel().getCurrentRootNamespace().getName();
            System.out.println(dbDataSource.getDasChildren(ObjectKind.TABLE).toList());
        }

//        for (DasObject modelRoot : dbDataSource.getModel().getModelRoots()) {
//            modelRoot.getDasChildren(ObjectKind.TABLE);
//        }
    }
}
