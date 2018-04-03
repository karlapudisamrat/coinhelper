package dev;

import dev.data.model.Coin;
import dev.data.model.Coins;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.vmware.xenon.common.DeferredResult;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.StatelessService;

/**
 * Periodically fetches all coin data from coinmarketcap.com and build an in memory cache
 * for faster retrieval of coin data
 */
public class CoinReporter extends StatelessService {
    public static final String SELF_LINK = "coin-reporter";
    private static final String COIN_MARKET_CAP_API = "https://api.coinmarketcap.com/v1/ticker/?limit=2000";
    public static String COIN_LOGO_URL = "https://files.coinmarketcap.com/static/img/coins/32x32/%s.png";
    private static URI coinMarketCapUri;
    private static Map<String, List<Coin>> coinCache = new HashMap<>();

    private State cachedState = null;

    public static class State extends ServiceDocument {
        public Boolean maintenanceOn;
        public Long interval;

        public boolean shouldTurnOnMaintenance() {
            return maintenanceOn != null && maintenanceOn &&
                    interval != null && interval > 0;
        }
    }

    public CoinReporter() {
        super(State.class);
    }

    @Override
    public void handleStart(Operation start) {
        State state = start.getBody(State.class);
        if (state != null && state.shouldTurnOnMaintenance()) {
            super.toggleOption(ServiceOption.PERIODIC_MAINTENANCE, true);
            super.setMaintenanceIntervalMicros(TimeUnit.MILLISECONDS.toMicros(state.interval));
        }
        this.cachedState = state;

        try {
            coinMarketCapUri = new URI(COIN_MARKET_CAP_API);
        } catch (URISyntaxException e) {}

        fetchCoinData();
        start.complete();
    }

    @Override
    public void handleGet(Operation op) {
        op.setBody(this.cachedState).complete();
    }

    @Override
    public void handlePeriodicMaintenance(Operation post) {
        fetchCoinData().whenCompleteNotify(post);
    }

    private DeferredResult<Operation> fetchCoinData() {
        //getHost().log(Level.INFO, "Fetching coin data .....");
        return this.sendWithDeferredResult(Operation.createGet(coinMarketCapUri))
                .whenComplete((o, e) -> {
                    if(e != null) {
                        getHost().log(Level.WARNING, "Error fetching coin data", e);
                        return;
                    }

                    // Build coin cache here
                    Map<String, List<Coin>> newCoinCache = new HashMap<>();

                    // De-serializing
                    Coins coins = new Gson().fromJson(String.valueOf(o.getBodyRaw()), Coins.class);
                    coins.forEach(coin -> {
                        //getHost().log(Level.INFO, "Coin: " + coin.getSymbol());
                        String temp = "";
                        for(int i=0; i<coin.getName().length(); i++) {
                            temp += coin.getName().charAt(i);
                            temp = temp.toLowerCase();
                            List<Coin> coinsSubset = newCoinCache.get(temp);
                            if(coinsSubset == null) {
                                coinsSubset = new ArrayList<>();
                            }

                            coinsSubset.add(coin);
                            newCoinCache.put(temp, coinsSubset);
                        }
                    });

                    // Once complete, replace the existing cache
                    setCoinCache(newCoinCache);
                });
    }

    static Map<String, List<Coin>> getCoinCache() {
        return coinCache;
    }

    private void setCoinCache(Map<String, List<Coin>> newCoinCache) {
        clearCoinCache();
        coinCache = newCoinCache;
    }

    private void clearCoinCache() {
        coinCache = null;
    }
}