package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class ProjetResponseDto {
    private int id;
    private String titre;
    private String description;
    private String pith_url;
    private String quota_propose;
}
