import com.coride.constant.StatusConstant;
import com.coride.dto.RideMatchResultDTO;
import com.coride.entity.CarpoolGroup;
import com.coride.entity.Carpooler;
import com.coride.entity.Driver;
import com.coride.mapper.CarpoolerMapper;
import com.coride.mapper.DriverMapper;
import com.coride.matching.RideMatcher;
import com.coride.service.message.MessagingServiceTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideMatcherMethodTest {

    @Mock
    private DriverMapper driverMapper;

    @Mock
    private CarpoolerMapper carpoolerMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private MessagingServiceTemplate messagingServiceTemplate;

    @InjectMocks
    private RideMatcher rideMatcher;

    private CarpoolGroup carpoolGroup;
    private Driver driver;
    private Carpooler matchedPassenger;

    @BeforeEach
    void setUp() {
        // 初始化 CarpoolGroup
        carpoolGroup = new CarpoolGroup();
        carpoolGroup.setIdDriver(1L); // 用户 ID 设置为 int 型
        carpoolGroup.setSeatsAvailable(0);

        // 初始化 Driver
        driver = new Driver();
        driver.setIdUser(1L);

        // 初始化 Carpooler
        matchedPassenger = new Carpooler();
        matchedPassenger.setIdUser(100L);
        matchedPassenger.setName("Test Passenger");

    }

    @Test
    void testFindMatch() {

        when(carpoolerMapper.findBestMatchID(any())).thenReturn(100L);
        when(carpoolerMapper.findById(100L)).thenReturn(matchedPassenger);

        // 调用被测方法
        Carpooler result = rideMatcher.findMatch(carpoolGroup);

        // 验证返回结果
        assertNotNull(result, "匹配结果应不为空");
        assertEquals(100, result.getIdUser(), "匹配到的乘客 ID 应为 100");
        assertEquals("Test Passenger", result.getName(), "匹配到的乘客名称应为 'Test Passenger'");

        // 验证 Mapper 方法调用
        verify(carpoolerMapper, times(1)).findBestMatchID(any());
        verify(carpoolerMapper, times(1)).findById(100L);
        verify(carpoolerMapper, times(1)).updateCarpoolerStatus(100L, StatusConstant.PENDING);
    }

    @Test
    void testCarpoolGroupComplete() {
        // Mock 乘客 ID 列表
        List<Long> passengerIds = new ArrayList<>(Arrays.asList(100L, 101l));
        carpoolGroup.setPassengersIds(passengerIds);

        // 调用被测方法
        rideMatcher.carpoolGroupComplete(carpoolGroup, passengerIds, mock(ScheduledExecutorService.class), driver);

        // 验证发送通知的方法被调用一次
        verify(messagingServiceTemplate, times(1)).sendMatchResultNotification(any(RideMatchResultDTO.class));

        // 捕获发送的 RideMatchResultDTO 参数
        ArgumentCaptor<RideMatchResultDTO> argumentCaptor = ArgumentCaptor.forClass(RideMatchResultDTO.class);
        verify(messagingServiceTemplate).sendMatchResultNotification(argumentCaptor.capture());
        RideMatchResultDTO capturedResult = argumentCaptor.getValue();

        // 验证通知的内容是否正确
        assertEquals("Success", capturedResult.getResultType(), "通知状态应为 Success");
        assertEquals(1, capturedResult.getDriverId(), "通知的司机 ID 应为 1");
        assertTrue(capturedResult.getCarpoolerIds().containsAll(passengerIds), "通知的乘客 ID 列表应包含所有匹配的乘客");

        // 验证 Mapper 方法调用
        verify(driverMapper, times(1)).updateDriverStatus(StatusConstant.COMPLETED, 1L);
        verify(driverMapper, times(1)).insertCarpoolGroup(carpoolGroup);
        verify(carpoolerMapper, times(2)).updateCarpoolerStatus(anyLong(), eq(StatusConstant.COMPLETED));
    }
}