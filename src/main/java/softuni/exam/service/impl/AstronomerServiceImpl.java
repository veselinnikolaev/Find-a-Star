package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.AstronomerDto;
import softuni.exam.models.dto.AstronomerWrapper;
import softuni.exam.models.entity.Astronomer;
import softuni.exam.models.entity.Star;
import softuni.exam.repository.AstronomerRepository;
import softuni.exam.repository.StarRepository;
import softuni.exam.service.AstronomerService;
import softuni.exam.util.ValidationUtils;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static softuni.exam.models.Constants.*;

@Service
public class AstronomerServiceImpl implements AstronomerService{
    private final AstronomerRepository astronomerRepository;
    private final StarRepository starRepository;
    private final ValidationUtils validationUtils;
    private final ModelMapper modelMapper;
    private final XmlParser xmlParser;

    public AstronomerServiceImpl(AstronomerRepository astronomerRepository, StarRepository starRepository, ValidationUtils validationUtils, ModelMapper modelMapper, XmlParser xmlParser) {
        this.astronomerRepository = astronomerRepository;
        this.starRepository = starRepository;
        this.validationUtils = validationUtils;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;
    }


    @Override
    public boolean areImported() {
        return this.astronomerRepository.count() > 0;
    }

    @Override
    public String readAstronomersFromFile() throws IOException {
        return Files.readString(Path.of("src/main/resources/files/xml/astronomers.xml"));
    }

    @Override
    public String importAstronomers() throws IOException, JAXBException {
        final StringBuilder output = new StringBuilder();
        List<AstronomerDto> astronomers = this.xmlParser.fromFile(Path.of("src/main/resources/files/xml/astronomers.xml").toFile(), AstronomerWrapper.class).getAstronomers();

        for (AstronomerDto astronomer: astronomers) {
            output.append(System.lineSeparator());

            if(this.validationUtils.isValid(astronomer) && !this.astronomerRepository.findFirstByFirstNameAndLastName(astronomer.getFirstName(), astronomer.getLastName()).isPresent()){
                Optional<Star> star = this.starRepository.findById(astronomer.getObservingStarId());
                if(star.isEmpty()){
                   output.append(String.format(INVALID_FORMAT, ASTRONOMER));
                   continue;
                }
                Astronomer astronomerToSave = this.modelMapper.map(astronomer, Astronomer.class);
                astronomerToSave.setObservingStar(star.get());
                this.astronomerRepository.save(astronomerToSave);
                output.append(String.format(SUCCESSFUL_FORMAT_ASTRONOMER, ASTRONOMER, (astronomer.getFirstName() + " " + astronomer.getLastName()), astronomer.getAverageObservationHours()));
                continue;
            }
            output.append(String.format(INVALID_FORMAT, ASTRONOMER));
        }
        return output.toString().trim();
    }
}
