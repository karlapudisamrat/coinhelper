package dev.data.model;

import com.google.gson.annotations.SerializedName;

/**
 *   Expecting JSON from coinmarketcap.com to be in this format
     {
     "id": "bitcoin",
     "name": "Bitcoin",
     "symbol": "BTC",
     "rank": "1",
     "price_usd": "11795.3",
     "price_btc": "1.0",
     "24h_volume_usd": "8432160000.0",
     "market_cap_usd": "198542759499",
     "available_supply": "16832362.0",
     "total_supply": "16832362.0",
     "max_supply": "21000000.0",
     "percent_change_1h": "-0.09",
     "percent_change_24h": "2.42",
     "percent_change_7d": "1.74",
     "last_updated": "1517188767"
     }
 */
public class Coin {
    private String id;
    private String name;
    private String symbol;
    private String rank;
    private double price_usd;
    private double price_btc;
    @SerializedName("24h_volume_usd")
    private float volume24Hr;
    private long market_cap_usd;
    private double available_supply;
    private double total_supply;
    private double max_supply;
    private float percent_change_1h;
    private float percent_change_24h;
    private float percent_change_27d;
    private long last_updated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public double getPrice_usd() {
        return price_usd;
    }

    public void setPrice_usd(double price_usd) {
        this.price_usd = price_usd;
    }

    public double getPrice_btc() {
        return price_btc;
    }

    public void setPrice_btc(double price_btc) {
        this.price_btc = price_btc;
    }

    public float getVolume24Hr() {
        return volume24Hr;
    }

    public void setVolume24Hr(float volume24Hr) {
        this.volume24Hr = volume24Hr;
    }

    public long getMarket_cap_usd() {
        return market_cap_usd;
    }

    public void setMarket_cap_usd(long market_cap_usd) {
        this.market_cap_usd = market_cap_usd;
    }

    public double getAvailable_supply() {
        return available_supply;
    }

    public void setAvailable_supply(double available_supply) {
        this.available_supply = available_supply;
    }

    public double getTotal_supply() {
        return total_supply;
    }

    public void setTotal_supply(double total_supply) {
        this.total_supply = total_supply;
    }

    public double getMax_supply() {
        return max_supply;
    }

    public void setMax_supply(double max_supply) {
        this.max_supply = max_supply;
    }

    public float getPercent_change_1h() {
        return percent_change_1h;
    }

    public void setPercent_change_1h(float percent_change_1h) {
        this.percent_change_1h = percent_change_1h;
    }

    public float getPercent_change_24h() {
        return percent_change_24h;
    }

    public void setPercent_change_24h(float percent_change_24h) {
        this.percent_change_24h = percent_change_24h;
    }

    public float getPercent_change_27d() {
        return percent_change_27d;
    }

    public void setPercent_change_27d(float percent_change_27d) {
        this.percent_change_27d = percent_change_27d;
    }

    public long getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(long last_updated) {
        this.last_updated = last_updated;
    }
}
