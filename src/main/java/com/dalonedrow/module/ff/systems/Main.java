package com.dalonedrow.module.ff.systems;

import com.dalonedrow.module.ff.graph.FFWorldMap;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.ui.FFGUI;
import com.dalonedrow.rpg.base.flyweights.RPGException;

public class Main {

    public static void main(String[] args) {
        try {
            new FFController();
            new FFInteractive();
            new FFScript();
            new FFSpeech();
            new FFSpellController();
            new FFText();
            new FFGUI();
            FFWorldMap.getInstance().load();
            GameCycle.getInstance();
            // create new hero
            ((FFInteractive) FFInteractive.getInstance()).newHero();
            // put hero in room 1
            FFInteractiveObject io = ((FFController)
                    FFController.getInstance()).getPlayerIO();
            io.setPosition(
                    FFWorldMap.getInstance().getRoom(
                            1).getMainNode().getLocation());
            io = null;
            // set welcome screen
            FFInterface.getInstance().addFlag(FFInterface.WELCOME);
            while (!FFController.getInstance().isGameOver()) {
                GameCycle.getInstance().execute();
            }
        } catch (RPGException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
    }

}
