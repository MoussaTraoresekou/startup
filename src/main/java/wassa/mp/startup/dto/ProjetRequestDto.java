package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.model.SecteurActivite;


@Data
@AllArgsConstructor @NoArgsConstructor
public class ProjetRequestDto {
    private String titre;
    private String description;
    private String pith_url;
    private String quota_propose;
    private int secteur_id;

}
