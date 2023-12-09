package laundry.daeseda.service.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {

    Page<Object[]> searchUserPage(String type, String keyword, Pageable pageable);


}
