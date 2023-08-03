package blossom.project.ec.db.manager;

public class Action {
    private String methodName;
    private Object[] setParamVals;

    public Action(String methodName, Object... setParamVals) {
        this.methodName = methodName;
        this.setParamVals = setParamVals;
        if (this.setParamVals == null) {
            this.setParamVals = new Object[0];
        }

    }

    public String getMethodName() {
        return this.methodName;
    }

    public Object[] getSetParamVals() {
        return this.setParamVals;
    }
}
