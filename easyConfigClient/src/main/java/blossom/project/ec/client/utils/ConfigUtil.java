package blossom.project.ec.client.utils;

import blossom.project.ec.client.config.ConfigData;
import blossom.project.ec.client.config.ProjectConfig;
import blossom.project.ec.client.config.ConfigVersion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigUtil {

    /**
     * 实现this.configInfoList.stream().map(ProjectConfig::getProjectName).distinct().collect(Collectors.toList())
     * @param projectConfigs
     * @return
     */
    public static Map<String, List<String>> getProjectNames(List<ProjectConfig> projectConfigs) {
        if (projectConfigs.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> result = new HashMap<>();
        /**
         * 先按env进行分组
         */
        Map<String, List<ProjectConfig>> guoupByEnvMap = guoupByEnv(projectConfigs);

        /**
         * 分组内容去重
         */
        for (Map.Entry<String, List<ProjectConfig>> entry : guoupByEnvMap.entrySet()) {
            Set<String> projectNameSet = new HashSet<>();
            List<ProjectConfig> lists = entry.getValue();
            for (ProjectConfig p : lists) {
                projectNameSet.add(p.getProjectName());
            }
            result.put(entry.getKey(), new ArrayList<>(projectNameSet));
        }

        return result;
    }


    /**
     *  按env进行分组
     */
    public static Map<String, List<ProjectConfig>> guoupByEnv(List<ProjectConfig> projectConfigs) {
        Map<String, List<ProjectConfig>> groupMap = new HashMap<>();
        for (ProjectConfig config : projectConfigs) {
            List<ProjectConfig> groupList = groupMap.get(config.getEnv());
            if (groupList == null) {
                groupList = new ArrayList<>();
            }
            groupList.add(config);
        }

        return groupMap;
    }


    /**
     * 实现configInfoList.stream().map(p -> new ConfigVersion(p.getProjectName(), p.getEnv(),p.getServerRoom())).distinct().collect(Collectors.toList())
     * @param projectConfigs
     * @return
     */
    public static List<ConfigVersion> getConfigVersions(List<ProjectConfig> projectConfigs) {
        if (projectConfigs.isEmpty()) {
            return Collections.emptyList();
        }
        Set<ConfigVersion> configVersions = new HashSet<>();
        for (ProjectConfig p : projectConfigs) {
            configVersions.add(new ConfigVersion(p.getProjectName(), p.getEnv(), p.getGroup()));
        }
        return new ArrayList<>(configVersions);
    }

    /**
     * 实现this.configInfoList.stream()
     *                 .filter(p -> p.getProjectName().equals(projectName))
     *                 .flatMap(p -> p.getConfig().stream())
     *                 .map(p -> p.getKey())
     *                 .collect(Collectors.toSet());
     * @param projectConfigs
     * @param projectName
     * @return
     */
    public static Set<String> getProjectConfigKeys(List<ProjectConfig> projectConfigs, String projectName) {
        if (projectConfigs.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> projectConfigKeys = new HashSet<>();
        for (ProjectConfig p : projectConfigs) {
            if (p.getProjectName().equals(projectName)) {
                for (ConfigData configData : p.getConfig()) {
                    projectConfigKeys.add(configData.getKey());
                }
            }
        }
        return projectConfigKeys;
    }

    /**
     * 将配置list转为map
     * @return
     */
    public static Map<String, ConfigData> getProjectDataMap(List<ConfigData> dataList) {
        Map<String, ConfigData> result = new HashMap<>();
        for (ConfigData item : dataList) {
            result.put(item.getKey(), item);
        }
        return result;
    }

    /**
     * (ForExample)当前时间：2019-07-01 20:51:05
     * @return
     */
    public static String getTimeStr() {
        Date date = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        String timeStr = sf.format(date);
        return timeStr;
    }

}
