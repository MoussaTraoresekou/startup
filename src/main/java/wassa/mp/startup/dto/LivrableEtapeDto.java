package wassa.mp.startup.dto;

import java.util.List;

public class LivrableEtapeDto {
    private String projetTitre;
    private String etapeTitre;
    private String porteurNomComplet;
    private List<QuestionReponseDto> elements;

    public LivrableEtapeDto(String projetTitre, String etapeTitre, String porteurNomComplet, List<QuestionReponseDto> elements) {
        this.projetTitre = projetTitre;
        this.etapeTitre = etapeTitre;
        this.porteurNomComplet = porteurNomComplet;
        this.elements = elements;
    }

    public String getProjetTitre() { return projetTitre; }
    public String getEtapeTitre() { return etapeTitre; }
    public String getPorteurNomComplet() { return porteurNomComplet; }
    public List<QuestionReponseDto> getElements() { return elements; }

    public static class QuestionReponseDto {
        private String questionTexte;
        private String reponseValeur;

        public QuestionReponseDto(String questionTexte, String reponseValeur) {
            this.questionTexte = questionTexte;
            this.reponseValeur = reponseValeur;
        }

        public String getQuestionTexte() { return questionTexte; }
        public String getReponseValeur() { return reponseValeur; }
    }
}
