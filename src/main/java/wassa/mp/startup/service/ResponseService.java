package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wassa.mp.startup.Enumeration.StatutEtapeEnum;
import wassa.mp.startup.dto.ReponseRequestDto;
import wassa.mp.startup.dto.ReponseResponseDto;
import wassa.mp.startup.exception.OperationInterditeException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.ProjetEtape;
import wassa.mp.startup.model.Question;
import wassa.mp.startup.model.Reponse;
import wassa.mp.startup.repository.ProjetEtapeRepository;
import wassa.mp.startup.repository.QuestionRepository;
import wassa.mp.startup.repository.ReponseRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ResponseService {
    @Autowired
    private ReponseRepository responseService;
    @Autowired
    private ProjetEtapeRepository projetEtapeRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ReponseRepository reponseRepository;

    public List<ReponseResponseDto> getReponseByEtapePorjet_id(int etape_id) {
        ProjetEtape projetEtape = projetEtapeRepository.findById(etape_id).orElseThrow(
                () -> new ResourceNotFoundException("Etape" + etape_id + "not found")
        );
        return responseService.findByReponseByProjetEtape(projetEtape.getId()).stream().map(
                reponse -> new ReponseResponseDto(reponse.getId(), reponse.getProjetEtape().getProjet().getTitre(), reponse.getProjetEtape().getEtape().getType().toString(), reponse.getQuestion().getLibelle(), reponse.getReponse())
        ).toList();

    }

    public Reponse repondreunequestion(int etape_projet_id, int question_id, ReponseRequestDto reponseRequestDto) {
        Question question = questionRepository.findById(question_id).orElseThrow(
                () -> new ResourceNotFoundException("Cette question n'existe pas")
        );
        ProjetEtape projetEtape = projetEtapeRepository.findById(etape_projet_id).orElseThrow(
                () -> new ResourceNotFoundException("Le projet à cette etape n'existe pas")
        );

        if (question.getEtape().getId() != projetEtape.getEtape().getId()) {
            throw new OperationInterditeException("Cette question n'appartient pas à l'étape en cours de ce projet.");
        }
        if (projetEtape.getStatutEtape() == StatutEtapeEnum.VALIDE) {
            throw new OperationInterditeException("Impossible de modifier votre réponse : cette étape a déjà été validée et clôturée.");
        }
        Optional<Reponse> reponseExistante = reponseRepository.findByProjetEtapeIdAndQuestionId(etape_projet_id, question_id);

        if (reponseExistante.isPresent()) {
            Reponse reponseAModifier = reponseExistante.get();
            reponseAModifier.setReponse(reponseRequestDto.getReponse());
            return reponseRepository.save(reponseAModifier);
        } else {
            Reponse nouvelleReponse = new Reponse();
            nouvelleReponse.setReponse(reponseRequestDto.getReponse());
            nouvelleReponse.setProjetEtape(projetEtape);
            nouvelleReponse.setQuestion(question);

            return reponseRepository.save(nouvelleReponse);
        }
    }


}
