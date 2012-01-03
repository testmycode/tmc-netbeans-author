package fi.helsinki.cs.tmc.author.highlight;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;

public class ModelAnswerHighlightsContainer extends AbstractHighlightsContainer {

    private Document doc;
    private OffsetsBag highlights;
    private AttributeSet attrs;

    ModelAnswerHighlightsContainer(Document doc) {
        this.doc = doc;
        this.highlights = new OffsetsBag(doc);
        
        this.attrs = makeNormalTextAttrs();
        remakeHighlights();
        
        doc.addDocumentListener(docListener);
    }

    private static AttributeSet makeNormalTextAttrs() {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setBackground(attrs, Color.ORANGE);
        return attrs;
    }

    private DocumentListener docListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            remakeHighlights();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            remakeHighlights();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
    };

    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return highlights.getHighlights(startOffset, endOffset);
    }

    private static final Pattern beginEndPattern = Pattern.compile("(^.*//\\s*BEGIN\\s+MODEL.*$)|(^.*//\\s*END\\s+MODEL.*\n)", Pattern.MULTILINE);
    private static final Pattern wholeFilePattern = Pattern.compile("//\\s*MODEL FILE");

    private void remakeHighlights() {
        removeAllHighlights();
        
        int depth = 0;
        int start = -1;
        
        String text;
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
            Logger.getLogger(ModelAnswerHighlightsContainer.class.getName()).log(Level.WARNING, null, ex);
            return;
        }
        
        if (wholeFilePattern.matcher(text).find()) {
            highlights.addHighlight(0, doc.getLength(), attrs);
            fireHighlightsChange(0, doc.getLength());
            return;
        }
        
        Matcher matcher = beginEndPattern.matcher(text);
        while (matcher.find()) {
            if (matcher.group(1) != null) { // "BEGIN MODEL"
                depth += 1;
                if (depth == 1) {
                    start = matcher.start();
                }
            } else { // "END MODEL"
                depth = Math.max(0, depth - 1);
                if (depth == 0 && start > -1) {
                    int end = matcher.end();
                    
                    highlights.addHighlight(start, end, attrs);
                    
                    fireHighlightsChange(start, end);
                    start = -1;
                }
            }
        }
    }

    private void removeAllHighlights() {
        HighlightsContainer oldHighlights = highlights;
        highlights = new OffsetsBag(doc);
        HighlightsSequence seq = oldHighlights.getHighlights(0, doc.getLength());
        while (seq.moveNext()) {
            fireHighlightsChange(seq.getStartOffset(), seq.getEndOffset());
        }
    }
}
