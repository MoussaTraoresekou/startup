package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor @NoArgsConstructor
public class ProjetRequestDto {
    private String titre;
    private String description;
    private String pith_url;
    private String quota_propose;

}
