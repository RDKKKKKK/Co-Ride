import com.coride.entity.CarpoolGroup;
import com.coride.entity.Driver;
import com.coride.mapper.CarpoolerMapper;
import com.coride.mapper.DriverMapper;
import com.coride.matching.RideMatcher;
import com.coride.service.message.MessagingServiceTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.*;

/**
 * 单元测试类：测试 RideMatcher 类的拼车匹配功能
 * 通过 Mock 模拟外部依赖，验证业务逻辑是否正确
 */
public class RideMatcherTest {

    /**
     * Mock：模拟 DriverMapper，用于测试 Driver 数据库操作
     */
    @Mock
    private DriverMapper driverMapper;

    /**
     * Mock：模拟 CarpoolerMapper，用于测试乘客相关数据库操作
     */
    @Mock
    private CarpoolerMapper carpoolerMapper;

    /**
     * Mock：模拟 Redis 缓存操作
     */
    @Mock
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Mock：模拟消息服务，用于发送拼车匹配结果通知
     */
    @Mock
    private MessagingServiceTemplate messagingServiceTemplate;

    /**
     * Mock：模拟调度器，用于调度匹配任务
     */
    @Mock
    private ScheduledExecutorService executor;

    /**
     * InjectMocks：被测试类 RideMatcher，依赖会被注入到其中
     */
    @InjectMocks
    private RideMatcher rideMatcher;

    /**
     * 初始化 Mock 对象，确保所有 @Mock 注解的依赖被正确注入
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // 初始化所有的 Mock 和 InjectMocks
    }

    /**
     * 测试场景：司机信息存在缓存中（Cache Hit）
     * 测试逻辑：当司机信息已缓存，方法应直接从缓存获取数据，而不会查询数据库
     */
    @Test
    void testRideMatch_CacheHit() {
        // 准备测试数据
        CarpoolGroup carpoolGroup = new CarpoolGroup(); // 模拟拼车组
        carpoolGroup.setIdDriver(1L); // 设置司机 ID
        String driverJson = "{\"idUser\":1,\"name\":\"Test Driver\"}"; // 缓存中的司机数据

        // 配置 Mock 行为：当 Redis 查询指定 key 时返回预设值
        when(stringRedisTemplate.opsForValue().get("cache:user:1")).thenReturn(driverJson);

        // 调用被测试方法
        rideMatcher.rideMatch(carpoolGroup);

        // 验证逻辑
        verify(stringRedisTemplate).opsForValue(); // 验证 Redis 操作被调用
        verifyNoInteractions(driverMapper); // 验证数据库未被调用（确保逻辑正确）
    }

    /**
     * 测试场景：司机信息不存在缓存中（Cache Miss）
     * 测试逻辑：当缓存未命中时，方法应查询数据库并将结果写入缓存
     */
    @Test
    void testRideMatch_CacheMiss() {
        // 准备测试数据
        CarpoolGroup carpoolGroup = new CarpoolGroup(); // 模拟拼车组
        carpoolGroup.setIdDriver(1L); // 设置司机 ID
        Driver driver = new Driver(); // 模拟数据库中返回的司机数据
        driver.setIdUser(1L);

        // 配置 Mock 行为：Redis 查询返回 null，数据库查询返回 driver
        when(stringRedisTemplate.opsForValue().get("cache:user:1")).thenReturn(null);
        when(driverMapper.getDriverById(1L)).thenReturn(driver);

        // 调用被测试方法
        rideMatcher.rideMatch(carpoolGroup);

        // 验证逻辑
        verify(driverMapper).getDriverById(1L); // 验证是否调用了数据库查询
        verify(stringRedisTemplate.opsForValue()).set(anyString(), anyString()); // 验证缓存写入
    }

    /**
     * 测试场景：调度匹配任务
     * 测试逻辑：验证匹配任务是否被正确调度
     */
    @Test
    void testRideMatch_TaskScheduling() {
        // 准备测试数据
        CarpoolGroup carpoolGroup = new CarpoolGroup(); // 模拟拼车组
        carpoolGroup.setIdDriver(1L); // 设置司机 ID
        Driver driver = new Driver(); // 模拟数据库返回的司机数据
        driver.setIdUser(1L);

        // 配置 Mock 行为：数据库查询返回 driver
        when(driverMapper.getDriverById(1L)).thenReturn(driver);

        // 调用被测试方法
        rideMatcher.rideMatch(carpoolGroup);

        // 验证逻辑：检查调度器是否被正确调用
        verify(executor).scheduleAtFixedRate(
                any(Runnable.class), eq(0L), eq(30L), eq(java.util.concurrent.TimeUnit.SECONDS));
    }
}