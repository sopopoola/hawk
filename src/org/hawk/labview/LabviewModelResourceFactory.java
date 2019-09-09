package org.hawk.labview;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.soap.Node;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.hawk.core.IFileImporter;
import org.hawk.core.IModelResourceFactory;
import org.hawk.core.model.IHawkModelResource;
import org.hawk.emf.EMFWrapperFactory;
import org.hawk.emf.model.EMFModelResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import uk.ac.aston.log2repo.LocalFolderLogConverter;
//import uk.ac.aston.log2repo.LogConverter;
//import uk.ac.aston.log2repo.TimeSliceConverter;
//import uk.ac.aston.stormlog.Log;

/**
 * Parses RDM log files into EMF resources, using the {@link LogConverter} and
 * {@link TimeSliceConverter} classes. Requires that the RDM configuration file
 * is present in the root folder of the monitored location, with
 * {@link #RDM_JSON} as its filename.
 * 
 * RDM log files should be named following the pattern `log*.json`: they should
 * start with `log` and end with `.json`.
 */
public class LabviewModelResourceFactory implements IModelResourceFactory {

	private static final String RDM_JSON = "rdm.json";
	private final String xml_opening_tag = "<?xml version=\"1.0\" encoding=\"ASCII\"?>\r\n" + 
			"<BlockDiagram xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:LabView=\"http://www.example.org/LabView\">";
	private final String xml_closing_tag = "</BlockDiagram>";

	@Override
	public String getHumanReadableName() {
		return "LabVIEW Parser";
	}

	@Override
	public IHawkModelResource parse(IFileImporter importer, File labviewFile) throws Exception {
		//File rdmJSON = importer.importFile(RDM_JSON);
		//Log log = new LocalFolderLogConverter(rdmJSON, logJSON).convert();
		File file= localParse(labviewFile);
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		Resource r = rs.createResource(URI.createFileURI(file.getAbsolutePath()));
		System.out.println("contents");
		r.load(null);
		
		System.out.println(r.getContents());
		//System.out.println(file);
		
		//r.getContents().add(log);

		return new EMFModelResource(r, new EMFWrapperFactory(), this);
	}

	@Override
	public void shutdown() {
		// nothing to do
	}

	@Override
	public boolean canParse(File f) {
		//return f.getName().endsWith(".giv") && f.getName().startsWith("log");
		return f.getName().endsWith(".gvi"); 
	}

	@Override
	public Collection<String> getModelExtensions() {
		return Collections.singletonList(".gvi");
	}
	public static File localParse(File file) throws ParserConfigurationException, IOException, SAXException {
		File f= new File(getFileName(file.getName()));
		System.out.println("file parser is called");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    //factory.setValidating(true);
	    factory.setIgnoringElementContentWhitespace(true);
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    //File file = new File("Main.gvi");
	    Document doc = builder.parse(file);
	    NodeList listA= doc.getElementsByTagName("BlockDiagram");
	    Node node = (Node) listA.item(0);
	   // ((Element)node).setAttribute("file", file.getAbsolutePath());
	    try {
			//System.out.println(nodeToString(node));
			PrintWriter writer = new PrintWriter(f.getName(), "UTF-8");
			System.out.println(f.getAbsolutePath());
			
			writer.println(nodeToString(node));
			
			//f.
			//System.out.println("test  "+ getFileName(file.getName()));
			//writer.println("The second line");
			writer.close();
		} catch (TransformerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	   // node.
	    //Element element= (Element)node;
	   // NodeList list =element.getChildNodes();
	    //System.out.println();
	   
	    //System.out.println(doc.getElementsByTagName("BlockDiagram"));
	   //// for (int i = 0; i < list.getLength(); i++) {
            //Node nNode = (Node) list.item(i);
	    //}
	    return f;
	    // Do something with the document here.
	}
	
	private static String getFileName(String name) {
		String[] nameList= name.split("\\.");
		String result= nameList[0] + ".customxml";
		return result;
		
	}
	
	private static String nodeToString(Node node)
			throws TransformerException
			{
			    StringWriter buf = new StringWriter();
			    Transformer xform = TransformerFactory.newInstance().newTransformer();
			    xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			    xform.transform(new DOMSource(node), new StreamResult(buf));
			    return(buf.toString());
			}

}