package wiley.xmlobjectlibrary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class XmlNode {
	
	public static class Attribute {
		private String name;
		private String value;
		
		public Attribute(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
		public String toString() {
			return name+"=\""+value+"\"";
		}
	}
	
	protected String nodeName;
	protected String textContent;
	protected List<XmlNode> nodes;
	protected List<Attribute> attributes;
	protected List<Attribute> namespaces;
	protected boolean isComment;
	
	public XmlNode(String nodeName) {
		this.nodeName = nodeName;
		this.textContent = "";
		this.attributes = new ArrayList<Attribute>();
		this.namespaces = new ArrayList<Attribute>();
		this.nodes = new ArrayList<XmlNode>();
	}
	
	public XmlNode(String nodeName, String textContent) {
		this.nodeName = nodeName;
		this.textContent = textContent;
		this.attributes = new ArrayList<Attribute>();
		this.namespaces = new ArrayList<Attribute>();
		this.nodes = new ArrayList<XmlNode>();
	}
	
	public XmlNode(String nodeName, String textContent, List<Attribute> attributes) {
		this.nodeName = nodeName;
		this.textContent = textContent;
		this.attributes = attributes;
		this.namespaces = new ArrayList<Attribute>();
		this.nodes = new ArrayList<XmlNode>();
	}

	public XmlNode(String nodeName, String textContent, Attribute[] attributes) {
		this.nodeName = nodeName;
		this.textContent = textContent;
		this.attributes = new ArrayList<Attribute>(Arrays.asList(attributes));
		this.namespaces = new ArrayList<Attribute>();
		this.nodes = new ArrayList<XmlNode>();
	}

	public boolean isComment() {
		return isComment;
	}

	public void setCommentType(boolean isComment) {
		this.isComment = isComment;
	}

	public void addNamespace(String name, String value) {
		namespaces.add(new Attribute(name, value));
	}
	
	public void addAttribute(String name, String value) {
		attributes.add(new Attribute(name, value));
	}
	
	public void setAttribute(String name, String value) {
		Attribute p = getAttributeByName(name);
		if(p != null) {
			p.setValue(value);
		}
	}
	
	public void removeAttribute(String name) {
		Iterator<Attribute> iterator = attributes.iterator();
		while (iterator.hasNext()) {
			Attribute s = iterator.next();
		    if (s.getName().equals(name)) {
		    	iterator.remove();
		    	break;
		    }
		}
	}
	
	public Attribute getAttributeByName(String name) {
		for(Attribute p : attributes) {
			if(p.getName().equals(name)) {
				return  p;
			}
		}
		return null;
	}
	
	public XmlNode addNode(XmlNode node) {
		nodes.add(node);
		return node;
	}
	
	public XmlNode addNode(XmlNode node, int index) {
		nodes.add(index, node);
		return node;
	}

	public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}
	
	public void trimTextContent() {
		this.textContent = this.textContent.trim();
	}
	
	public void addTextContent(String textContent) {
		this.textContent += textContent;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public List<Attribute> getNamespaces() {
		return namespaces;
	}
	
	public XmlNode getFirstChild(String nodeName, Attribute[] attributes) {
		XmlNode result = null;
		boolean found = false;
		for(XmlNode childNode : nodes) {
			if(!childNode.isComment() && childNode.getNodeName().equals(nodeName) && childNode.attributesFound(attributes))
			{
				result = childNode;
				found = true;
				break;
			}
		}
		
		if(!found) {
			for(XmlNode p : nodes) {
				result = p.getFirstChild(nodeName, attributes);
			}
		}
		return result;
	}
	
	public XmlNode getFirstChildByName(String nodeName) {
		XmlNode result = null;
		for(XmlNode childNode : nodes) {
			if(!childNode.isComment() && childNode.getNodeName().equals(nodeName))
			{
				result = childNode;
				break;
			}
		}
		return result;
	}
	
	public void removeFirstChildByName(String nodeName) {
		for (int i = 0; i < nodes.size(); i++) {
			XmlNode childNode = nodes.get(i);
			if(!childNode.isComment() && childNode.getNodeName().equals(nodeName))
			{
				nodes.remove(i);
				break;
			}
		}
	}
	
	public XmlNode getFirstChild() {
		if(nodes == null || nodes.size() <= 0) {
			return null;
		}
		
		return nodes.get(0);
	}
	
	private boolean attributesFound(Attribute[] attributes) {
		boolean attributesFound = true;
		
		for(Attribute a : attributes) {
			if(!containsAttribute(a.name) || !getAttributeByName(a.name).value.equals(a.value)) {
				attributesFound = false;
				break;
			}
		}
		
		return attributesFound;
	}
	
	public XmlNode[] getAllChildsByName(String nodeName) {
		List<XmlNode> result = new ArrayList<XmlNode>();
		
		for(XmlNode node : nodes) {
			if(!node.isComment() && node.getNodeName().equals(nodeName)) {
				result.add(node);
			}
		}
		
		return result.toArray(new XmlNode[0]);
	}
	
	public XmlNode[] getAllChilds() {
		return nodes.toArray(new XmlNode[0]);
	}
	
	public XmlNode getChildByIndex(/*String nodeName, int nodeOrderIndex*/) {
		
		/*if(nodeOrderIndex > nodes.size()-1) {
			return null;
		}
		
		int i = 0;
		for(XmlNode node : nodes) {
			if(!node.isComment() && node.getNodeName().equals(nodeName)) {
				if(nodeOrderIndex == i) {
					return node;
				}
				i++;
			}
		}*/
		
		return null;
	}
	
	public XmlNode getNodeByTextContent(String text) {
		XmlNode result = null;
		if(text != null && textContent != null && textContent.contains(text))
			result = this;
		
		for(XmlNode p : nodes) {
			p.getNodeByTextContent(text);
		}
		
		return result;
	}
	
	public XmlNode getNodeByIndex(int index) {
		if(index > nodes.size()-1 || index < 0) {
			return null;
		}
		return nodes.get(index);
	}

	public List<XmlNode> getNodes() {
		return nodes;
	}
	
	public List<XmlNode> getNodesByName() {
		return nodes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}
	
	public boolean containsAttribute(String attrName) {
		for(Attribute a : attributes) {
			if(a.name.equals(attrName)) {
				return true;
			}
		}
		return false;
	}
}
