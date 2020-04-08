package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dtos.OfferSeedRootDto;
import softuni.exam.models.entities.Car;
import softuni.exam.models.entities.Offer;
import softuni.exam.models.entities.Picture;
import softuni.exam.models.entities.Seller;
import softuni.exam.repository.CarRepository;
import softuni.exam.repository.OfferRepository;
import softuni.exam.repository.PictureRepository;
import softuni.exam.repository.SellerRepository;
import softuni.exam.service.OfferService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static softuni.exam.constants.GlobalConstants.OFFERS_FILE_PATH;

@Service
public class OfferServiceImpl implements OfferService {
    private final OfferRepository offerRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;
    private final CarRepository carRepository;
    private final SellerRepository sellerRepository;
    private final PictureRepository pictureRepository;

    public OfferServiceImpl(OfferRepository offerRepository, ModelMapper modelMapper, ValidationUtil validationUtil, XmlParser xmlParser, CarRepository carRepository, SellerRepository sellerRepository, PictureRepository pictureRepository) {
        this.offerRepository = offerRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
        this.carRepository = carRepository;
        this.sellerRepository = sellerRepository;
        this.pictureRepository = pictureRepository;
    }

    @Override
    public boolean areImported() {
        return this.offerRepository.count() > 0;
    }

    @Override
    public String readOffersFileContent() throws IOException {
        return Files.readString(Paths.get(OFFERS_FILE_PATH));
    }

    @Override
    public String importOffers() throws IOException, JAXBException {
        StringBuilder result = new StringBuilder();
        OfferSeedRootDto offerSeedRootDto = this.xmlParser
                .convertFromFile(OFFERS_FILE_PATH, OfferSeedRootDto.class);

        offerSeedRootDto.getOffers()
                .forEach(offerSeedDto -> {
                    if(this.validationUtil.isValid(offerSeedDto)){
                        LocalDateTime addedOn = LocalDateTime.parse
                                (offerSeedDto.getAddedOn(),
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        if(this.offerRepository.getByDescriptionAndAddedOn
                                (offerSeedDto.getDescription(), addedOn) == null){
                            Offer offer = this.modelMapper.map(offerSeedDto, Offer.class);
                            Car car = this.carRepository.getOne(offerSeedDto.getCar().getId());
                            Seller seller = this.sellerRepository.getOne
                                    (offerSeedDto.getSeller().getId());
                            offer.setCar(car);
                            offer.setSeller(seller);
                            offer.setAddedOn(addedOn);
                            List<Picture> pictures = this.pictureRepository.getAllByCar(car);
                            offer.setPictures(pictures);
                            result.append(String.format
                                    ("Successfully import offer %s - %s",
                                            offer.getAddedOn(), offer.isHasGoldStatus()));
                            this.offerRepository.saveAndFlush(offer);
                        }else {
                            result.append("This offer is already in DB!");
                        }
                    }else{
                        result.append("Invalid offer");
                    }
                    result.append(System.lineSeparator());
                });
        return result.toString();
    }
}
