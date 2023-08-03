package blossom.project.ec.client.config;

/**
 * Created by xyj6035 on 2016/3/24.
 *
 */
public abstract class ConfigChanged implements IConfigChanged{
    @Override
    public void configChanged(ChangeConfigData changeInfo) {
        System.err.printf("CONFIG IS CHANGED，THE CHANGEINFO IS: %s\n", changeInfo);
    }
}
