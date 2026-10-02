package wassa.mp.startup.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wassa.mp.startup.dto.QuestionResponseDto;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Etape;
import wassa.mp.startup.repository.EtapeRepository;
import wassa.mp.startup.repository.QuestionRepository;
import java.util.List;
@Service
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private EtapeRepository etapeRepository;
    public List<QuestionResponseDto> getQuestionByEtape(int etape_id) {
        Etape etape=etapeRepository.findById(etape_id).orElseThrow(
                ()-> new  ResourceNotFoundException("etape non trouvée")
        );
        return questionRepository.findByEtape(etape.getId()).stream().map(
                question -> new QuestionResponseDto(question.getId(),question.getLibelle(),question.getEtape().getType().toString())
        ).toList();

    }
}
