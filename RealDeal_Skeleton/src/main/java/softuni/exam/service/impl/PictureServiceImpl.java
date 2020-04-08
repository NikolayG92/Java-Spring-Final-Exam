package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dtos.PictureSeedDto;
import softuni.exam.models.entities.Car;
import softuni.exam.models.entities.Picture;
import softuni.exam.repository.CarRepository;
import softuni.exam.repository.PictureRepository;
import softuni.exam.service.PictureService;
import softuni.exam.util.ValidationUtil;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static softuni.exam.constants.GlobalConstants.PICTURES_FILE_PATH;

@Service
public class PictureServiceImpl implements PictureService {
    private final PictureRepository pictureRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final Gson gson;
    private final CarRepository carRepository;

    public PictureServiceImpl(PictureRepository pictureRepository, ModelMapper modelMapper, ValidationUtil validationUtil, Gson gson, CarRepository carRepository) {
        this.pictureRepository = pictureRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.gson = gson;
        this.carRepository = carRepository;
    }

    @Override
    public boolean areImported() {
        return this.pictureRepository.count() > 0;
    }

    @Override
    public String readPicturesFromFile() throws IOException {
        return Files.readString(Path.of(PICTURES_FILE_PATH));
    }

    @Override
    public String importPictures() throws IOException {
        StringBuilder result = new StringBuilder();
        PictureSeedDto[] pictureSeedDtos = this.gson
                .fromJson(new FileReader(PICTURES_FILE_PATH), PictureSeedDto[].class);
        Arrays.stream(pictureSeedDtos)
                .forEach(pictureSeedDto -> {
                    if(this.validationUtil.isValid(pictureSeedDto)){
                        if(this.pictureRepository.getByName(pictureSeedDto.getName()) == null){
                            Picture picture = this.modelMapper.map(pictureSeedDto, Picture.class);
                            Car car = this.carRepository.getOne(pictureSeedDto.getCar());
                            LocalDateTime date = LocalDateTime.parse
                                    (pictureSeedDto.getDateAndTime(),
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            picture.setDateAndTime(date);
                            picture.setCar(car);
                            result.append(String.format("Successfully import picture - %s",
                                    picture.getName()));
                            this.pictureRepository.saveAndFlush(picture);
                        }else {
                            result.append("This picture is already in DB!");
                        }
                    }else {
                        result.append("Invalid picture");
                    }
                    result.append(System.lineSeparator());
                });
        return result.toString();
    }
}
