package com.dalonedrow.module.ff.ui;

import com.dalonedrow.module.ff.systems.FFInterface;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.systems.ConsoleInterface;

public class GUI extends ConsoleInterface {
    /**
     *
     */
    public GUI() {
        super.setInstance(this);
    }
    @Override
    public void prepareForRendering() throws RPGException {
        if (FFInterface.getInstance().hasFlag(FFInterface.WELCOME)) {
            WelcomeScreen.getInstance().render();
        } else {
            System.exit(1);
        }
    }
}
