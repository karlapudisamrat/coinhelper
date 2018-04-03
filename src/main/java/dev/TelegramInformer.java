package dev;

import dev.data.model.Coin;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.model.request.InputTextMessageContent;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.StatelessService;

/**
 * Periodic service, gets updates from telegram and responds for inline queries
 * by retrieving data from coin cache in {@link CoinReporter}
 */
public class TelegramInformer extends StatelessService {
    public static final String SELF_LINK = "telegram-informer";

    private static int messageOffset = 0;
    private State cachedState = null;
    private String[] defaultCoinsToDisplay = {"bitcoin", "ethereum", "ripple"};
    private TelegramBot bot;

    public static class State extends ServiceDocument {
        public Boolean maintenanceOn;
        public Long interval;

        public boolean shouldTurnOnMaintenance() {
            return maintenanceOn != null && maintenanceOn &&
                    interval != null && interval > 0;
        }
    }

    public TelegramInformer() {
        super(State.class);
    }

    @Override
    public void handleStart(Operation start) {
        if (!start.hasBody()) {
            start.fail(new IllegalArgumentException("Body is required"));
            return;
        }
        State state = start.getBody(State.class);
        if (state != null && state.shouldTurnOnMaintenance()) {
            super.toggleOption(ServiceOption.PERIODIC_MAINTENANCE, true);
            super.setMaintenanceIntervalMicros(TimeUnit.MILLISECONDS.toMicros(state.interval));
        }
        this.cachedState = state;

        // Initializing the bot here
        String TOKEN = System.getProperty("TOKEN", "");
        if(TOKEN.isEmpty()) {
            start.fail(new RuntimeException("Set Token as Java property"));
            return;
        }

        bot = new TelegramBot(TOKEN);
        start.complete();
    }

    @Override
    public void handleGet(Operation op) {
        op.setBody(this.cachedState).complete();
    }

    @Override
    public void handlePeriodicMaintenance(Operation post) {
        //getHost().log(Level.INFO, "Getting updates from Telegram ...");

        GetUpdates getUpdates = new GetUpdates().limit(100).offset(messageOffset).timeout(0);
        // Bot API to get updates
        GetUpdatesResponse updatesResponse = bot.execute(getUpdates);
        List<Update> updates = updatesResponse.updates();
        updates.forEach(u -> {
            messageOffset = u.updateId();
            //TODO Inform user that messaging bot is not supported
            if(u.message() != null) {
                //getHost().log(Level.INFO, "Message: " + u.message().text());
            }

            if(u.inlineQuery() != null) {
                String coinQuery = u.inlineQuery().query();
                getHost().log(Level.INFO, "Inline query: " + coinQuery);
                coinQuery = coinQuery.toLowerCase();

                List<Coin> coins = CoinReporter.getCoinCache().get(coinQuery);

                // If not found display the default coins
                if(coins == null || coins.isEmpty()) {
                    coins = Arrays.stream(defaultCoinsToDisplay)
                            .map(c -> CoinReporter.getCoinCache().get(c).get(0))
                            .collect(Collectors.toList());
                }

                //coins.forEach(c -> getHost().log(Level.INFO, " " + c.getName()));

                List<InlineQueryResultArticle> results = coins.stream()
                        .map(coin -> new InlineQueryResultArticle(coin.getSymbol(), coin.getName(),
                                            // Message shown to the user after selecting one of the inline results
                                            new InputTextMessageContent(
                                                    "*"+coin.getName()+"* ("+coin.getSymbol()+")\n" +
                                                    "USD: *"+coin.getPrice_usd()+"*$\n" +
                                                    "1hr change: *"+coin.getPercent_change_1h()+"*%")
                                                    .parseMode(ParseMode.Markdown))
                                    .thumbUrl(String.format(CoinReporter.COIN_LOGO_URL, coin.getId())))
                        .limit(10)
                        .collect(Collectors.toList());

                // Bot API to answer inline queries
                bot.execute(new AnswerInlineQuery(u.inlineQuery().id(), results.toArray(new InlineQueryResultArticle[results.size()])));
            }
        });

        // Increment offset to not receive previous updates
        messageOffset += 1;
        post.complete();
    }
}