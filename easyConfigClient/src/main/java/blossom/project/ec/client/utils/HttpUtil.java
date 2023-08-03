package blossom.project.ec.client.utils;


public class HttpUtil {

    private String user;
    private String password;

    public HttpUtil(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String get(String url) throws Exception {
        return HttpInvoker(url, "GET", user, password, null);
    }

    public String post(String url, String body) throws Exception {
        return HttpInvoker(url, "POST", user, password, body);
    }

    private String HttpInvoker(String url, String method, String user, String password, String body) throws Exception {
        HttpRequest request = buildRequest(url, method, user, password, body);
        int code = request.code();
        String response = request.body();
        if (code == 200) {
            return response;
        } else {
            if (code == 401) {
                throw new Exception("当前输入的项目名称和密码错误,请从配置中心查询项目正确的密码或联系bps获取");
            } else {
                throw new Exception("httperror-" + code + "\r\n" + response);
            }
        }
    }

    private HttpRequest buildRequest(String url, String method, String user, String password, String body) {
        switch (method) {
            case "POST":
                return HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .basic(user, password)
                    .connectTimeout(10000)
                    .readTimeout(10000)
                    .useCaches(false).send(body);

            case "GET":
                return HttpRequest.get(url).basic(user, password)
                    .connectTimeout(10000)
                    .readTimeout(10000)
                    .useCaches(false);
        }
        return null;
    }


    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
