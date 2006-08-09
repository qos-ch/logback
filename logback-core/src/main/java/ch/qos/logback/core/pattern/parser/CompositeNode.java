package ch.qos.logback.core.pattern.parser;

public class CompositeNode extends FormattingNode {
	Node childNode;

	CompositeNode() {
		super(Node.COMPOSITE);
	}

	public Node getChildNode() {
		return childNode;
	}

	public void setChildNode(Node childNode) {
		this.childNode = childNode;
	}

	public boolean equals(Object o) {
		//System.out.println("CompositeNode.equals()");
    if(!super.equals(o)) {
      return false;
    }
    if (!(o instanceof CompositeNode)) {
			return false;
		}
		CompositeNode r = (CompositeNode) o;

		return (childNode != null) ? childNode.equals(r.childNode)
						: (r.childNode == null);
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if(childNode != null) {
		 buf.append("CompositeNode("+childNode+")");
		} else {
			buf.append("CompositeNode(no child)");
		}
		buf.append(printNext());
		return buf.toString();
	}
}