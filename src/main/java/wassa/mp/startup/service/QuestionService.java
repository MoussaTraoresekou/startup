package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wassa.mp.startup.dto.QuestionResponseDto;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Etape;
import wassa.mp.startup.model.Question;
import wassa.mp.startup.model.ProjetEtape;
import wassa.mp.startup.repository.EtapeRepository;
import wassa.mp.startup.repository.QuestionRepository;
import wassa.mp.startup.repository.ProjetEtapeRepository; // AJOUT
import wassa.mp.startup.repository.ReponseRepository;     // AJOUT

import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {
@Autowired
private QuestionRepository questionRepository;
@Autowired
private EtapeRepository etapeRepository;
@Autowired
private ProjetEtapeRepository projetEtapeRepository;
@Autowired
private ReponseRepository reponseRepository;
public List<QuestionResponseDto> getQuestionByEtape(int etape_id, int projetId) {
Etape etape = etapeRepository.findById(etape_id).orElseThrow(
        () -> new ResourceNotFoundException("etape non trouvée")
);
List<Question> questions = questionRepository.findByEtape(etape.getId());
Optional<ProjetEtape> projetEtapeOpt = projetEtapeRepository.findByProjetIdAndEtapeId(projetId, etape_id);
return questions.stream().map(question -> {
    String texteDeLaReponse = null;
    if (projetEtapeOpt.isPresent()) {
        int projetEtapeId = projetEtapeOpt.get().getId();
        Optional<wassa.mp.startup.model.Reponse> reponseOpt = reponseRepository
                .findByProjetEtapeIdAndQuestionId(projetEtapeId, question.getId());
        if (reponseOpt.isPresent()) {
            texteDeLaReponse = reponseOpt.get().getReponse();
        }
    }
    return new QuestionResponseDto(
            question.getId(),
            question.getLibelle(),
            question.getEtape().getType().toString(),
            texteDeLaReponse
    );
}).toList();
}
}
