import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.Multimap;
import io.netty.util.HashedWheelTimer;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * @author: 张锦标
 * @date: 2023/7/13 18:56
 * TimingWheel类
 */
public class TimingWheel {
    public static void main(String[] args) {
        DeferredResult dr = new DeferredResult();

        Multimap multimap = new ForwardingMultimap() {
            @Override
            protected Multimap delegate() {
                return null;
            }
        };
        HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();
    }
}
/**
 * DeferredResult
 * Multimap
 */