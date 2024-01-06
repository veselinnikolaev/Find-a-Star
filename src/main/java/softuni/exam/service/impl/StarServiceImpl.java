package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.StarDto;
import softuni.exam.models.dto.StarFormatDto;
import softuni.exam.models.entity.Constellation;
import softuni.exam.models.entity.Star;
import softuni.exam.models.entity.StarType;
import softuni.exam.repository.ConstellationRepository;
import softuni.exam.repository.StarRepository;
import softuni.exam.service.StarService;
import softuni.exam.util.ValidationUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static softuni.exam.models.Constants.*;

@Service
public class StarServiceImpl implements StarService {
    private final StarRepository starRepository;
    private final ConstellationRepository constellationRepository;
    private final ValidationUtils validationUtils;
    private final ModelMapper modelMapper;
    private final Gson gson;

    public StarServiceImpl(StarRepository starRepository, ConstellationRepository constellationRepository, ValidationUtils validationUtils, ModelMapper modelMapper, Gson gson) {
        this.starRepository = starRepository;
        this.constellationRepository = constellationRepository;
        this.validationUtils = validationUtils;
        this.modelMapper = modelMapper;
        this.gson = gson;
    }

    @Override
    public boolean areImported() {
        return this.starRepository.count() > 0;
    }

    @Override
    public String readStarsFileContent() throws IOException {
        return Files.readString(Path.of("src/main/resources/files/json/stars.json"));
    }

    @Override
    public String importStars() throws IOException {
        final StringBuilder output = new StringBuilder();
        List<StarDto> stars = Arrays.stream(gson.fromJson(this.readStarsFileContent(), StarDto[].class)).collect(Collectors.toList());

        for (StarDto star: stars) {
            output.append(System.lineSeparator());

            if(this.validationUtils.isValid(star) && this.starRepository.findFirstByName(star.getName()).isEmpty()){

                Optional<Constellation> constellation = this.constellationRepository.findById(star.getConstellation());
                Star starToSave = this.modelMapper.map(star, Star.class);
                    starToSave.setConstellation(constellation.get());
                    this.starRepository.save(starToSave);
                output.append(String.format(SUCCESSFUL_FORMAT_STAR, STAR, star.getName(), star.getLightYears(), "light years"));
continue;

            }
            output.append(String.format(INVALID_FORMAT, STAR));
        }
        return output.toString().trim();
    }

    @Override
    public String exportStars() {
        return this.starRepository.findByStarTypeAndObserversIsEmptyOrderByLightYears(StarType.RED_GIANT).stream().map(star -> this.modelMapper.map(star, StarFormatDto.class)).map(StarFormatDto::toString).collect(Collectors.joining()).trim();
    }
}
