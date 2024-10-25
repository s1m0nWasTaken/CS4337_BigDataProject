package CS4337.Project;

enum ShopType {
    CLOTHING,
    ELECTRONICS,
    FOOD,
    BOOKS,
    TOYS,
    OTHER
}

public class Shop {
    private int id;
    private int shopOwnerid;
    private String shopName;
    private String description;
    private String imageData;
    private ShopType shopType;
    private String shopEmail;


    public Shop() {
    }

    public Shop(int id, int shopOwnerid, String shopName, String description, String imageData, ShopType shopType, String shopEmail) {
        this.id = id;
        this.shopOwnerid = shopOwnerid;
        this.shopName = shopName;
        this.description = description;
        this.imageData = imageData;
        this.shopType = shopType;
        this.shopEmail = shopEmail;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getShopOwnerid() {
        return shopOwnerid;
    }

    public void setShopOwnerid(int shopOwnerid) {
        this.shopOwnerid = shopOwnerid;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public ShopType getShopType() {
        return shopType;
    }

    public void setShopType(ShopType shopType) {
        this.shopType = shopType;
    }

    public String getShopEmail() {
        return shopEmail;
    }

    public void setShopEmail(String shopEmail) {
        this.shopEmail = shopEmail;
    }

}