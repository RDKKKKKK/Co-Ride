import com.coride.constant.StatusConstant;
import com.coride.dto.RideMatchResultDTO;
import com.coride.entity.*;
import com.coride.mapper.CarpoolerMapper;
import com.coride.mapper.DriverMapper;
import com.coride.matching.RideMatcher;
import com.coride.service.message.MessagingServiceTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideMatcherThreadTest {

    @Mock
    private RideMatcher rideMatcher;

    @Mock
    private DriverMapper driverMapper;

    @Mock
    private CarpoolerMapper carpoolerMapper;

    @Mock
    private MessagingServiceTemplate messagingServiceTemplate;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    ExecutorService executor;



    private Driver mockDriver;
    private CarpoolGroup mockCarpoolGroup;
    private RideMatcher.MatchingTask matchingTask;


    @BeforeEach
    void setUp() {
        // 初始化测试数据
        mockDriver = new Driver();
        mockDriver.setIdUser(1L);
        mockDriver.setMatchIds(null);

        mockCarpoolGroup = new CarpoolGroup();
        mockCarpoolGroup.setIdDriver(1L);
        mockCarpoolGroup.setPassengersIds(new ArrayList<>());
        mockCarpoolGroup.setSeatsAvailable(3);
        mockCarpoolGroup.setTotalSeats(3);

        matchingTask = new RideMatcher.MatchingTask(
                mockDriver,
                mockCarpoolGroup,
                rideMatcher,
                driverMapper,
                carpoolerMapper,
                executor,
                System.currentTimeMillis() - 26000L,
                messagingServiceTemplate,
                redisTemplate
        );
    }

    @Test
    void successByAccept(){
        Carpooler carpooler = new Carpooler();
        carpooler.setIdUser(2L);

        // Mock Redis 返回的状态
        ConfirmationState mockState = new ConfirmationState(true, true, 2L, 1L);
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("confirmation_state:1-2")).thenReturn(mockState);
        when(rideMatcher.findMatch(any())).thenReturn(carpooler);

        // Mock 数据库交互
        when(carpoolerMapper.getOriginById(2L)).thenReturn("Origin");
        when(carpoolerMapper.getDestinationById(2L)).thenReturn("Destination");
        when(driverMapper.getVehiclesByPlateNo(any())).thenReturn(new Vehicle());

        // 执行匹配任务
        matchingTask.run();

        // 验证状态更新
        verify(carpoolerMapper, times(1)).updateCarpoolerStatus(2L, StatusConstant.ACCEPTED);
        verify(carpoolerMapper, times(1)).addToCarpoolGroup(2L, 1L);

        // 验证拼车组状态更新
        assertEquals(2, mockCarpoolGroup.getSeatsAvailable());
        assertTrue(mockCarpoolGroup.getPassengersIds().contains(2L));

        // 验证 Redis 缓存清理
        verify(redisTemplate, times(1)).delete("confirmation_state:1-2");

        // 验证通知发送
        verify(messagingServiceTemplate, times(2)).sendMatchConfirmNotification(any());

        /*
        Carpooler newPassenger = new Carpooler();
        newPassenger.setIdUser(100L);
        Driver driver = new Driver();
        driver.setIdUser(1000L);

        RideMatcher.MatchingTask matchingTask = new RideMatcher.MatchingTask(driver, new CarpoolGroup(), rideMatcher, null, null, null, System.currentTimeMillis(), messagingServiceTemplate, redisTemplate);


        ConfirmationState mockState = new ConfirmationState(true, true, 100L, 1000L);

        when(rideMatcher.findMatch(any())).thenReturn(newPassenger);
        doNothing().when(messagingServiceTemplate).sendMatchConfirmNotification(any());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(mockState);

        verify(messagingServiceTemplate, times(2)).sendMatchResultNotification(any());
*/

    }

    @Test
    void failureByDecline(){
        Carpooler carpooler = new Carpooler();
        carpooler.setIdUser(2L);

        // Mock Redis 返回的状态
        ConfirmationState mockState = new ConfirmationState(false, true, 2L, 1L);
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("confirmation_state:1-2")).thenReturn(mockState);
        when(rideMatcher.findMatch(any())).thenReturn(carpooler);

        // Mock 数据库交互
        when(carpoolerMapper.getOriginById(2L)).thenReturn("Origin");
        when(carpoolerMapper.getDestinationById(2L)).thenReturn("Destination");
        when(driverMapper.getVehiclesByPlateNo(any())).thenReturn(new Vehicle());

        // 执行匹配任务
        matchingTask.run();

        // 验证状态更新
        verify(carpoolerMapper, times(1)).updateCarpoolerStatus(2L, StatusConstant.AVAILABLE);

        // 验证拼车组状态更新
        assertEquals(3, mockCarpoolGroup.getSeatsAvailable());
        assertFalse(mockCarpoolGroup.getPassengersIds().contains(2L));

        // 验证 Redis 缓存清理
        verify(redisTemplate, times(1)).delete("confirmation_state:1-2");

        // 验证通知发送
        verify(messagingServiceTemplate, times(2)).sendMatchResultNotification(any());

        // 验证超时
        verify(driverMapper, times((1))).updateDriverStatus(StatusConstant.REJECTED, 1L);

    }


}
