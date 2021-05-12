package com.binance.client.model.custom;

import com.binance.client.constant.BinanceApiConstants;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

public class DownPerItem {

  private String coin;

  private BigDecimal max;

  private BigDecimal current;

  private BigDecimal downPer;

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public BigDecimal getMax() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max = max;
    }

    public BigDecimal getCurrent() {
        return current;
    }

    public void setCurrent(BigDecimal current) {
        this.current = current;
    }

    public BigDecimal getDownPer() {
        return downPer;
    }

    public void setDownPer(BigDecimal downPer) {
        this.downPer = downPer;
    }
}
