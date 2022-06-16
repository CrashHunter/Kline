package com.binance.client.model.custom;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public class HoldPriceItem {

  private String coin;

  private BigDecimal max;

  private BigDecimal min;

  //    private BigDecimal current;
  private BigDecimal holdNum = BigDecimal.ZERO;

  // 持仓成本
  private BigDecimal sumBuy = BigDecimal.ZERO;
  //持仓价
  private BigDecimal holdPrice= BigDecimal.ZERO;
  //当前价
  @Nullable public BigDecimal currentPrice = BigDecimal.ZERO;

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
