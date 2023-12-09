package laundry.daeseda.service.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class SearchServiceImpl implements SearchService {

    @Override
    public Page<Object[]> searchUserPage(String type, String keyword, Pageable pageable) {
        return null;
    }
}
