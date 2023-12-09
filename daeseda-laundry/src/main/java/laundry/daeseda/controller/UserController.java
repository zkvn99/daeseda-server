package laundry.daeseda.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import laundry.daeseda.dto.page.PageRequestDto;
import laundry.daeseda.dto.page.PageResultDto;
import laundry.daeseda.dto.user.EmailConfirmDto;
import laundry.daeseda.dto.user.EmailDto;
import laundry.daeseda.dto.user.UserDto;
import laundry.daeseda.dto.user.UserUpdateDto;
import laundry.daeseda.entity.user.UserEntity;
import laundry.daeseda.service.mail.MailService;
import laundry.daeseda.service.user.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Api(tags = {"User API"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final MailService mailService;
    private final RedisTemplate<String, Object> redisTemplate;

    @ApiOperation(value = "get signup user-form", notes = "회원가입 시 필요한 목록 불러오기")
    @GetMapping("/signup")
    public ResponseEntity<List<String>> getSignup() { //register 호출
        String result = "userNickname userName userPhone userEmail userPassword";
        String[] userArray = result.split(" ");
        List<String> userList = new ArrayList<>();
        for(String n : userArray){
            userList.add(n);
        }
        return ResponseEntity.ok().body(userList);
    }
    // HttpStatus.OK (200) - Get 요청

    @ApiOperation(value = "request signup user", notes = "회원가입 요청")
    @PostMapping("/signup")
    public ResponseEntity<String> signupUser(@RequestBody @Valid UserDto userDto) { //register 호출
        userService.signup(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("ok");
    }
    // HttpStatus.CREATED (201), HttpStatus.OK (200) - Post 요청

    @ApiOperation(value = "request logout user", notes = "로그아웃 요청 및 redis 로그인 정보 삭제")
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<String> logout() {
        userService.signout();
        return ResponseEntity.status(HttpStatus.CREATED).body("ok");
    }

    @ApiOperation(value = "get user-info", notes = "회원 정보 불러오기")
    @GetMapping("/myInfo")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<UserDto> getMyUserInfo() {
        long startTime = System.nanoTime();

        UserDto userDto = userService.getMyUserWithAuthorities();

        long endTime = System.nanoTime(); // 작업 종료 시간 기록
        double elapsedTime = (endTime - startTime) / 1000000.0; // 밀리초로 변환

        System.out.println("총 소요 시간: " + elapsedTime + " ms");

        return ResponseEntity.ok(userDto);
    }
    // HttpStatus.OK (200) - Get 요청

    @ApiOperation(value = "update user-name", notes = "회원 이름 변경")
    @PatchMapping("/name")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<String> patchUsername(@RequestBody @Valid UserUpdateDto userDto) {
        if (userService.update(userDto) > 0) {
            return ResponseEntity.ok().body("User updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @ApiOperation(value = "update user-nickname", notes = "회원 닉네임 변경")
    @PatchMapping("/nickname")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<String> patchUserNickname(@RequestBody @Valid UserUpdateDto userDto) {
        if (userService.update(userDto) > 0) {
            return ResponseEntity.ok().body("User updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @ApiOperation(value = "update user-phone", notes = "회원 전화번호 변경")
    @PatchMapping("/phone")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<String> patchUserPhone(@RequestBody @Valid UserUpdateDto userDto) {
        if (userService.update(userDto) > 0) {
            return ResponseEntity.ok().body("User updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }


    @ApiOperation(value = "update user-delete", notes = "회원 탈퇴")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<String> deleteUser() {

        if (userService.delete() > 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }
    // HttpStatus.NO_CONTENT (204) - Delete 요청(성공)
    // HttpStatus.NOT_FOUND (404) - Delete 요청(실패)

    @ApiOperation(value = "request email-authentication", notes = "이메일 인증")
    @ResponseBody
    @PostMapping("/mailAuthentication")
    public ResponseEntity<String> mailAuthentication(@RequestBody EmailDto emailDto) throws Exception {
        if(emailDto != null){
            if(userService.checkDuplicateEmail(emailDto)) {
                String code = mailService.sendMessage(emailDto.getUserEmail());
                System.out.println("인증코드 : " + code);
                redisTemplate.opsForValue().set("EMAIL_CODE" + emailDto.getUserEmail(), code);
                return ResponseEntity.ok(code); // 200 OK with the code
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("중복된 이메일입니다."); // 409 Conflict
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("잘못된 형식입니다.");
    }

    @ApiOperation(value = "request authentication-confirm", notes = "이메일 인증 확인")
    @ResponseBody
    @PostMapping("/mailConfirm")
    public ResponseEntity<String> mailConfirm(@RequestBody EmailConfirmDto emailConfirmDto) throws Exception {
        if(redisTemplate.hasKey("EMAIL_CODE" + emailConfirmDto.getUserEmail())){
            String redisKey = "EMAIL_CODE" + emailConfirmDto.getUserEmail();
            String storedCode = (String) redisTemplate.opsForValue().get(redisKey);
            if(storedCode.equals(emailConfirmDto.getCode())) {
                redisTemplate.delete(redisKey);
                System.out.println("인증코드 : " + emailConfirmDto.getCode());
                return ResponseEntity.ok("ok"); // 200 OK with the code
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("인증번호가 잘못되었습니다");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("잘못된 형식입니다.");
    }

    @ApiOperation(value = "get uesr-list", notes = "회원 조회")
    @ResponseBody
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<PageResultDto<UserDto, Object[]>> getUserList
            (@RequestParam(value = "page", required = false, defaultValue = "1") int page,
             @RequestParam(value = "perPage", required = false, defaultValue = "10") int perPage,
             @RequestParam(value = "perPagination", required = false, defaultValue = "5") int perPagination,
             @RequestParam(value = "type", required = false, defaultValue = "n") String type,
             @RequestParam(value = "keyword", required = false, defaultValue = "@") String keyword) {

        PageRequestDto pageRequestDto = PageRequestDto.builder()
                .page(page)
                .perPage(perPage)
                .perPagination(perPagination)
                .type(type)
                .keyword(keyword)
                .build();

        PageResultDto<UserDto, Object[]> resultDto = userService.getUserList(pageRequestDto);

        return ResponseEntity.ok(resultDto);
    }
}
