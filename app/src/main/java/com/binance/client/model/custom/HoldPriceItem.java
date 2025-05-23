package com.binance.client.model.custom;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public class HoldPriceItem {

  // 含USDT后缀
  private String coin;

  private BigDecimal max;

  private BigDecimal min;

  //    private BigDecimal current;
  private BigDecimal holdNum = BigDecimal.ZERO;

  // 持仓成本
  private BigDecimal sumBuy = BigDecimal.ZERO;
  // 持仓价
  private BigDecimal holdPrice = BigDecimal.ZERO;
  // 当前价
  @Nullable public BigDecimal currentPrice = BigDecimal.ZERO;

  // 当前价值
   public BigDecimal currentValue = BigDecimal.ZERO;

  // 回本需要涨多少倍
  @Nullable public BigDecimal multi = BigDecimal.ZERO;

  private Double volume_24h = 0.0;

  private Double marketcap = 0.0;

  public Double getMarketcap() {
    return marketcap;
  }

  public void setMarketcap(Double marketcap) {
    this.marketcap = marketcap;
  }

  public Double getVolume_24h() {
    return volume_24h;
  }

  public void setVolume_24h(Double volume_24h) {
    this.volume_24h = volume_24h;
  }

  public BigDecimal getCurrentValue() {
    return currentValue;
  }

  public void setCurrentValue(BigDecimal currentValue) {
    this.currentValue = currentValue;
  }

  @Nullable
  public BigDecimal getCurrentPrice() {
    return currentPrice;
  }

  public void setCurrentPrice(@Nullable BigDecimal currentPrice) {
    this.currentPrice = currentPrice;
  }

  @Nullable
  public BigDecimal getMulti() {
    return multi;
  }

  public void setMulti(@Nullable BigDecimal multi) {
    this.multi = multi;
  }

  public BigDecimal getRoi() {
    return roi;
  }

  public BigDecimal getHoldNum() {
    return holdNum;
  }

  public void setHoldNum(BigDecimal holdNum) {
    this.holdNum = holdNum;
  }

  public BigDecimal getSumBuy() {
    return sumBuy;
  }

  public void setSumBuy(BigDecimal sumBuy) {
    this.sumBuy = sumBuy;
  }

  public void setRoi(BigDecimal roi) {
    this.roi = roi;
  }

  private BigDecimal roi = new BigDecimal(-1.0);

  public BigDecimal getHoldPrice() {
    return holdPrice;
  }

  public void setHoldPrice(BigDecimal holdPrice) {
    this.holdPrice = holdPrice;
  }

  public BigDecimal getMin() {
    return min;
  }

  public void setMin(BigDecimal min) {
    this.min = min;
  }

  public BigDecimal getUpPer() {
    return upPer;
  }

  public void setUpPer(BigDecimal upPer) {
    this.upPer = upPer;
  }

  private BigDecimal downPer;
  private BigDecimal upPer;

  private BigDecimal rateInc;

  public BigDecimal getRateInc() {
    return rateInc;
  }

  public void setRateInc(BigDecimal rateInc) {
    this.rateInc = rateInc;
  }

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

  //    public BigDecimal getCurrent() {
  //        return current;
  //    }
  //
  //    public void setCurrent(BigDecimal current) {
  //        this.current = current;
  //    }

  public BigDecimal getDownPer() {
    return downPer;
  }

  public void setDownPer(BigDecimal downPer) {
    this.downPer = downPer;
  }
}
