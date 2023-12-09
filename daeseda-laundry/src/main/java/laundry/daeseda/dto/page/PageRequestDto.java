package laundry.daeseda.dto.page;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Builder
@Getter
public class PageRequestDto {

    private int page; // 요청 페이지
    private int perPage; // 페이지당 게시물 수
    private int perPagination; // 한 화면에 나타나는 페이지 수
    private String type; // 검색 유형
    private String keyword; // 검색어

    public Pageable getPageable(Sort sort) {
        return PageRequest.of(page -1, perPage, sort);
    }
}
