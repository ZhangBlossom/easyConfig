/**
 * @author: 张锦标
 * @date: 22023/7/13 11:00
 * PropertyTest类
 */
public class PropertyTest {
    public static void main(String[] args) {
        String property = System.getProperty("easyconfig");
        if (property!=null){
            System.out.println(property);
        }
    }
}
