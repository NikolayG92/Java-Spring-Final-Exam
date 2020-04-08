package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.constants.GlobalConstants;
import softuni.exam.models.dtos.SellerSeedRootDto;
import softuni.exam.models.entities.Rating;
import softuni.exam.models.entities.Seller;
import softuni.exam.repository.SellerRepository;
import softuni.exam.service.SellerService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static softuni.exam.constants.GlobalConstants.SELLERS_FILE_PATH;

@Service
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final ModelMapper modelMapper;
    private final XmlParser xmlParser;
    private final ValidationUtil validationUtil;

    public SellerServiceImpl(SellerRepository sellerRepository, ModelMapper modelMapper, XmlParser xmlParser, ValidationUtil validationUtil) {
        this.sellerRepository = sellerRepository;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;
        this.validationUtil = validationUtil;
    }

    @Override
    public boolean areImported() {
        return this.sellerRepository.count() > 0;
    }

    @Override
    public String readSellersFromFile() throws IOException {
        return Files.readString(Paths.get(SELLERS_FILE_PATH));
    }

    @Override
    public String importSellers() throws IOException, JAXBException {
        StringBuilder result = new StringBuilder();
        SellerSeedRootDto sellerSeedRootDto = this.xmlParser
                .convertFromFile(SELLERS_FILE_PATH, SellerSeedRootDto.class);
        sellerSeedRootDto.getSellers()
                .forEach(sellerSeedDto -> {
                    if(this.validationUtil.isValid(sellerSeedDto) &&
                    this.isValidRating(sellerSeedDto.getRating())){
                       if(this.sellerRepository.getByEmail(sellerSeedDto.getEmail()) == null){
                           Seller seller = this.modelMapper.map(sellerSeedDto, Seller.class);
                           Rating rating = Rating.valueOf(sellerSeedDto.getRating());
                           seller.setRating(rating);

                           result.append(String.format
                                   ("Successfully import seller %s - %s",
                                   seller.getLastName(), seller.getEmail()));

                           this.sellerRepository.saveAndFlush(seller);
                       }else {
                           result.append("This seller is already in DB!");
                       }
                    }else{
                        result.append("Invalid seller");
                    }
                    result.append(System.lineSeparator());
                });

        return result.toString();
    }

    private boolean isValidRating(String rating) {
        return rating.equals("BAD") || rating.equals("GOOD") || rating.equals("UNKNOWN");
    }
}
