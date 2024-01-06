package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.ConstellationDto;
import softuni.exam.models.entity.Constellation;
import softuni.exam.repository.ConstellationRepository;
import softuni.exam.service.ConstellationService;
import softuni.exam.util.ValidationUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static softuni.exam.models.Constants.*;

@Service
public class ConstellationServiceImpl implements ConstellationService {
    private final ConstellationRepository constellationRepository;
    private final ValidationUtils validationUtils;
    private final ModelMapper modelMapper;
    private final Gson gson;

    public ConstellationServiceImpl(ConstellationRepository constellationRepository, ValidationUtils validationUtils, ModelMapper modelMapper, Gson gson) {
        this.constellationRepository = constellationRepository;
        this.validationUtils = validationUtils;
        this.modelMapper = modelMapper;
        this.gson = gson;
    }

    @Override
    public boolean areImported() {
        return this.constellationRepository.count() > 0;
    }

    @Override
    public String readConstellationsFromFile() throws IOException {
        return Files.readString(Path.of("src/main/resources/files/json/constellations.json"));
    }

    @Override
    public String importConstellations() throws IOException {
        final StringBuilder output = new StringBuilder();

        List<ConstellationDto> constellations = Arrays.stream(gson.fromJson(readConstellationsFromFile(), ConstellationDto[].class)).collect(Collectors.toList());

        for (ConstellationDto constellation: constellations) {
            output.append(System.lineSeparator());

            if(!this.constellationRepository.findFirstByName(constellation.getName()).isPresent() && this.validationUtils.isValid(constellation)){
                this.constellationRepository.save(this.modelMapper.map(constellation, Constellation.class));
                output.append(String.format(SUCCESSFUL_FORMAT, CONSTELLATION, constellation.getName(), constellation.getDescription()));
                continue;
            }
            output.append(String.format(INVALID_FORMAT, CONSTELLATION));
        }
        return output.toString().trim();
    }
}
