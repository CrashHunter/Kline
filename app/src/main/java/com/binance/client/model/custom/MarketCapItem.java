package com.binance.client.model.custom;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public class MarketCapItem {

  // 含USDT后缀
  private String coin;


  private Double marketcap = 0.0;
  private Double volume_24h = 0.0;

  public String getCoin() {
    return coin;
  }

  public void setCoin(String coin) {
    this.coin = coin;
  }

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
}
