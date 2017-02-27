package com.dalonedrow.module.ff.ui;

import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.rpg.base.consoleui.Panel;
import com.dalonedrow.rpg.base.flyweights.RPGException;

/**
 * @author 588648
 */
public class FFPanel extends Panel {
    /**
     * @param w
     * @param b
     * @param h
     */
    public FFPanel(int w, boolean b, int h) {
        super(w, b, h);
    }
    /**
     * Creates a new instance of {@link FFPanel}.
     * @param width the number of characters in the panel, including borders and
     *            padding
     * @param bordered flag indicating whether the panel is bordered
     * @param height the panel's height in lines, including borders
     * @param text the text displayed in the panel
     * @param title if bordered, the panel's title
     */
    public FFPanel(final int width, final boolean bordered, final int height,
            final String text, final String title) {
        super(width, bordered, height, text, title);
    }
    /**
     * {@inheritDoc}
     * @throws RPGException
     */
    @Override
    protected String getTitledTableMarkup() throws RPGException {
        return FFWebServiceClient.getInstance().loadText("table_titled");
    }
    /**
     * {@inheritDoc}
     * @throws RPGException
     */
    @Override
    protected String getUnTitledTableMarkup() throws RPGException {
        return FFWebServiceClient.getInstance().loadText("table_untitled");
    }
}
