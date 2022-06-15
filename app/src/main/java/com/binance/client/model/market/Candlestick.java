package com.binance.client.model.market;

import com.binance.client.constant.BinanceApiConstants;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

public class Candlestick {

  private String symbol;
  private Long openTime;

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  private BigDecimal open;

  private BigDecimal high;

  private BigDecimal low;

  private BigDecimal close;

  private BigDecimal volume;

  private Long closeTime;

  private Double volume_24h = 0.0;

  private Double roi = 0.0;

  public Double getVolume_24h() {
    return volume_24h;
  }

  public void setVolume_24h(Double volume_24h) {
    this.volume_24h = volume_24h;
  }

  public Double getRoi() {
    return roi;
  }

  public void setRoi(Double roi) {
    this.roi = roi;
  }

  public Integer getTDhigh() {
    return TDhigh;
  }

  public void setTDhigh(Integer TDhigh) {
    this.TDhigh = TDhigh;
  }

  public Integer getTDlow() {
    return TDlow;
  }

  public void setTDlow(Integer TDlow) {
    this.TDlow = TDlow;
  }

  private Integer TDhigh = 0;
  private Integer TDlow = 0;

  private BigDecimal quoteAssetVolume;

  private Integer numTrades;

  private BigDecimal takerBuyBaseAssetVolume;

  private BigDecimal takerBuyQuoteAssetVolume;

  private BigDecimal ignore;

  public BigDecimal divide;

  public Long getOpenTime() {
    return openTime;
  }

  public void setOpenTime(Long openTime) {
    this.openTime = openTime;
  }

  public BigDecimal getOpen() {
    return open;
  }

  public void setOpen(BigDecimal open) {
    this.open = open;
  }

  public BigDecimal getHigh() {
    return high;
  }

  public void setHigh(BigDecimal high) {
    this.high = high;
  }

  public BigDecimal getLow() {
    return low;
  }

  public void setLow(BigDecimal low) {
    this.low = low;
  }

  public BigDecimal getClose() {
    return close;
  }

  public void setClose(BigDecimal close) {
    this.close = close;
  }

  public BigDecimal getVolume() {
    return volume;
  }

  public void setVolume(BigDecimal volume) {
    this.volume = volume;
  }

  public Long getCloseTime() {
    return closeTime;
  }

  public void setCloseTime(Long closeTime) {
    this.closeTime = closeTime;
  }

  public BigDecimal getQuoteAssetVolume() {
    return quoteAssetVolume;
  }

  public void setQuoteAssetVolume(BigDecimal quoteAssetVolume) {
    this.quoteAssetVolume = quoteAssetVolume;
  }

  public Integer getNumTrades() {
    return numTrades;
  }

  public void setNumTrades(Integer numTrades) {
    this.numTrades = numTrades;
  }

  public BigDecimal getTakerBuyBaseAssetVolume() {
    return takerBuyBaseAssetVolume;
  }

  public void setTakerBuyBaseAssetVolume(BigDecimal takerBuyBaseAssetVolume) {
    this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume;
  }

  public BigDecimal getTakerBuyQuoteAssetVolume() {
    return takerBuyQuoteAssetVolume;
  }

  public void setTakerBuyQuoteAssetVolume(BigDecimal takerBuyQuoteAssetVolume) {
    this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume;
  }

  public BigDecimal getIgnore() {
    return ignore;
  }

  public void setIgnore(BigDecimal ignore) {
    this.ignore = ignore;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
        .append("openTime", openTime)
        .append("open", open)
        .append("high", high)
        .append("low", low)
        .append("close", close)
        .append("volume", volume)
        .append("closeTime", closeTime)
        .append("quoteAssetVolume", quoteAssetVolume)
        .append("numTrades", numTrades)
        .append("takerBuyBaseAssetVolume", takerBuyBaseAssetVolume)
        .append("takerBuyQuoteAssetVolume", takerBuyQuoteAssetVolume)
        .append("ignore", ignore)
        .toString();
  }
}
