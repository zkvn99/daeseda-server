package laundry.daeseda.user;

import laundry.daeseda.dto.user.UserDto;
import laundry.daeseda.entity.user.AuthorityEntity;
import laundry.daeseda.entity.user.UserEntity;
import laundry.daeseda.exception.DuplicateUserException;
import laundry.daeseda.perf.CompareTimer;
import laundry.daeseda.repository.user.UserRepository;
import laundry.daeseda.service.user.UserService;;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@SpringBootTest
public class UserTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void signupUser() { // rangeClosed 구간만큼 user 생성
        CompareTimer timer = new CompareTimer();
        IntStream.rangeClosed(0, 10)
                .forEach(i -> {
                    UserDto userDto = UserDto.builder()
                            .userName("minwook" + i)
                            .userPhone("0105" + i)
                            .userNickname("min" + i)
                            .userEmail("email" + i + "@daeseda.com")
                            .userPassword("pw" + i)
                            .build();
                    if (userService.signup(userDto) > 0) {
                        System.out.println("등록 성공: " + i);
                        timer.checkNanoTime();
                    } else {
                        timer.checkNanoTime();
                        System.out.println("등록 실패: " + i);
                    }
                });
    }

    @Test
    public void findAllUserEntity() { // 모든 user 조회 (id, 이름)
        List<UserEntity> userEntityList = userRepository.findAll();
        for(UserEntity userEntity : userEntityList) {
            System.out.println("유저 id = "+userEntity.getUserId() + " 유저 이름 = " + userEntity.getUserName());
        }
    }

    @Test
    public void deleteByUserId() { // rangeClosed 구간만큼 유저 삭제
        LongStream.rangeClosed(13, 23)
                .forEach(i -> userRepository.deleteById(i));
    }

}
