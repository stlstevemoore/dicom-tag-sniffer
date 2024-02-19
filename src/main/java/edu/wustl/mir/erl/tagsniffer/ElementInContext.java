package edu.wustl.mir.erl.tagsniffer;

// ElementInContext contains a list of element tags that you

public class ElementInContext {
    private long tagPosition = 0;
    private int  elementTag  = 0;
    private String elementName = null;

    public ElementInContext(long tagPosition, int elementTag, String elementName) {
        this.tagPosition = tagPosition;
        this.elementTag = elementTag;
        this.elementName = elementName;
    }

    public long getTagPosition() {
        return tagPosition;
    }

    public void setTagPosition(long tagPosition) {
        this.tagPosition = tagPosition;
    }

    public int getElementTag() {
        return elementTag;
    }

    public void setElementTag(int elementTag) {
        this.elementTag = elementTag;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }
}
