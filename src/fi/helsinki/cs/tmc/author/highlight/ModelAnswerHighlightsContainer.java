package fi.helsinki.cs.tmc.author.highlight;

import java.awt.Color;
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
    private AttributeSet modelCodeAttrs;
    private AttributeSet stubCommentAttrs;
    private AttributeSet stubCodeAttrs;

    ModelAnswerHighlightsContainer(Document doc) {
        this.doc = doc;
        this.highlights = new OffsetsBag(doc);
        
        this.modelCodeAttrs = makeModelCodeAttrs();
        this.stubCommentAttrs = makeStubCommentAttrs();
        this.stubCodeAttrs = makeStubCodeAttrs();
        remakeHighlights();
        
        doc.addDocumentListener(docListener);
    }

    private static AttributeSet makeModelCodeAttrs() {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setBackground(attrs, Color.ORANGE);
        return attrs;
    }
    
    private static AttributeSet makeStubCommentAttrs() {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setBackground(attrs, new Color(0x7D1DF1));
        return attrs;
    }
    
    private static AttributeSet makeStubCodeAttrs() {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setBackground(attrs, new Color(0xA65DFF));
        StyleConstants.setForeground(attrs, Color.BLACK);
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

    private void remakeHighlights() {
        removeAllHighlights();
        
        String text = documentText();
        makeStubHighlights(text);
        makeModelHighlights(text);
    }

    private void removeAllHighlights() {
        HighlightsContainer oldHighlights = highlights;
        highlights = new OffsetsBag(doc);
        HighlightsSequence seq = oldHighlights.getHighlights(0, doc.getLength());
        while (seq.moveNext()) {
            fireHighlightsChange(seq.getStartOffset(), seq.getEndOffset());
        }
    }
    
    private static final Pattern stubPattern = Pattern.compile("^.*//[ \t]*STUB:[ \t]*(.*)$", Pattern.MULTILINE);
    private static final Pattern beginEndModelPattern = Pattern.compile("(^.*//[ \t]*BEGIN[ \t]+MODEL.*$)|(^.*//[ \t]*END[ \t]+MODEL.*\n)", Pattern.MULTILINE);
    private static final Pattern wholeFilePattern = Pattern.compile("//[ \t]*MODEL FILE");
    
    private void makeStubHighlights(String text) {
        Matcher matcher = stubPattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int contentStart = matcher.start(1);
            int end = matcher.end();
            
            highlights.addHighlight(start, contentStart, stubCommentAttrs);
            highlights.addHighlight(contentStart, end, stubCodeAttrs);
        }
    }
    
    private void makeModelHighlights(String text) {
        if (wholeFilePattern.matcher(text).find()) {
            highlights.addHighlight(0, doc.getLength(), modelCodeAttrs);
            fireHighlightsChange(0, doc.getLength());
            return;
        }
        
        int depth = 0;
        int start = -1;
        
        Matcher matcher = beginEndModelPattern.matcher(text);
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
                    
                    highlights.addHighlight(start, end, modelCodeAttrs);
                    
                    fireHighlightsChange(start, end);
                    start = -1;
                }
            }
        }
    }
    
    private String documentText() throws RuntimeException {
        try {
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
