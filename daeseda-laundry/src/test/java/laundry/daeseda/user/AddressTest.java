package laundry.daeseda.user;

import laundry.daeseda.entity.user.AddressEntity;
import laundry.daeseda.entity.user.UserEntity;
import laundry.daeseda.perf.CompareTimer;
import laundry.daeseda.repository.user.AddressRepository;
import laundry.daeseda.repository.user.UserRepository;
import laundry.daeseda.service.user.AddressServiceImpl;
import laundry.daeseda.service.user.UserService;
import laundry.daeseda.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.LongStream;

@SpringBootTest
public class AddressTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private AddressServiceImpl addressService;

    CompareTimer timer = new CompareTimer();

    @Test
    public void createAddress() {
        UserEntity user = userRepository.getById(1L);
        long startTime = System.nanoTime(); // 작업 시작 시간 기록

        LongStream.rangeClosed(1, 10000)
                .forEach(i -> {
                    timer.checkNanoTime();
                    timer.checkCurrentTimeMills();
                    AddressEntity addressEntity = AddressEntity.builder()
                            .addressId(i)
                            .addressZipcode("testzip"+i)
                            .addressDetail("testDetail"+i)
                            .addressName("testName"+i)
                            .addressRoad("testRoad"+i)
                            .user(user)
                            .build();
                    addressRepository.save(addressEntity);
                });

        long endTime = System.nanoTime(); // 작업 종료 시간 기록
        double elapsedTime = (endTime - startTime) / 1000000.0; // 밀리초로 변환

        System.out.println("총 소요 시간: " + elapsedTime + " ms");
    } // 총 소요 시간: 33675.183667 ms / 총 소요 시간: 34762.935709 ms

//    @Test
//    public void createAddress() {
//        UserEntity user = userRepository.getById(1L);
//        long startTime = System.nanoTime(); // 작업 시작 시간 기록
//        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//
//        List<CompletableFuture<Void>> futures = LongStream.rangeClosed(1, 10000)
//                .mapToObj(i -> CompletableFuture.runAsync(() -> {
//                    timer.checkNanoTime();
//                    timer.checkCurrentTimeMills();
//                    AddressEntity addressEntity = AddressEntity.builder()
//                            .addressId(i)
//                            .addressZipcode("testzip" + i)
//                            .addressDetail("testDetail" + i)
//                            .addressName("testName" + i)
//                            .addressRoad("testRoad" + i)
//                            .user(user)
//                            .build();
//                    addressRepository.save(addressEntity);
//                }, executorService))
//                .collect(Collectors.toList());
//
//        // CompletableFuture 리스트의 모든 작업이 완료될 때까지 기다림
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//        long endTime = System.nanoTime(); // 작업 종료 시간 기록
//        double elapsedTime = (endTime - startTime) / 1000000.0; // 밀리초로 변환
//
//        System.out.println("총 소요 시간: " + elapsedTime + " ms");
//    } // 총 소요 시간: 30077.181917 ms

//    @Test
//    public void createAddress() {
//        UserEntity user = userRepository.getById(1L);
//
//        long startTime = System.nanoTime(); // 작업 시작 시간 기록
//        LongStream.rangeClosed(1, 10000)
//                .parallel() // Parallel Stream으로 변경
//                .forEach(i -> {
//                    timer.checkNanoTime();
//                    timer.checkCurrentTimeMills();
//                    AddressEntity addressEntity = AddressEntity.builder()
//                            .addressId(i)
//                            .addressZipcode("testzip" + i)
//                            .addressDetail("testDetail" + i)
//                            .addressName("testName" + i)
//                            .addressRoad("testRoad" + i)
//                            .user(user)
//                            .build();
//                    addressRepository.save(addressEntity);
//                });
//
//        long endTime = System.nanoTime(); // 작업 종료 시간 기록
//        double elapsedTime = (endTime - startTime) / 1000000.0; // 밀리초로 변환
//        System.out.println("총 소요 시간: " + elapsedTime + " ms");
//    } // 총 소요 시간: 29157.81325 ms / 총 소요 시간: 29987.307792 ms / 총 소요 시간: 30340.377125 ms

}
