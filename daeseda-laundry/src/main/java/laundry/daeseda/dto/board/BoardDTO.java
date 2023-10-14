package laundry.daeseda.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BoardDTO {
    private Long boardId;
    private Long userId;
    private String boardCategory;
    private String boardTitle;
    private String boardContent;

    private LocalDateTime regDate;
    private LocalDateTime modDate;
}