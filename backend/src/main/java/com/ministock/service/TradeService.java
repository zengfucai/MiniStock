package com.ministock.service;

import com.ministock.entity.Account;
import com.ministock.entity.FundRecord;
import com.ministock.entity.Position;
import com.ministock.entity.Stock;
import com.ministock.entity.TradeApplication;
import com.ministock.entity.TradeRecord;
import com.ministock.exception.BusinessException;
import com.ministock.mapper.AccountMapper;
import com.ministock.mapper.FundRecordMapper;
import com.ministock.mapper.PositionMapper;
import com.ministock.mapper.StockMapper;
import com.ministock.mapper.TradeApplicationMapper;
import com.ministock.mapper.TradeRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TradeService {

    private final TradeApplicationMapper applicationMapper;
    private final TradeRecordMapper tradeRecordMapper;
    private final FundRecordMapper fundRecordMapper;
    private final AccountMapper accountMapper;
    private final PositionMapper positionMapper;
    private final StockMapper stockMapper;

    public TradeService(
            TradeApplicationMapper applicationMapper,
            TradeRecordMapper tradeRecordMapper,
            FundRecordMapper fundRecordMapper,
            AccountMapper accountMapper,
            PositionMapper positionMapper,
            StockMapper stockMapper
    ) {
        this.applicationMapper = applicationMapper;
        this.tradeRecordMapper = tradeRecordMapper;
        this.fundRecordMapper = fundRecordMapper;
        this.accountMapper = accountMapper;
        this.positionMapper = positionMapper;
        this.stockMapper = stockMapper;
    }

    public List<Position> getPositions(Long userId) {
        return positionMapper.findByUserId(userId);
    }

    public List<TradeApplication> getApplications(Long userId) {
        return applicationMapper.findByUserId(userId);
    }

    public List<TradeApplication> getAllApplications() {
        return applicationMapper.findAll();
    }

    public List<TradeRecord> getTradeRecords(Long userId) {
        return tradeRecordMapper.findByUserId(userId);
    }

    public List<TradeRecord> getAllTradeRecords() {
        return tradeRecordMapper.findAll();
    }

    @Transactional
    public void createApplication(
            Long userId,
            Long stockId,
            String type,
            Integer quantity
    ) {

        if (!"BUY".equals(type)
                && !"SELL".equals(type)) {

            throw new BusinessException(
                    "交易类型错误"
            );
        }

        if (quantity == null || quantity <= 0) {

            throw new BusinessException(
                    "交易数量必须大于0"
            );
        }

        Stock stock =
                stockMapper.findById(stockId);

        if (stock == null) {

            throw new BusinessException(
                    "股票不存在"
            );
        }

        if (!"NORMAL".equals(
                stock.getStatus()
        )) {

            throw new BusinessException(
                    "股票当前不可交易"
            );
        }

        BigDecimal amount =
                stock.getPrice()
                        .multiply(
                                BigDecimal.valueOf(quantity)
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        if ("BUY".equals(type)) {

            Account account =
                    accountMapper.findByUserId(
                            userId
                    );

            if (account == null) {

                throw new BusinessException(
                        "账户不存在"
                );
            }

            if (account.getBalance()
                    .compareTo(amount) < 0) {

                throw new BusinessException(
                        "账户余额不足"
                );
            }

        } else {

            Position position =
                    positionMapper.findByUserAndStock(
                            userId,
                            stockId
                    );

            if (position == null
                    || position.getQuantity() < quantity) {

                throw new BusinessException(
                        "持仓数量不足"
                );
            }
        }

        TradeApplication application =
                new TradeApplication();

        application.setUserId(userId);
        application.setStockId(stockId);
        application.setType(type);
        application.setQuantity(quantity);
        application.setPrice(stock.getPrice());
        application.setAmount(amount);

        applicationMapper.insert(application);
    }

    @Transactional
    public void approve(Long applicationId) {

        TradeApplication application =
                applicationMapper.findById(
                        applicationId
                );

        if (application == null) {

            throw new BusinessException(
                    "交易申请不存在"
            );
        }

        if (!"PENDING".equals(
                application.getStatus()
        )) {

            throw new BusinessException(
                    "交易申请不是待审核状态"
            );
        }

        Stock stock =
                stockMapper.findById(
                        application.getStockId()
                );

        if (stock == null) {

            throw new BusinessException(
                    "股票不存在"
            );
        }

        if (!"NORMAL".equals(
                stock.getStatus()
        )) {

            throw new BusinessException(
                    "股票当前不可交易"
            );
        }

        BigDecimal amount =
                application.getAmount();

        if ("BUY".equals(
                application.getType()
        )) {

            Account account =
                    accountMapper.findByUserId(
                            application.getUserId()
                    );

            if (account == null) {

                throw new BusinessException(
                        "账户不存在"
                );
            }

            if (account.getBalance()
                    .compareTo(amount) < 0) {

                throw new BusinessException(
                        "账户余额不足"
                );
            }

            int result =
                    accountMapper.subtractBalance(
                            application.getUserId(),
                            amount
                    );

            if (result == 0) {

                throw new BusinessException(
                        "账户余额不足"
                );
            }

            Position position =
                    positionMapper.findByUserAndStock(
                            application.getUserId(),
                            application.getStockId()
                    );

            if (position == null) {

                position =
                        new Position();

                position.setUserId(
                        application.getUserId()
                );

                position.setStockId(
                        application.getStockId()
                );

                position.setQuantity(
                        application.getQuantity()
                );

                position.setAveragePrice(
                        application.getPrice()
                );

                positionMapper.insert(
                        position
                );

            } else {

                int oldQuantity =
                        position.getQuantity();

                int newQuantity =
                        oldQuantity +
                                application.getQuantity();

                BigDecimal oldCost =
                        position.getAveragePrice()
                                .multiply(
                                        BigDecimal.valueOf(
                                                oldQuantity
                                        )
                                );

                BigDecimal newCost =
                        application.getPrice()
                                .multiply(
                                        BigDecimal.valueOf(
                                                application.getQuantity()
                                        )
                                );

                BigDecimal average =
                        oldCost
                                .add(newCost)
                                .divide(
                                        BigDecimal.valueOf(
                                                newQuantity
                                        ),
                                        2,
                                        RoundingMode.HALF_UP
                                );

                position.setQuantity(
                        newQuantity
                );

                position.setAveragePrice(
                        average
                );

                positionMapper.update(
                        position
                );
            }

            Account newAccount =
                    accountMapper.findByUserId(
                            application.getUserId()
                    );

            FundRecord fundRecord =
                    new FundRecord();

            fundRecord.setUserId(
                    application.getUserId()
            );

            fundRecord.setType(
                    "TRADE_BUY"
            );

            fundRecord.setAmount(
                    amount.negate()
            );

            fundRecord.setBalanceAfter(
                    newAccount.getBalance()
            );

            fundRecord.setDescription(
                    "买入股票：" +
                            stock.getName()
            );

            fundRecordMapper.insert(
                    fundRecord
            );

        } else {

            Position position =
                    positionMapper.findByUserAndStock(
                            application.getUserId(),
                            application.getStockId()
                    );

            if (position == null
                    || position.getQuantity()
                    < application.getQuantity()) {

                throw new BusinessException(
                        "持仓数量不足"
                );
            }

            int newQuantity =
                    position.getQuantity()
                            - application.getQuantity();

            if (newQuantity == 0) {

                positionMapper.delete(
                        position.getId()
                );

            } else {

                position.setQuantity(
                        newQuantity
                );

                positionMapper.update(
                        position
                );
            }

            int result =
                    accountMapper.addBalance(
                            application.getUserId(),
                            amount
                    );

            if (result == 0) {

                throw new BusinessException(
                        "余额增加失败"
                );
            }

            Account account =
                    accountMapper.findByUserId(
                            application.getUserId()
                    );

            FundRecord fundRecord =
                    new FundRecord();

            fundRecord.setUserId(
                    application.getUserId()
            );

            fundRecord.setType(
                    "TRADE_SELL"
            );

            fundRecord.setAmount(
                    amount
            );

            fundRecord.setBalanceAfter(
                    account.getBalance()
            );

            fundRecord.setDescription(
                    "卖出股票：" +
                            stock.getName()
            );

            fundRecordMapper.insert(
                    fundRecord
            );
        }

        TradeRecord record =
                new TradeRecord();

        record.setUserId(
                application.getUserId()
        );

        record.setStockId(
                application.getStockId()
        );

        record.setType(
                application.getType()
        );

        record.setQuantity(
                application.getQuantity()
        );

        record.setPrice(
                application.getPrice()
        );

        record.setAmount(
                amount
        );

        tradeRecordMapper.insert(
                record
        );

        applicationMapper.review(
                applicationId,
                "APPROVED",
                "管理员审核通过"
        );
    }

    @Transactional
    public void reject(
            Long applicationId,
            String reason
    ) {

        TradeApplication application =
                applicationMapper.findById(
                        applicationId
                );

        if (application == null) {

            throw new BusinessException(
                    "交易申请不存在"
            );
        }

        if (!"PENDING".equals(
                application.getStatus()
        )) {

            throw new BusinessException(
                    "交易申请不是待审核状态"
            );
        }

        if (reason == null
                || reason.isBlank()) {

            reason = "管理员拒绝";
        }

        applicationMapper.review(
                applicationId,
                "REJECTED",
                reason
        );
    }
}