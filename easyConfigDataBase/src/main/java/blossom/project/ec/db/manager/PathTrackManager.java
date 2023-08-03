package blossom.project.ec.db.manager;

import blossom.project.ec.db.manager.TransactionContext;
import blossom.project.ec.db.util.CommonUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class PathTrackManager {
    public PathTrackManager() {
    }

    public void doPathChain(TransactionContext tr) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Queue<Action> actionPath = tr.getActions();
        if (!CommonUtil.isNullOrEmpty(actionPath)) {
            for(Action action = (Action)actionPath.poll(); action != null; action = (Action)actionPath.poll()) {
                this.doPathChain(tr.getRealConnection(), action);
            }

        }
    }

    public Object doPathChain(Object target, Action action) throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Class[] clazz = null;
        if (action.getSetParamVals() != null && action.getSetParamVals().length != 0) {
            clazz = new Class[action.getSetParamVals().length];

            for(int i = 0; i < clazz.length; ++i) {
                clazz[i] = CommonUtil.getBasicType(action.getSetParamVals()[i]);
            }
        } else {
            clazz = new Class[0];
        }

        Method method = target.getClass()
                .getMethod(action.getMethodName(), clazz);
        method.setAccessible(true);
        return method.invoke(target, action.getSetParamVals());
    }

    public <T> T getMethodValue(TransactionContext tr,
                                String targetMethodName,
                                Class<T> valClass,
                                int valueIndex,
                                T defVal) {
        Queue<Action> actionPath = tr.getActions();
        return this.getMethodValue(actionPath, targetMethodName, valClass, valueIndex, defVal);
    }

    public <T> T getMethodValue(Queue<Action> actionPath,
                                String targetMethodName, Class<T> valClass,
                                int valueIndex,
                                T defVal) {
        if (actionPath != null && !actionPath.isEmpty()) {
            List<Action> actions = new ArrayList(actionPath);
            Iterator var7 = actions.iterator();

            Action action;
            do {
                do {
                    if (!var7.hasNext()) {
                        if (defVal == null) {
                            return null;
                        }

                        return defVal;
                    }

                    action = (Action)var7.next();
                } while(!action.getMethodName().equals(targetMethodName));

                if (action.getSetParamVals() == null || action.getSetParamVals().length == 0) {
                    return defVal == null ? null : defVal;
                }
            } while(!action.getSetParamVals()[0].getClass().equals(valClass));

            return (T) action.getSetParamVals()[valueIndex];
        } else {
            return defVal == null ? null : defVal;
        }
    }
}
