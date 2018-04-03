package dev;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceHost;
import com.vmware.xenon.services.common.*;

class Host extends ServiceHost {

    public static void main(String[] args) throws Throwable {
        Host h = new Host();
        h.initialize(args);
        h.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            h.log(Level.WARNING, "Host stopping ...");
            h.stop();
            h.log(Level.WARNING, "Host is stopped");
        }));
    }

    @Override
    public ServiceHost start() throws Throwable {
        super.start();

        startDefaultCoreServicesSynchronously();
        super.startService(new RootNamespaceService());
        setAuthorizationContext(null);

        // Start periodic services
        TelegramInformer.State pState = new TelegramInformer.State();
        pState.maintenanceOn = true;
        pState.interval = 500l;
        super.startService(Operation.createPost(this, TelegramInformer.SELF_LINK).setBody(pState),
                new TelegramInformer());

        CoinReporter.State state = new CoinReporter.State();
        state.maintenanceOn = true;
        state.interval = TimeUnit.SECONDS.toMillis(30);
        super.startService(Operation.createPost(this, CoinReporter.SELF_LINK).setBody(state),
                new CoinReporter());

        super.log(Level.INFO, "Started services successfully ...");
        return this;
    }
}
