package com.binance.client.model.trade;

import com.google.gson.annotations.SerializedName;

public class BalancesItem{

	@SerializedName("asset")
	private String asset;

	@SerializedName("free")
	private String free;

	@SerializedName("locked")
	private String locked;

	public void setAsset(String asset){
		this.asset = asset;
	}

	public String getAsset(){
		return asset;
	}

	public void setFree(String free){
		this.free = free;
	}

	public String getFree(){
		return free;
	}

	public void setLocked(String locked){
		this.locked = locked;
	}

	public String getLocked(){
		return locked;
	}
}