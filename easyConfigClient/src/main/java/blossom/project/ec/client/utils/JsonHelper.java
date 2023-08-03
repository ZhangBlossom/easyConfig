package blossom.project.ec.client.utils;

import blossom.project.ec.client.config.ProjectConfig;
import blossom.project.ec.client.config.ConfigVersion;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.UUID;

/**
 * Created by 张锦标 on 2023/4/12.
 *
 */
public class JsonHelper {
   static Gson gson = new Gson();
    public static List<ProjectConfig> convertToProjectConfig(String json) {

        return gson.fromJson(json, new TypeToken<List<ProjectConfig>>() {
        }.getType());
        //return JSON.parseArray(json, ProjectConfig.class);
    }

    public String buildCmdWatchProject(String projectName, String env, List<String> projectNameList) {
        CmdRequest request = new CmdRequest();
        request.setCmdId(UUID.randomUUID().toString());
        request.setCmdName("watchproject");
        request.setEnv(env);
        request.setArgs(projectNameList);
        return gson.toJson(request);
    }

    public static String buildConfigVersionList(List<ConfigVersion> versionList) {
        return gson.toJson(versionList);
    }

    public static List<ConfigVersion> convertToConfigVersionList(String json) {

        return gson.fromJson(json, new TypeToken<List<ConfigVersion>>() {
        }.getType());
    }

    public static String buildWatchProjectCommand(String cmdId,String cmdName,String env,String serverRoom,List<String> projectNames) {

        CmdRequest request=new CmdRequest();
        request.setEnv(env);
        request.setCmdName(cmdName);
        request.setServerRoom(serverRoom);
        request.setCmdId(cmdId);
        request.setArgs(projectNames);

        return gson.toJson(request);
    }

    public static String buildProjectListString(List<ProjectConfig> configList) {

        return gson.toJson(configList);
    }
}
