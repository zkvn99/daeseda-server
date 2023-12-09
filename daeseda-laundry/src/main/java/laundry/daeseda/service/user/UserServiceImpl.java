package laundry.daeseda.service.user;

import laundry.daeseda.controller.BoardController;
import laundry.daeseda.dto.address.AddressDto;
import laundry.daeseda.dto.page.PageRequestDto;
import laundry.daeseda.dto.page.PageResultDto;
import laundry.daeseda.dto.user.EmailDto;
import laundry.daeseda.dto.user.UserDto;
import laundry.daeseda.dto.user.UserUpdateDto;
import laundry.daeseda.entity.order.OrderEntity;
import laundry.daeseda.entity.reply.ReplyEntity;
import laundry.daeseda.entity.user.AddressEntity;
import laundry.daeseda.entity.user.AuthorityEntity;
import laundry.daeseda.entity.user.UserEntity;
import laundry.daeseda.exception.DuplicateUserException;
import laundry.daeseda.exception.NotFoundUserException;
import laundry.daeseda.repository.board.BoardRepository;
import laundry.daeseda.repository.delivery.DeliveryRepository;
import laundry.daeseda.repository.order.OrderClothesRepository;
import laundry.daeseda.repository.order.OrderRepository;
import laundry.daeseda.repository.payment.PaymentRepository;
import laundry.daeseda.repository.reply.ReplyRepository;
import laundry.daeseda.repository.review.ReviewRepository;
import laundry.daeseda.repository.user.AddressRepository;
import laundry.daeseda.repository.user.AuthorityRepository;
import laundry.daeseda.repository.user.UserRepository;
import laundry.daeseda.service.payment.PaymentService;
import laundry.daeseda.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final ReplyRepository replyRepository;
    private final DeliveryRepository deliveryRepository;
    private final OrderClothesRepository orderClothesRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public int signup(UserDto userDto) {
        if (userRepository.findOneWithAuthoritiesByUserEmail(userDto.getUserEmail()).orElse(null) != null) {
            throw new DuplicateUserException("이미 가입되어 있는 유저입니다.");
        }

        String authorityName = "ROLE_USER";
        AuthorityEntity authorityEntity = authorityRepository.findByAuthorityName(authorityName);

        if (authorityEntity == null) {
            // "ROLE_USER" 권한이 없으면 새로 생성
            authorityEntity = AuthorityEntity.builder()
                    .authorityName(authorityName)
                    .build();
            authorityRepository.save(authorityEntity);
        }

        UserEntity userEntity = UserEntity.builder()
                .userNickname(userDto.getUserNickname())
                .userName(userDto.getUserName())
                .userPhone(userDto.getUserPhone())
                .userEmail(userDto.getUserEmail())
                .userPassword(passwordEncoder.encode(userDto.getUserPassword()))
                .authorities(Collections.singleton(authorityEntity))
                .activated(true)
                .build();

        if(userRepository.save(userEntity) != null)
            return 1;
        else
            return 0;
    }

    @Transactional
    public int signout() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            redisTemplate.delete("JWT_TOKEN:" + userDetails.getUsername()); //Token 삭제
            return 1;
        } catch (NullPointerException n) {
            return 0;
        }
    }


    @Transactional(readOnly = true)
    public UserDto getUserWithAuthorities(String userEmail) {
        return UserDto.from(userRepository.findOneWithAuthoritiesByUserEmail(userEmail).orElse(null));
    }

    @Transactional(readOnly = true)
    public UserDto getMyUserWithAuthorities() {
        return UserDto.from(
                SecurityUtil.getCurrentUsername()
                        .flatMap(userRepository::findOneWithAuthoritiesByUserEmail)
                        .orElseThrow(() -> new NotFoundUserException("User not found"))
        );
    }

    @Transactional
    public int update(UserUpdateDto userDto) {
        UserEntity user = userRepository.findByUserEmail(SecurityUtil.getCurrentUsername().get()).get();

        if(userDto.getUserName() != null){
            userRepository.updateUserName(userDto.getUserName(), user.getUserId());
            return 1;
        } else if (userDto.getUserNickname() != null) {
            userRepository.updateUserNickname(userDto.getUserNickname(), user.getUserId());
            return 1;
        } else if (userDto.getUserPhone() != null) {
            userRepository.updateUserPhone(userDto.getUserPhone(), user.getUserId());
            return 1;
        }
        return 0;
    }

    @Override
    public UserEntity getUserEntity() {
        String currentUserEmail = SecurityUtil.getCurrentUsername().get();
        UserEntity userEntity = userRepository.findByUserEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return userEntity;
    }

    @Override
    public PageResultDto<UserDto, Object[]> getUserList(PageRequestDto pageRequestDto) {
        Page<Object[]> result = userRepository.searchUserPage(
                pageRequestDto.getType(),
                pageRequestDto.getKeyword(),
                pageRequestDto.getPageable(Sort.by("userName").descending()));

        Function<Object[], UserDto> fn = (entity -> {
            UserEntity user = (UserEntity) entity[0];
            UserDto userDto = UserDto.from(user);
            return userDto;
        });
        return new PageResultDto<>(result, fn, 5);
    }

    @Override
    public boolean checkDuplicateEmail(EmailDto emailDto) {
        if(!userRepository.findByUserEmail(emailDto.getUserEmail()).isPresent())
            return true;
        else
            return false;
    }

    @Transactional
    public boolean settingDefaultAddress(AddressDto addressDto) {
        AddressEntity address = addressRepository.findById(addressDto.getAddressId()).get();
        UserEntity userEntity = userRepository.findByUserEmail(SecurityUtil.getCurrentUsername().get()).get();

        if(address.getUser().getUserId().equals(userEntity.getUserId())){
            UserEntity user = UserEntity.builder()
                    .userId(userEntity.getUserId())
                    .userNickname(userEntity.getUserNickname())
                    .userName(userEntity.getUserName())
                    .userPhone(userEntity.getUserPhone())
                    .userEmail(userEntity.getUserEmail())
                    .userPassword(userEntity.getUserPassword())
                    .defaultAddress(address)
                    .activated(true)
                    .build();

            userRepository.updateDefaultAddress(address, user.getUserId());
            return true;
        }

        return false;
    }

    @Transactional
    public int delete() {
        try {
            Optional<UserEntity> user = userRepository.findByUserEmail(SecurityUtil.getCurrentUsername().get());
            List<OrderEntity> order = orderRepository.getByUser(user.get());
            List<AddressEntity> address = addressRepository.getByUser(user.get());
            List<ReplyEntity> reply = replyRepository.getByUser(user.get());

            userRepository.updateDefaultAddress(null, user.get().getUserId());

            for (OrderEntity orderEntity : order) {
                deliveryRepository.deleteByOrderId(orderEntity); // 수정 필요 (배송 중일 때 삭제 불가로)
                paymentRepository.deleteByOrderId(orderEntity);
                orderRepository.deleteById(orderEntity.getOrderId());
            }

            for (AddressEntity addressEntity : address) {
                addressRepository.deleteById(addressEntity.getAddressId());
            }

            for (ReplyEntity replyEntity : reply) {
                replyRepository.deleteById(replyEntity.getReplyId());
            }

            reviewRepository.deleteByUserId(user.get());
            boardRepository.deleteByUserId(user.get());
            userRepository.deleteById(user.get().getUserId());

            return 1;
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

}
