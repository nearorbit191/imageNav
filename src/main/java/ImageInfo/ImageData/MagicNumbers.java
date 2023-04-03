package ImageInfo.ImageData;

public enum MagicNumbers {
    PNG("89504e47"),
    JPG("ffd8ff"),
    GIF("47494638");




    public final String magicValues;


    private MagicNumbers(String magicValues) {
        this.magicValues = magicValues;
    }
}
