package com.dalonedrow.module.ff.systems;

import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFScriptTimer;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.systems.Speech;

public class FFSpeech extends Speech<FFInteractiveObject> {
    /** Creates a new instance of {@link FFSpeech}. 
     * @throws RPGException */
    public FFSpeech() throws RPGException {
        super.setInstance(this);
    }
    @Override
    public int ARX_SPEECH_AddSpeech(FFInteractiveObject io, int mood,
            String speech, long voixoff) {
        if (true) {
            Combat.getInstance().addMessage(Combat.MESSAGE_WARNING, speech);
        }
        return 0;
    }

}
