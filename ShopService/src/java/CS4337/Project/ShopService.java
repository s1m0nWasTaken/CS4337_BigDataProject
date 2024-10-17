package java.CS4337.Project;

import jakarta.websocket.server.PathParam;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@RestController
public class ShopService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(ShopService.class, args);
    }

    @GetMapping("/shop")
    public List<Map<String, Object>> shop() {
        List<Map<String, Object>> users;
        try {
            users = jdbcTemplate.queryForList("SELECT * FROM Shop");
        } catch (DataAccessException e) {
            jdbcTemplate.execute("USE user_service;");
            jdbcTemplate.execute(
                    "CREATE TABLE `ShopItem` ("
                            + "id INT AUTO_INCREMENT PRIMARY KEY, "
                            + "shopOwnerid INT NOT NULL, "
                            + "shopName VARCHAR(255) NOT NULL, "
                            + "imageData VARCHAR(255), "
                            + "description VARCHAR(255), "
                            + "shopType ENUM(CLOTHING, ELECTRONICS,FOOD, BOOKS, TOYS, OTHER) NOT NULL, "
                            + "shopEmail VARCHAR (255) NOT NULL);");
            users = jdbcTemplate.queryForList(" SELECT * FROM Shop ");
        }
    }

    @PostMapping("/shop")
    public Map<String, Object> addShop(@RequestBody Shop shop) {
        try {
            SimpleJdbcInsert inserter = new SimpleJdbcInsert(jdbcTemplate);
            String sqlInsert =
                    "INSERT INTO 'Shop' (shopOwnerid, shopName, imageData, description, shopType, shopEmail) " + "VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(
                    sqlInsert,
                    shop.getShopOwnerid(),
                    shop.getShopName(),
                    shop.getImageData(),
                    shop.getDescription(),
                    shop.getShopType().name(),
                    shop.getShopEmail());
            return Map.of("success", 1);
        } catch (TransientDataAccessResourceException e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/shopitem")
    public List<Map<String, Object>> shopItem() {
        List<Map<String, Object>> users;
        try {
            users = jdbcTemplate.queryForList("SELECT * FROM ShopItem");
        } catch (DataAccessException e) {
            jdbcTemplate.execute("USE user_service;");
            jdbcTemplate.execute(
                    "CREATE TABLE `ShopItem` ("
                            + "id INT AUTO_INCREMENT PRIMARY KEY, "
                            + "shopid INT NOT NULL, "
                            + "price DOUBLE(10, 2), "
                            + "stock INT NOT NULL"
                            + "picture VARCHAR(255), "
                            + "description VARCHAR(255), "
                            + "canUpdate BOOL DEFAULT TRUE, "
                            + "isHidden BOOL DEFAULT TRUE);");
            users = jdbcTemplate.queryForList(" SELECT * FROM ShopItem ");
        }
    }

    @PostMapping("/shopItem")
    public Map<String, Object> addShopItem(@RequestBody ShopItem shopItem) {
        try {
            SimpleJdbcInsert inserter = new SimpleJdbcInsert(jdbcTemplate);
            String sqlInsert =
                    "INSERT INTO 'ShopItem' (shopid, price, stock, picture, description) " + "VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(
                    sqlInsert,
                    shopItem.getShopid(),
                    shopItem.getPrice(),
                    shopItem.getStock(),
                    shopItem.getPicture(),
                    shopItem.getDescription());
            return Map.of("success", 1);
        } catch (TransientDataAccessResourceException e) {
            return Map.of("error", e.getMessage());
        }
    }

}