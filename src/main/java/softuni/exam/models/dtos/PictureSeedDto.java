package softuni.exam.models.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

public class PictureSeedDto {

    @Expose
    private String name;
    @Expose
    private String dateAndTime;
    @Expose
    private Long car;

    public PictureSeedDto() {
    }

    @NotNull
    @Length(min = 2, max = 20)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    @SerializedName("date_and_time")
    public String getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    @NotNull
    public Long getCar() {
        return car;
    }

    public void setCar(Long car) {
        this.car = car;
    }
}
