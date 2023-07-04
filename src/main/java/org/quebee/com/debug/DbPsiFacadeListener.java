package org.quebee.com.debug;

import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DbPsiFacadeListener implements DbPsiFacade.Listener {
    @Override
    public void onChanged(@Nullable DbDataSource dbDataSource) {
//        System.out.println(dbDataSource);
        if (Objects.nonNull(dbDataSource)) {
            var name = dbDataSource.getModel().getCurrentRootNamespace().getName();
//            System.out.println(dbDataSource.getDasChildren(ObjectKind.TABLE).toList());
        }

//        for (DasObject modelRoot : dbDataSource.getModel().getModelRoots()) {
//            modelRoot.getDasChildren(ObjectKind.TABLE);
//        }
    }
}
