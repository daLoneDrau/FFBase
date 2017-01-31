package com.dalonedrow.module.ff.systems;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFScriptTimer;
import com.dalonedrow.module.ff.rpg.FFScriptable;
import com.dalonedrow.module.ff.rpg.FFStackedEvent;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.constants.ScriptConsts;
import com.dalonedrow.rpg.base.flyweights.BaseInteractiveObject;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.systems.Script;

/**
 * @author drau
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class FFScript extends
		Script<FFInteractiveObject, FFScriptTimer, FFScriptable, FFStackedEvent> {
	private FFStackedEvent[]			eventstack;
	private final int					eventTotalCount	= 0;
	private final FFInteractiveObject	master;
	private FFStackedEvent[]			postScriptStack;
	/** the list of script timers. */
	private FFScriptTimer[]				scriptTimers;
	/** Creates a new instance of {@link FFScript}. 
	 * @throws RPGException */
	public FFScript() throws RPGException {
		master = ((FFInteractive) Interactive.getInstance()).getMasterScript();
		eventStackInit();
		super.setMaxTimerScript(100);
		scriptTimers = new FFScriptTimer[super.getMaxTimerScript()];
		for (int i = super.getMaxTimerScript() - 1; i >= 0; i--) {
		    scriptTimers[i] = new FFScriptTimer();
		}
		// init global params
        super.setGlobalVariable("PLAYERCASTING", 0);
        super.setGlobalVariable("COMBATROUND", 0);
		super.setInstance(this);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearAdditionalEventStacks() {
		for (int i = 0; i < ScriptConsts.MAX_EVENT_STACK; i++) {
			if (postScriptStack[i].exists()) {
				postScriptStack[i].setParams(null);
				postScriptStack[i].setEventname(null);
				postScriptStack[i].setSender(null);
				postScriptStack[i].setExist(false);
				postScriptStack[i].setIo(null);
				postScriptStack[i].setMsg(0);
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearAdditionalEventStacksForIO(final FFInteractiveObject io) {
		for (int i = 0; i < ScriptConsts.MAX_EVENT_STACK; i++) {
			if (postScriptStack[i].exists()
					&& io.equals(postScriptStack[i].getIo())) {
				postScriptStack[i].setParams(null);
				postScriptStack[i].setEventname(null);
				postScriptStack[i].setSender(null);
				postScriptStack[i].setExist(false);
				postScriptStack[i].setIo(null);
				postScriptStack[i].setMsg(0);
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void destroyScriptTimers() {
		if (scriptTimers != null) {
			for (int i = scriptTimers.length - 1; i >= 0; i--) {
				scriptTimers[i] = null;
			}
			scriptTimers = null;
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void eventStackInit() {
		eventstack = new FFStackedEvent[ScriptConsts.MAX_EVENT_STACK];
		for (int i = 0; i < ScriptConsts.MAX_EVENT_STACK; i++) {
			eventstack[i] = new FFStackedEvent();
		}
		postScriptStack = new FFStackedEvent[ScriptConsts.MAX_EVENT_STACK];
		for (int i = 0; i < ScriptConsts.MAX_EVENT_STACK; i++) {
			postScriptStack[i] = new FFStackedEvent();
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void executeAdditionalStacks() {
		transferPostScriptStack();
	}
	/**
	 * Gets the IO for the master script.
	 * @return {@link BaseInteractiveObject}
	 */
	public FFInteractiveObject getMasterScript() {
		return master;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public FFScriptTimer getScriptTimer(final int id) {
		return scriptTimers[id];
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public FFScriptTimer[] getScriptTimers() {
		return scriptTimers;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected FFStackedEvent getStackedEvent(final int index) {
		return eventstack[index];
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initScriptTimers() {
		scriptTimers = new FFScriptTimer[getMaxTimerScript()];
		for (int i = 0; i < scriptTimers.length; i++) {
			scriptTimers[i] = new FFScriptTimer();
		}
	}
	private void postScriptStackClear() {
		for (int i = 0; i < ScriptConsts.MAX_EVENT_STACK; i++) {
			if (postScriptStack[i].exists()) {
				postScriptStack[i].setParams(null);
				postScriptStack[i].setEventname(null);
				postScriptStack[i].setSender(null);
				postScriptStack[i].setExist(false);
				postScriptStack[i].setIo(null);
				postScriptStack[i].setMsg(0);
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setScriptTimer(final int index, final FFScriptTimer timer) {
		scriptTimers[index] = timer;
	}
	public void stackSendPostScriptEvent(final FFInteractiveObject io,
			final int msg, final Object[] params, final String eventname) {
		for (int i = 0; i < ScriptConsts.MAX_EVENT_STACK; i++) {
			if (!postScriptStack[i].exists()) {
				if (params != null
						&& params.length > 0) {
					postScriptStack[i].setParams(params);
				} else {
					postScriptStack[i].setParams(null);
				}
				if (eventname != null
						&& eventname.length() > 0) {
					postScriptStack[i].setEventname(eventname);
				} else {
					postScriptStack[i].setEventname(null);
				}

				postScriptStack[i].setSender(super.getEventSender());
				postScriptStack[i].setIo(io);
				postScriptStack[i].setMsg(msg);
				postScriptStack[i].setExist(true);
				break;
			}
		}
	}
	private void transferPostScriptStack() {
		for (int i = 0; i < ScriptConsts.MAX_EVENT_STACK; i++) {
			if (postScriptStack[i].exists()) {
				stackSendIOScriptEvent(
						postScriptStack[i].getIo(),
						postScriptStack[i].getMsg(),
						postScriptStack[i].getParams(),
						postScriptStack[i].getEventname());
			}
		}
	}
}
