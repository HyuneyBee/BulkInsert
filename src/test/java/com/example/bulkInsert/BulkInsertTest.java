package com.example.bulkInsert;

import com.example.bulkInsert.domain.Product;
import com.example.bulkInsert.repository.ProductRepository;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
import org.jeasy.random.randomizers.range.IntegerRangeRandomizer;
import org.jeasy.random.randomizers.range.LocalDateTimeRangeRandomizer;
import org.jeasy.random.randomizers.time.LocalDateTimeRandomizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BulkInsertTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final static EasyRandomParameters parameters = new EasyRandomParameters()
        .excludeField(FieldPredicates.named("id"))
        .stringLengthRange(10, 50)
        .randomize(FieldPredicates.named("price"), new IntegerRangeRandomizer(1000, 9999999))
        .randomize(FieldPredicates.named("starRating"), new IntegerRangeRandomizer(1, 6))
        .randomize(FieldPredicates.named("discountRate"), new IntegerRangeRandomizer(1, 100))
        .randomize(FieldPredicates.named("discountStart"), new LocalDateTimeRangeRandomizer(
            LocalDateTime.of(2015,1, 1,0,0),
            LocalDateTime.of(2020,12, 30,23,59)))
        .randomize(FieldPredicates.named("discountEnd"), new LocalDateTimeRangeRandomizer(
            LocalDateTime.of(2020,1, 1,0,0),
            LocalDateTime.of(2025,12, 30,23,59)))
        .randomize(FieldPredicates.named("createAt"), new LocalDateTimeRandomizer());

    @Test
    public void saveTest(){
        var easyRandom = new EasyRandom(parameters);

        IntStream.range(0, 1000000)
                .forEach(i -> {
                    var product = easyRandom.nextObject(Product.class);
                    productRepository.save(product);
                });
    }

    @Test
    public void saveAllTest(){
        var easyRandom = new EasyRandom(parameters);

        List<Product> productList = new ArrayList<>();

        IntStream.range(0,1000000)
            .forEach(i -> {
                var product = easyRandom.nextObject(Product.class);
                productList.add(product);
            });

        productRepository.saveAll(productList);
    }

    @Test
    @Rollback(value = false)
    public void jdbcTemplateTest() {
        var easyRandom = new EasyRandom(parameters);
        var batchSize = 10000;

        List<Product> productList = new ArrayList<>();

        IntStream.range(0, 1000000)
            .forEach(i -> {
                var product = easyRandom.nextObject(Product.class);
                productList.add(product);

                if ((i + 1) % batchSize == 0) {
                    batchInsert(productList);
                }
            });

        if(!productList.isEmpty()){
            batchInsert(productList);
        }
    }

    private void batchInsert(List<Product> productList){
        String sql = "insert into product (" +
            "title, " +
            "price, " +
            "category, " +
            "star_rating, " +
            "discount_rate, " +
            "discount_start, " +
            "discount_end, " +
            "create_at) " +
            "values (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Product p = productList.get(i);
                ps.setString(1, p.getTitle());
                ps.setInt(2, p.getPrice());
                ps.setString(3, p.getCategory().toString());
                ps.setInt(4, p.getStarRating());
                ps.setInt(5, p.getDiscountRate());
                ps.setTimestamp(6, Timestamp.valueOf(p.getDiscountStart()));
                ps.setTimestamp(7, Timestamp.valueOf(p.getDiscountEnd()));
                ps.setTimestamp(8, Timestamp.valueOf(p.getCreateAt()));
            }

            @Override
            public int getBatchSize() {
                return productList.size();
            }
        });

        productList.clear();
    }


}

