package ch.qos.logback.classic.spi;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.BasicContextListener.UpdateType;

public class ListContextListener implements LoggerContextListener  {

    List<BasicContextListener.UpdateType> updateList = new ArrayList<>();

    @Override
    public boolean isResetResistant() {
        return false;
    }

    @Override
    public void onStart(final LoggerContext context) {
        updateList.add(UpdateType.START);
    }

    @Override
    public void onReset(final LoggerContext context) {
        updateList.add(UpdateType.RESET);
    }

    @Override
    public void onStop(final LoggerContext context) {
        updateList.add(UpdateType.STOP);

    }

    @Override
    public void onLevelChange(final Logger logger, final Level level) {
        updateList.add(UpdateType.LEVEL_CHANGE);
    }

}
