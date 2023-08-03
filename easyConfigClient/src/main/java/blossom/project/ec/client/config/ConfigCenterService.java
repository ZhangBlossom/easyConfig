package blossom.project.ec.client.config;


import blossom.project.easyconfig.websocket.WebSocket;
import blossom.project.ec.client.utils.*;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 张锦标
 * 配置中心服务提供
 */
public class ConfigCenterService {

    private static Logger logger = LoggerFactory.getLogger(ConfigCenterService.class);
    private static ScheduledExecutorService executorService =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        }
    });
    private volatile static ConfigCenterService currentService = null;
    private static List<ConfigChanged> configChangedList = new CopyOnWriteArrayList<>();

    /**
     * 小流量配置
     */
    private static Map<String, Set<String>> smallFlowMap = new HashMap<>();
    //private static String cacheDir;
    //public static String cacheFile;
    //开发环境列表
    static List<String> envList = new ArrayList<>(4);

    static {
        envList.add("test");
        envList.add("qa");
        envList.add("dev");
        envList.add("product");

        File directory = new File("");
        String courseFile = "";
        try {
            courseFile = directory.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //cacheDir = courseFile + File.separator + "cache";
        //cacheFile = cacheDir + File.separator + "tcbase.configv6.cache";

        //executorService.scheduleWithFixedDelay(new Runnable() {
        //    @Override
        //    public void run() {
        //        if (currentService != null) {
        //            currentService.checkVersionByHttp();
        //        }
        //    }
        //}, 0, 10, TimeUnit.SECONDS);
        //十秒之后检测ws链接
        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (currentService != null) {
                    currentService.checkWebSocket();
                }
            }
        }, 0, 10, TimeUnit.SECONDS);

        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (currentService != null) {
                    currentService.login();
                }
            }
        }, 0, 10, TimeUnit.SECONDS);

        //executorService.scheduleWithFixedDelay(new Runnable() {
        //    @Override
        //    public void run() {
        //        if (currentService != null) {
        //            currentService.saveCacheData(ConfigCenterService.cacheFile);
        //        }
        //    }
        //}, 0, 10, TimeUnit.SECONDS);

        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (currentService != null) {
                    currentService.pullProjectConfig();
                }
            }
        }, 0, 3, TimeUnit.MINUTES);
    }

    public static String getProjectName() {
        return AppProperties.getProjectName();
    }

    public static String getEnv() {
        return AppProperties.getEnvironment();
    }

    public static String getStatus() {
        return currentService.currentStatus;
    }

    public static void addConfigChange(ConfigChanged configChanged) {
        configChangedList.add(configChanged);
    }

    public static void removeConfigChange(ConfigChanged configChanged) {
        configChangedList.remove(configChanged);
    }
    //双检锁保证单例
    public static void init() {
        if (currentService == null) {
            synchronized (ConfigCenterService.class) {
                if (currentService == null) {
                    currentService = new ConfigCenterService();
                }
            }
        }
    }

    public static boolean isInited() {
        return currentService != null;
    }

    private List<ProjectConfig> configInfoList = new CopyOnWriteArrayList<>();

    private ConfigCenterWebSocketClient webSocketClient;
    private HttpUtil httpUtil;
    private String currentStatus;


    private ConfigCenterService() {

        this.httpUtil = new HttpUtil(AppProperties.getProjectName(), AppProperties.getProjectName());
        //完成从配置中心拉取对应环境的所有配置信息并且解析为类
        List<ProjectConfig> projectConfigList =
                getProjectConfigListFromRemote(AppProperties.getProjectName(),
                AppProperties.getEnvironment(), AppProperties.getGroup());
        for (ProjectConfig projectConfig : projectConfigList) {
            addItemToConfigInfoList(projectConfig);
        }
        checkWebSocket();
        login();
    }

    private synchronized void addItemToConfigInfoList(ProjectConfig projectConfig) {
        //获取所有projectname和env是当前配置环境给的的配置
        ProjectConfig oldConfig = getProjectConfig(projectConfig
                .getProjectName(), projectConfig.getEnv());

        //TODO 这里的操作有点像新配置覆盖老配置
        //这个version字段我之后如何使用呢？
        if (oldConfig == null) {
            configInfoList.add(projectConfig);
        } else {
            if (projectConfig.getVersion() > 0) {
                configInfoList.add(projectConfig);
                configInfoList.remove(oldConfig);
            }
        }
    }

    /**
     * 检查WebSocket连接状态并且在未连接时进行链接
     */
    private synchronized void checkWebSocket() {
        try {
            if (this.webSocketClient != null && ((this.webSocketClient.getReadyState() == WebSocket.READYSTATE.OPEN) || (this.webSocketClient.getReadyState() == WebSocket.READYSTATE.CONNECTING))) {
                return;
            }
            buildWebSocket();
        } catch (Throwable throwable) {
            logger.error(throwable.getMessage(), throwable);
        }
    }

    /**
     * 建立ws的链接
     */
    private void buildWebSocket() {
        try {
            //根据url地址链接ws客户端
            webSocketClient = new ConfigCenterWebSocketClient(UrlBuilder.getWebSocketUrl());
            /**
             * 普通配置变更
             * 创建配置变更事件监听
             */
            webSocketClient.setProjectChange(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    try {
                        //重载项目配置
                        reloadProjectConfig(s, AppProperties.getEnvironment(),
                                AppProperties.getGroup(), true);
                    } catch (Throwable ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            });

            /**
             * 小流量配置变更
             */
            webSocketClient.setSmallFlowChange(new Consumer<String>() {
                @Override
                public void accept(String data) {
                    reloadSmallFlowProjectConfig(data);
                }
            });

            boolean success = webSocketClient.connectBlocking();
            if (success) {
                logger.debug("ws连接成功!");
            } else {
                logger.info("统一配置ws连接失败!" + UrlBuilder.getWebSocketUrl());
            }
        } catch (Throwable throwable) {
            logger.error(throwable.getMessage(), throwable);
        }
    }

    /**
     * 不同环境的分开login
     */
    private void login() {
        Map<String, List<String>> projectNames =
                ConfigUtil.getProjectNames(configInfoList);
        try {
            for (Map.Entry<String, List<String>> entry : projectNames.entrySet()) {
                String json = JsonHelper.buildWatchProjectCommand(UUID.randomUUID().toString(), "watchproject",
                        entry.getKey(), AppProperties.getGroup(), entry.getValue());
                if (webSocketClient != null && webSocketClient.getReadyState() == WebSocket.READYSTATE.OPEN) {
                    webSocketClient.send(json);
                }
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    AtomicInteger errorCount = new AtomicInteger(0);

    /**
     * 先检测版本号，如果版本号不一致，则直接去请求
     */
    //private void checkVersionByHttp() {
    //    List<ConfigVersion> versionList =
    //            ConfigUtil.getConfigVersions(configInfoList);
    //    if (versionList.size() == 0) {
    //        return;
    //    }
    //    String json = JsonHelper.buildConfigVersionList(versionList);
    //    try {
    //        String result = httpUtil.post(UrlBuilder.getVersionUrl(), json);
    //        errorCount.set(0);
    //        List<ConfigVersion> configVersions = JsonHelper.convertToConfigVersionList(result);
    //
    //        for (ConfigVersion configVersion : configVersions) {
    //            ProjectConfig oldData = getProjectConfig(configVersion.getProjectName(), configVersion.getEnv());
    //
    //            /**
    //             * 如果Redis故障，此处configVersion.getVersion()值为-1；必定不等于oldData.getVersion()
    //             * 但是这种情况下不需要重新加载配置
    //             */
    //            if (configVersion.getVersion() > 0 && (oldData == null || oldData.getVersion() != configVersion.getVersion())) {
    //                if (oldData != null) {
    //                    reloadProjectConfig(configVersion.getProjectName(), configVersion.getEnv(),
    //                            configVersion.getGroup(), true);
    //                }
    //            }
    //        }
    //    } catch (Throwable throwable) {
    //        int count = errorCount.incrementAndGet();
    //        if (count > 5) {
    //            errorCount.set(0);
    //            logger.error(throwable.getMessage(), throwable);
    //        }
    //    }
    //}

    /**
     * 检测配置，查看是否有需要更新的配置
     * 注意：只会更新oldData中有的key，如果没有会添加进OldData中
     */
    private synchronized void pullProjectConfig() {
        // 是否更新文件的标记
        for (ProjectConfig configVersion : configInfoList) {
            try {
                reloadProjectConfig(configVersion.getProjectName(), configVersion.getEnv(), configVersion.getGroup(),
                        false);
            } catch (Exception e) {
                logger.error("pullProjectConfig error", e);
            }
        }
    }

    private ProjectConfig getProjectConfig(final String projectName, final String env) {
        return CollectionUtil.findFirst(configInfoList, new Predicate<ProjectConfig>() {
            @Override
            public boolean test(ProjectConfig p) {
                return p.getProjectName().equals(projectName) && p.getEnv().equals(env);
            }
        });
    }

    /**
     * env不为空时需同时判断projectName和env
     * 为空时只判断projectName
     */
    private List<ProjectConfig> getProjectConfigList(String projectName, String env) {
        List<ProjectConfig> selectedConfigs = new ArrayList<>();
        for (ProjectConfig p : configInfoList) {
            if (env != null) {
                if (p.getProjectName().equals(projectName) && p.getEnv().equals(env)) {
                    selectedConfigs.add(p);
                }
            } else {
                if (p.getProjectName().equals(projectName)) {
                    selectedConfigs.add(p);
                }
            }

        }
        return selectedConfigs;
    }


    private List<ProjectConfig> getProjectConfigListFromRemote(String projectName, String env, String group) {
        try {
            //根据项目配置信息去获取对应的配置文件的url
            String url = UrlBuilder.getConfigUrl(projectName, env, group);
            String json = httpUtil.get(url);
            currentStatus = "ready";
            return JsonHelper.convertToProjectConfig(json);
        } catch (Exception ex) {
            currentStatus = "offline";
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }


    private void reloadSmallFlowProjectConfig(String data) {
        Gson gson = new Gson();
        Map<String, String> dataMap = gson.fromJson(data, Map.class);
        String projectName = dataMap.get("projectName");
        String env = dataMap.get("env");
        String serverRoom = dataMap.get("serverRoom");
        final String key = dataMap.get("key");
        String value = dataMap.get("value");
        boolean isStop = false;
        if (dataMap.get("stop").equals("0")) {
            isStop = true;
        }
        Set<String> smallFlowKeys = smallFlowMap.get(projectName);

        /**
         * 如果停用小流量，则立即重新加载配置
         */
        if (isStop && smallFlowKeys != null) {
            smallFlowKeys.remove(key + "@" + value);
            try {
                reloadProjectConfig(projectName, env, serverRoom, true);
            } catch (Exception e) {
                logger.error("小流量停止重新再加配置失败:{}", e);
            }
            return;
        }

        /**
         * 记录小流浪key、value
         * 格式为：key+@+value
         */
        if (smallFlowKeys == null) {
            smallFlowKeys = new HashSet<>();
            smallFlowKeys.add(key + "@" + value);
            smallFlowMap.put(projectName, smallFlowKeys);
        } else {
            smallFlowKeys.add(key + "@" + value);
        }

        ProjectConfig existData = getProjectByProjectAndEnvAndGroup(projectName, env, serverRoom);
        if (existData != null) {
            List<ConfigData> configList = existData.getConfig();
            ConfigData configData = CollectionUtil.findFirst(configList, new Predicate<ConfigData>() {
                @Override
                public boolean test(ConfigData c) {
                    return c.getKey().equals(key);
                }
            });

            if (configData != null) {
                configData.setValue(value);
            } else {
                logger.info("小流量修改配置失败，当前key不存在");
            }
        } else {
            logger.info("小流量修改配置失败，当前配置不存在,请核对!当前修改信息为:{}", data);
        }
    }

    /**
     * 查找可修改的配置
     */
    private ProjectConfig getProjectByProjectAndEnvAndGroup(final String projectName, final String env,
                                                            final String serverRoom) {
        return CollectionUtil.findFirst(configInfoList, new Predicate<ProjectConfig>() {
            @Override
            public boolean test(ProjectConfig p) {
                return p.getProjectName().equals(projectName) && p.getEnv().equals(env) && p.getGroup().equals(serverRoom);
            }
        });
    }

    /**
     * 重新加载配置
     *
     * @param projectName
     * @param env
     * @param serverRoom
     * @throws Exception
     */
    private void reloadProjectConfig(String projectName, String env, String serverRoom, boolean isdelete) throws Exception {
        /**
         * 此处获取配置时如果Redis故障，返回的ProjectConfig version为-1；config为emptyList
         */
        List<ProjectConfig> projectConfigList = getProjectConfigListFromRemote(projectName, env, serverRoom);
        for (ProjectConfig projectConfig : projectConfigList) {
            ChangeConfigData changeConfigData = mergeChange(projectConfig, isdelete);
            if (changeConfigData == null) {
                continue;
            }
            if (changeConfigData.getAddList().size() == 0 && changeConfigData.getUpdateList().size() == 0 && changeConfigData.getRemoveList().size() == 0) {
                continue;
            }
            for (ConfigChanged changed : configChangedList) {
                changed.configChanged(changeConfigData);
            }
        }
    }

    private HashMap<String, String> getAllKeyFromProjectByDefault(String projectName) {
        Set<String> keys = ConfigUtil.getProjectConfigKeys(configInfoList, projectName);

        HashMap<String, String> hashMap = new HashMap<>();
        for (String key : keys) {
            //如果这里是父子环境的子环境，父环境变更后子环境检测不到变更，但可能子环境用的是父环境的配置
            hashMap.put(key, getConfigByDefault(projectName, null, key));
        }
        return hashMap;
    }


    //private void saveCacheData(String cacheFile) {
    //    FileOutputStream out = null;
    //    try {
    //        List<ProjectConfig> saveList = new ArrayList<>();
    //        /**
    //         * 当前项目信息
    //         */
    //        ProjectConfig current = new ProjectConfig();
    //        current.setProjectName(AppProperties.getProjectName());
    //        current.setConfig(new ArrayList<ConfigData>());
    //        saveList.add(current);
    //
    //        /**
    //         * 如果当前项目配置了小流量，则添加小流量标识
    //         */
    //        Set<String> smallFlow = smallFlowMap.get(current.getProjectName());
    //        if (smallFlow != null && smallFlow.size() > 0) {
    //            ProjectConfig smallFlowTag = new ProjectConfig();
    //            smallFlowTag.setProjectName("当前项目有【" + smallFlow.size() + "】个key进行了小流量操作");
    //            saveList.add(smallFlowTag);
    //        }
    //
    //        saveList.addAll(configInfoList);
    //
    //        String json = JsonHelper.buildProjectListString(saveList);
    //
    //        File dic = new File(cacheDir);
    //        if (!dic.exists() && !dic.isDirectory()) {
    //            dic.mkdir();
    //        }
    //        File file = new File(cacheFile);
    //        if (!file.exists()) {
    //            file.createNewFile();
    //        }
    //        out = new FileOutputStream(file, false);
    //        out.write(json.getBytes("utf-8"));
    //    } catch (Exception ex) {
    //        logger.error(ex.getMessage(), ex, cacheFile);
    //    } finally {
    //        if (out != null) {
    //            try {
    //                out.close();
    //            } catch (Exception ex) {
    //                logger.error(ex.getMessage(), ex);
    //            }
    //        }
    //    }
    //}

    private ChangeConfigData mergeChange(ProjectConfig newProjectConfig, boolean isdelete) {

        ChangeConfigData changeConfigData = new ChangeConfigData();

        HashMap<String, String> oldHashMap = getAllKeyFromProjectByDefault(newProjectConfig.getProjectName());

        /**
         * 新值覆盖旧值时忽略小流量配置的key
         */
        ignoreSmallFlowKey(newProjectConfig);

        addItemToConfigInfoList(newProjectConfig);

        HashMap<String, String> newHashMap = getAllKeyFromProjectByDefault(newProjectConfig.getProjectName());

        // 找出add和update的数据
        for (String key : newHashMap.keySet()) {
            if (!oldHashMap.containsKey(key)) {
                changeConfigData.getAddList().add(new ConfigData(key, newHashMap.get(key)));
            }

            if (oldHashMap.containsKey(key) && !StringUtils.equals(oldHashMap.get(key), newHashMap.get(key))) {
                changeConfigData.getUpdateList().add(new ConfigData(key, newHashMap.get(key)));
            }
        }
        if (isdelete) {
            // 找到remove的数据
            for (String key : oldHashMap.keySet()) {
                if (!newHashMap.containsKey(key)) {
                    changeConfigData.getRemoveList().add(new ConfigData(key, oldHashMap.get(key)));
                }

            }
        }

        changeConfigData.setProjectName(newProjectConfig.getProjectName());
        changeConfigData.setVersion(newProjectConfig.getVersion());
        changeConfigData.setEnv(newProjectConfig.getEnv());

        return changeConfigData;
    }

    private void ignoreSmallFlowKey(ProjectConfig newProjectConfig) {
        Set<String> smallFlowKeys = smallFlowMap.get(newProjectConfig.getProjectName());
        List<ConfigData> configList = newProjectConfig.getConfig();
        if (smallFlowKeys != null && smallFlowKeys.size() > 0) {
            for (String smallFlow : smallFlowKeys) {
                final String smallFlowKey = smallFlow.split("@")[0];
                final String smallFlowValue = smallFlow.split("@")[1];
                ConfigData newConfigData = CollectionUtil.findFirst(configList, new Predicate<ConfigData>() {
                    @Override
                    public boolean test(ConfigData c) {
                        return c.getKey().equals(smallFlowKey) && !c.getValue().equals(smallFlowValue);
                    }
                });

                if (newConfigData != null) {
                    newConfigData.setValue(smallFlowValue);
                }
            }
        }
    }

    /**
     * 如果指定环境，则使用指定环境过滤
     * 如果没指定环境，则使用默认环境过滤
     */
    private String getConfigByDefault(final String projectName, final String env, String key) {
        ProjectConfig projectConfig = CollectionUtil.findFirst(configInfoList, new Predicate<ProjectConfig>() {
            @Override
            public boolean test(ProjectConfig p) {
                if (env != null) {
                    return p.getProjectName().equals(projectName) && p.getEnv().equals(env);
                } else {
                    return p.getProjectName().equals(projectName) && p.getEnv().equals(AppProperties.getEnvironment());
                }
            }
        });

        /**
         * 如果当前环境配置不存在，说明还没被加载过，需要加载而不能取父环境
         * 这就要求服务端在当前环境没值时返回空值（""），而不能不返回这个环境的值
         */
        if (projectConfig == null) {
            return null;
        }

        String value = getConfigByProjectConfig(projectConfig, key);
        //考虑父环境  当前配置中心里面没有拿到数据去从父环境获取
        if (value == null && env != null) {
            final String parentEnv = ConfigCenterService.getParentEnv(env);
            projectConfig = CollectionUtil.findFirst(configInfoList, new Predicate<ProjectConfig>() {
                @Override
                public boolean test(ProjectConfig p) {
                    if (parentEnv != null) {
                        return p.getProjectName().equals(projectName) && p.getEnv().equals(parentEnv);
                    } else {
                        return p.getProjectName().equals(projectName) && p.getEnv().equals(AppProperties.getEnvironment());
                    }
                }
            });
        }
        return getConfigByProjectConfig(projectConfig, key);
    }

    private String getConfigByProjectConfig(ProjectConfig config, final String key) {
        if (config == null) {
            return null;
        }

        ConfigData configData = CollectionUtil.findFirst(config.getConfig(), (x) -> {
            return x.getKey().equals(key);
        });
        if (configData != null) {
            return configData.getValue();
        }
        return null;
    }



    public static synchronized String get(String projectName, String env, String key) throws Exception {
        if (currentService == null) {
            throw new RuntimeException("init方法未执行,请在获取配置前先执行init方法!");
        }
        if (projectName == null || key == null) {
            return null;
        }

        String result = currentService.getConfigByDefault(projectName, env, key);
        /**
         * 当前 projectName 和 env 的数据在项目启动时已经加载到内存，
         * 此处判断是当前projectName的就返回,如果为null，数据重新加载的工作交给定时任务执行
         * 如果不是，需要考虑把另外env或者另外projectName的数据也加载到内存
         */
        if (AppProperties.getProjectName().equals(projectName) && AppProperties.getEnvironment().equals(env)) {
            return result;
        } else {
            /**
             * 如果是查询不同env或者是另外projectName，但是数据已经加载到内存了，此处直接返回
             * 如果内存没有需要加载(定时任务不会加载另外env或者projectName的配置,加载进来后定时任务会更新它)
             */
            if (result != null) {
                return result;
            } else {
                List<ProjectConfig> projectConfig = currentService.getProjectConfigList(projectName, env);
                /**
                 * 如果根据projectName和env能匹配出配置项，但是没有这个key的（有这个key的话上面就返回了）
                 * 说明此key确实不存在，避免每次都reload，此处直接返回
                 */
                if (projectConfig != null && projectConfig.size() > 0) {
                    return result;
                }
                if (env != null) {
                    currentService.reloadProjectConfig(projectName, env, AppProperties.getGroup(), true);
                } else {
                    currentService.reloadProjectConfig(projectName, AppProperties.getEnvironment(),
                            AppProperties.getGroup(), true);
                }
                return currentService.getConfigByDefault(projectName, env, key);
            }
        }
    }

    public static HashMap<String, String> getAllConfig(String projectName) {
        return currentService.getAllKeyFromProjectByDefault(projectName);
    }

    public static synchronized String get(String key) throws Exception {
        return get(AppProperties.getProjectName(), AppProperties.getEnvironment(), key);
    }

    /**
     * 获取当前环境的父环境
     */
    public static String getParentEnv(String env) {
        for (String e : envList) {
            if (env.startsWith(e)) {
                return e;
            }
        }
        return env;
    }

    public static void main(String[] args) {
        System.out.println(getParentEnv("test_11212"));
    }
}
