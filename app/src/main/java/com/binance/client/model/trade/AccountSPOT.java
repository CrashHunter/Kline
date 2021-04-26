package com.binance.client.model.trade;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class AccountSPOT{

	@SerializedName("balances")
	private List<BalancesItem> balances;

	@SerializedName("makerCommission")
	private int makerCommission;

	@SerializedName("buyerCommission")
	private int buyerCommission;

	@SerializedName("canWithdraw")
	private boolean canWithdraw;

	@SerializedName("permissions")
	private List<String> permissions;

	@SerializedName("accountType")
	private String accountType;

	@SerializedName("sellerCommission")
	private int sellerCommission;

	@SerializedName("updateTime")
	private long updateTime;

	@SerializedName("canDeposit")
	private boolean canDeposit;

	@SerializedName("takerCommission")
	private int takerCommission;

	@SerializedName("canTrade")
	private boolean canTrade;

	public void setBalances(List<BalancesItem> balances){
		this.balances = balances;
	}

	public List<BalancesItem> getBalances(){
		return balances;
	}

	public void setMakerCommission(int makerCommission){
		this.makerCommission = makerCommission;
	}

	public int getMakerCommission(){
		return makerCommission;
	}

	public void setBuyerCommission(int buyerCommission){
		this.buyerCommission = buyerCommission;
	}

	public int getBuyerCommission(){
		return buyerCommission;
	}

	public void setCanWithdraw(boolean canWithdraw){
		this.canWithdraw = canWithdraw;
	}

	public boolean isCanWithdraw(){
		return canWithdraw;
	}

	public void setPermissions(List<String> permissions){
		this.permissions = permissions;
	}

	public List<String> getPermissions(){
		return permissions;
	}

	public void setAccountType(String accountType){
		this.accountType = accountType;
	}

	public String getAccountType(){
		return accountType;
	}

	public void setSellerCommission(int sellerCommission){
		this.sellerCommission = sellerCommission;
	}

	public int getSellerCommission(){
		return sellerCommission;
	}

	public void setUpdateTime(long updateTime){
		this.updateTime = updateTime;
	}

	public long getUpdateTime(){
		return updateTime;
	}

	public void setCanDeposit(boolean canDeposit){
		this.canDeposit = canDeposit;
	}

	public boolean isCanDeposit(){
		return canDeposit;
	}

	public void setTakerCommission(int takerCommission){
		this.takerCommission = takerCommission;
	}

	public int getTakerCommission(){
		return takerCommission;
	}

	public void setCanTrade(boolean canTrade){
		this.canTrade = canTrade;
	}

	public boolean isCanTrade(){
		return canTrade;
	}
}