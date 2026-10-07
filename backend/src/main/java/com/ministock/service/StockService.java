package com.ministock.service;

import com.ministock.entity.Stock;
import com.ministock.mapper.StockMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class StockService {

    private final StockMapper stockMapper;

    public StockService(StockMapper stockMapper) {
        this.stockMapper = stockMapper;
    }

    public List<Stock> findAll() {
        return stockMapper.findAll();
    }

    public Stock findById(Long id) {

        Stock stock = stockMapper.findById(id);

        if (stock == null) {
            throw new RuntimeException("股票不存在");
        }

        return stock;
    }

    public void save(Stock stock) {

        if (stock.getCode() == null
                || stock.getCode().isBlank()) {
            throw new RuntimeException("股票代码不能为空");
        }

        if (stock.getName() == null
                || stock.getName().isBlank()) {
            throw new RuntimeException("股票名称不能为空");
        }

        if (stock.getPrice() == null
                || stock.getPrice().signum() <= 0) {
            throw new RuntimeException("股票价格必须大于0");
        }

        if (stock.getChangeRate() == null) {
            stock.setChangeRate(BigDecimal.ZERO);
        }

        if (stock.getStatus() == null) {
            stock.setStatus("NORMAL");
        }

        if (stock.getId() == null) {

            BigDecimal basePrice =
                    stock.getPrice()
                            .setScale(2, RoundingMode.HALF_UP);

            BigDecimal changeRate =
                    stock.getChangeRate()
                            .setScale(2, RoundingMode.HALF_UP);

            BigDecimal price =
                    calculatePrice(
                            basePrice,
                            changeRate
                    );

            stock.setBasePrice(basePrice);
            stock.setPrice(price);
            stock.setChangeRate(changeRate);

            stockMapper.insert(stock);

            return;
        }

        Stock oldStock =
                stockMapper.findById(stock.getId());

        if (oldStock == null) {
            throw new RuntimeException("股票不存在");
        }

        BigDecimal basePrice =
                oldStock.getBasePrice();

        if (basePrice == null
                || basePrice.signum() <= 0) {

            basePrice =
                    oldStock.getPrice();
        }

        BigDecimal oldPrice =
                oldStock.getPrice();

        BigDecimal oldChangeRate =
                oldStock.getChangeRate();

        BigDecimal newPrice =
                stock.getPrice();

        BigDecimal newChangeRate =
                stock.getChangeRate();

        boolean priceChanged =
                oldPrice == null
                        || newPrice.compareTo(oldPrice) != 0;

        boolean changeRateChanged =
                oldChangeRate == null
                        || newChangeRate.compareTo(oldChangeRate) != 0;

        if (priceChanged && !changeRateChanged) {

            newPrice =
                    newPrice.setScale(
                            2,
                            RoundingMode.HALF_UP
                    );

            newChangeRate =
                    calculateChangeRate(
                            basePrice,
                            newPrice
                    );

        } else {

            newChangeRate =
                    newChangeRate.setScale(
                            2,
                            RoundingMode.HALF_UP
                    );

            newPrice =
                    calculatePrice(
                            basePrice,
                            newChangeRate
                    );
        }

        stock.setBasePrice(
                basePrice.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );

        stock.setPrice(newPrice);

        stock.setChangeRate(
                newChangeRate
        );

        stockMapper.update(stock);
    }

    private BigDecimal calculatePrice(
            BigDecimal basePrice,
            BigDecimal changeRate
    ) {

        return basePrice
                .multiply(
                        BigDecimal.ONE.add(
                                changeRate.divide(
                                        BigDecimal.valueOf(100),
                                        10,
                                        RoundingMode.HALF_UP
                                )
                        )
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateChangeRate(
            BigDecimal basePrice,
            BigDecimal price
    ) {

        return price
                .divide(
                        basePrice,
                        10,
                        RoundingMode.HALF_UP
                )
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    public void delete(Long id) {

        stockMapper.delete(id);
    }
}