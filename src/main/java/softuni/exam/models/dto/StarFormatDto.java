package softuni.exam.models.dto;

public class StarFormatDto {
    private String name;
    private Double lightYears;
    private String description;
    private ConstellationInfoDto constellation;

    public Double getLightYears() {
        return lightYears;
    }

    public void setLightYears(Double lightYears) {
        this.lightYears = lightYears;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ConstellationInfoDto getConstellation() {
        return constellation;
    }

    public void setConstellation(ConstellationInfoDto constellation) {
        this.constellation = constellation;
    }

    @Override
    public String toString() {
        return String.format("Star: %s\n" +
                "   *Distance: %.2f light years\n" +
                "   **Description: %s\n" +
                "   ***Constellation: %s\n",
                this.name, this.lightYears, this.description,
                this.constellation.getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
