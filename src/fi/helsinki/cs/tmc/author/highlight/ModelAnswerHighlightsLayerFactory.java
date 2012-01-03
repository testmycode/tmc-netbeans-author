package fi.helsinki.cs.tmc.author.highlight;

import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

public class ModelAnswerHighlightsLayerFactory implements HighlightsLayerFactory {
    @Override
    public HighlightsLayer[] createLayers(Context ctx) {
        return new HighlightsLayer[] {
            HighlightsLayer.create("TmcModelAnswer", ZOrder.DEFAULT_RACK, true, new ModelAnswerHighlightsContainer(ctx.getDocument()))
        };
    }
}
