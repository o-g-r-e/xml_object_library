package wiley.xmlobjectlibrary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringEscapeUtils;

public class XmlObjectManager
{	
	private String encodingScheme;
	private String xmlVersion;
	protected XmlNode rootNode;
	private String piTarget;
	private String piData;
	
	public XmlObjectManager(String version, String encoding, XmlNode rootNode) {
		this.xmlVersion = version;
		this.encodingScheme = encoding;
		this.rootNode = rootNode;
	}
	
	public  XmlObjectManager(File xmlFilePath) throws XMLStreamException, FileNotFoundException {
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new FileReader(xmlFilePath));
		parseXml(xmlStreamReader);
	}
	
	public  XmlObjectManager(String xmlContent) throws XMLStreamException, FileNotFoundException, UnsupportedEncodingException {
		byte[] byteArray = xmlContent.getBytes("UTF-8");
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
	    XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);
		parseXml(xmlStreamReader);
	}
	
	private void parseXml(XMLStreamReader xmlStreamReader) throws XMLStreamException {
		encodingScheme = xmlStreamReader.getCharacterEncodingScheme();
		xmlVersion = xmlStreamReader.getVersion();
		
		XmlNode currentNode = rootNode;
		Stack<XmlNode> parentsOfCurrent = new Stack<XmlNode>();
		
		while(xmlStreamReader.hasNext())
		{
			int xmlEvent = xmlStreamReader.next();
			if(xmlEvent == XMLStreamConstants.PROCESSING_INSTRUCTION) {
				piTarget = xmlStreamReader.getPITarget();
				piData = xmlStreamReader.getPIData();
			} else if (xmlEvent == XMLStreamConstants.START_ELEMENT) {
				String tagName = XmlObjectManager.computeXmlName(xmlStreamReader.getName());
				
				XmlNode newNode = new XmlNode(tagName);
				
				for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
				{
					String attrName = xmlStreamReader.getAttributeLocalName(i);
					
					if(xmlStreamReader.getAttributePrefix(i) != null && !xmlStreamReader.getAttributePrefix(i).equals("")) {
						attrName = xmlStreamReader.getAttributePrefix(i)+":"+attrName;
					}
					String attrValue = xmlStreamReader.getAttributeValue(i);
					newNode.addAttribute(attrName, attrValue);
				}
				
				for (int i = 0; i < xmlStreamReader.getNamespaceCount(); i++)
				{
					String attrName = "xmlns"+(xmlStreamReader.getNamespacePrefix(i)==null?"":":"+xmlStreamReader.getNamespacePrefix(i));
					String attrValue = xmlStreamReader.getNamespaceURI(i);
					newNode.addNamespace(attrName, attrValue);
				}
				
				if(rootNode == null) {
					rootNode = newNode;
					currentNode = rootNode;
					parentsOfCurrent.push(currentNode);
				}
				else 
				{
					currentNode.addNode(newNode);
					if(parentsOfCurrent.size() > 0 && !parentsOfCurrent.peek().equals(currentNode)) {
						parentsOfCurrent.push(currentNode);
					}
					currentNode = newNode;
				}
			} 
			else if (xmlEvent == XMLStreamConstants.END_ELEMENT)
			{
				currentNode.trimTextContent();
				currentNode = parentsOfCurrent.peek();
				
				if(parentsOfCurrent.size() > 1) {
					parentsOfCurrent.pop();
				}
			}
			else if(xmlEvent == XMLStreamConstants.COMMENT) {
				
				XmlNode commentNode = new XmlNode("");
				
				commentNode.setTextContent(xmlStreamReader.getText());
				commentNode.setCommentType(true);
				
				currentNode.addNode(commentNode);
				
			}
			else if(xmlEvent == XMLStreamConstants.CHARACTERS) {
				
				//currentNode.addTextContent(xmlStreamReader.getText().replace("\t", "").replaceAll("\n", ""));
				currentNode.addTextContent(xmlStreamReader.getText());
			}
		}
	}
	
	public void init() {
		rootNode = null;
		encodingScheme = null;
		xmlVersion = null;
		piTarget = null;
		piData = null;
	}
	
	public static String computeXmlName(QName name) {
		if(name.getPrefix() != null && !"".equals(name.getPrefix())) {
			return name.getPrefix()+":"+name.getLocalPart();
		}
		return name.getLocalPart();
	}
	
	public static Map<String, String>findAttributesInStream(XMLStreamReader xmlStreamReader, String[] attrNames) {
		Map<String, String> foundAttributes = new HashMap<String, String>();
		
		for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
		{
			String attrName = XmlObjectManager.computeXmlName(xmlStreamReader.getAttributeName(i));
			boolean attrFound = false;
			for(String inputName : attrNames) {
				if(inputName.equals(attrName)) {
					attrFound = true;
					break;
				}
			}
			
			if(attrFound) {
				String attrValue = xmlStreamReader.getAttributeValue(i);
				foundAttributes.put(attrName, attrValue);
			}
		}
		return foundAttributes;
	}
	
	public static String findAttributeValueByNameInStream(XMLStreamReader xmlStreamReader, String name) {
		String foundAttributeValue = null;
		
		for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
		{
			String attrName = XmlObjectManager.computeXmlName(xmlStreamReader.getAttributeName(i));
			
			if(name.equals(attrName)) {
				foundAttributeValue = xmlStreamReader.getAttributeValue(i);
				break;
			}
		}
		return foundAttributeValue;
	}
	
	private String xmlHeaderToString(String version, String encoding) {
		StringBuilder result = new StringBuilder();
		
		result.append("<?xml");
		
		if(version != null) {
			result.append(" version=\"").append(version).append("\"");
		}
		
		if(encoding != null) {
			result.append(" encoding=\"").append(encoding).append("\"");
		}
		
		result.append("?>");
		
		return result.toString();
	}
	
	private String nodesToXml(List<String> oneLineNodes) {
		StringBuilder result = new StringBuilder();
		
		result.append(xmlHeaderToString(getXmlVersion(), getEncodingScheme())).append("\n");
		
		if(getPiTarget() != null) {
			result.append("<?").append(getPiTarget());
			if(getPiData() != null) {
				result.append(" ").append(getPiData());
			}
			result.append("?>\n");
		}
		
		return result.append(nodeToString(getRootNode(), 0, oneLineNodes, (char)0)).toString();
	}
	
	private String nodeToString(XmlNode node, int indent, List<String> oneLineNodes, char terminate) {
		StringBuilder result = new StringBuilder();
		
		StringBuilder tab = new StringBuilder();
		
		for (int i = 0; i < indent; i++) {
			tab.append("\t");
		}
		
		result.append(tab);
		
		if(node.isComment()) {
			result.append("<!--"+node.getTextContent()+"-->\n");
		} else {
			result.append("<"+node.getNodeName());
			
			for(XmlNode.Attribute namespace : node.getNamespaces()) {
				result.append(" "+namespace.toString());
			}
			
			for(XmlNode.Attribute attribute : node.getAttributes()) {
				result.append(" "+attribute.toString());
			}
			
			//boolean innerContent = (node.getTextContent() != null && !node.getTextContent().equals("")) || (node.getNodes().size() > 0);
			boolean isSelfClosing = node.getNodes().size() <= 0 && node.textContent == null;
			if(isSelfClosing) {
				result.append("/>");
			} else {
				result.append(">");
				
				boolean oneLine = (oneLineNodes != null && oneLineNodes.contains(node.nodeName)) || node.getNodes().size() <= 0;
				
				if(!oneLine) {
					result.append("\n");
				}
				
				StringBuilder childNodes = new StringBuilder();
				
				for(XmlNode nextNode : node.getNodes()) {
					childNodes.append(nodeToString(nextNode, oneLine?0:indent+1, oneLineNodes, oneLine?(char)0:'\n'));
				}
				
				result.append(StringEscapeUtils.escapeXml(node.getTextContent())+childNodes.toString());
				
				if(!oneLine) {
					result.append(tab);
				}
				
				result.append("</"+node.getNodeName()+">");
				
				if(terminate != 0) {
					result.append(terminate);
				}
			}
		}
		
		return result.toString();
	}
	
	public void writeToFile(String filePath, String[] oneLineNodes) throws IOException {
		writeXml(filePath, oneLineNodes);
	}
	
	public void writeToFile(String filePath) throws IOException {
		writeXml(filePath, null);
	}
	
	private void writeXml(String filePath, String[] oneLineNodes) throws IOException {
		File xmlFile = new File(filePath);
		if(!xmlFile.exists()) {
			xmlFile.getParentFile().mkdirs(); 
			xmlFile.createNewFile();
		}
		
		try {
			PrintWriter writer = new PrintWriter(xmlFile, "UTF-8");
			String xmlString = oneLineNodes==null?toString():toString(oneLineNodes);
			writer.print(xmlString);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public String toString(String[] oneLineNodes) {
		return nodesToXml(new ArrayList<String>(Arrays.asList(oneLineNodes)));
	}
	
	public String toString() {
		return nodesToXml(null);
	}
	
	public String getEncodingScheme() {
		return encodingScheme;
	}
	
	public XmlNode getRootNode() {
		return rootNode;
	}

	public String getXmlVersion() {
		return xmlVersion;
	}

	public String getPiTarget() {
		return piTarget;
	}

	public String getPiData() {
		return piData;
	}
}