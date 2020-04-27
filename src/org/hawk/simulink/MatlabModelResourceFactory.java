package org.hawk.simulink;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
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
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngineOptions;
import org.eclipse.viatra.query.runtime.localsearch.matcher.integration.LocalSearchEMFBackendFactory;
import org.eclipse.viatra.query.runtime.rete.matcher.ReteBackendFactory;
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

import br.com.embraer.massif.commandevaluation.client.MatlabClient;
import br.com.embraer.massif.commandevaluation.exception.MatlabRMIException;
import hu.bme.mit.massif.communication.ICommandEvaluator;
import hu.bme.mit.massif.communication.command.MatlabCommand;
import hu.bme.mit.massif.communication.command.MatlabCommandFactory;
import hu.bme.mit.massif.communication.commandevaluation.CommandEvaluatorImpl;
import hu.bme.mit.massif.simulink.api.Importer;
import hu.bme.mit.massif.simulink.api.ModelObject;
import hu.bme.mit.massif.simulink.api.util.ImportMode;
import hu.bme.mit.massif.simulink.cli.util.CLIInitializationUtil;
import hu.bme.mit.massif.simulink.cli.util.CLISimulinkAPILogger;
import org.eclipse.viatra.query.runtime.extensibility.IQuerySpecificationProvider;
//import uk.ac.aston.log2repo.LocalFolderLogConverter;
//import uk.ac.aston.log2repo.LogConverter;
//import uk.ac.aston.log2repo.TimeSliceConverter;
//import uk.ac.aston.stormlog.Log;

/**
 * Parses Simulink files into EMF resources. 
 * Requires the Viatra Massif Simulink to EMF converter.
 * 
 */
public class MatlabModelResourceFactory implements IModelResourceFactory {

	//private static int number=0;
	ICommandEvaluator commandEvaluator=null;
	MatlabCommandFactory factory = null;
		
	@Override
	public String getHumanReadableName() {
		
		setupHeadlessEnvironment();
		try {
			commandEvaluator = new CommandEvaluatorImpl(new MatlabClient("127.0.0.1", 1098, "MatlabModelProviderr2018b12624"));
			//commandEvaluator.
			factory = new MatlabCommandFactory(commandEvaluator);
			//factory.
			
		} catch (MatlabRMIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return "Simulink Parser";
	}

	@Override
	public IHawkModelResource parse(IFileImporter importer, File labviewFile) throws Exception {
		File file= localParse(labviewFile);
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		Resource r = rs.createResource(URI.createFileURI(file.getAbsolutePath()));
		//System.out.println("contents");
		r.load(null);
		
		//System.out.println(r.getContents());
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
		return (f.getName().endsWith(".slx") || f.getName().endsWith(".mdl")) ; 
	}

	@Override
	public Collection<String> getModelExtensions() {
		//return Collections.singletonList(".gvi");
		return Arrays.asList(".mdl",".slx");
	}
	public File localParse(File file) throws Exception {
		File t = new File("");
		File f=null;
		String fname=file.getName();
		String name = t.getAbsoluteFile()+"/simulink/"+getFileName(fname);
		System.out.println("try 2 try");
		Importer importer =null;
		try {
			 //CLIInitializationUtil.setupEnvironment();
			System.out.println("try  try");
			//SimulinkMassifHandler simulinkMassifHandler = new SimulinkMassifHandler(matlabPath);
			if(commandEvaluator ==null)
				commandEvaluator = new CommandEvaluatorImpl(new MatlabClient("127.0.0.1", 1098, "MatlabModelProviderr2018b12624"));
			//commandEvaluator= new MatlabControlEvaluator("C:/Program File/MATLAB/R2017b/bin/matlab", true);
			if(factory ==null)
				factory = new MatlabCommandFactory(commandEvaluator);
			
			String modelPath= file.getParent();
			MatlabCommand addModelPath = factory.addPath();
			//System.out.println("model path"+ modelPath);
            addModelPath.addParam(modelPath);
            addModelPath.execute();
            String mname=fname.substring(0, fname.lastIndexOf("."));
            
			
            System.out.println("model name is  "+ mname);
            System.out.println("final name is  "+ name);
            System.out.println("model path is  "+ modelPath);
			
			//f.
			//System.out.println("test  "+ getFileName(file.getName()));
			//writer.println("The second line");
			
            
            ModelObject model = new ModelObject(mname, commandEvaluator);
			//model.setLoadPath("C:/Users/student/Desktop/matlab/test.slx");
			model.setLoadPath(modelPath);
			model.registerApplicableFilters("famfilter");
			System.out.println(model.getLoadPathAsURI());
			importer = new Importer(model, new CLISimulinkAPILogger());
			//Importer importer = new Importer(model);
			importer.traverseAndCreateEMFModel(ImportMode.SHALLOW);
			synchronized(this) {
				importer.saveEMFModel(name);
			}
			
			//commandEvaluator.
			f= new File(name+".simulink");
			closeFiles(factory);
			closeModels(factory);
			clear(fname);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//System.out.println(fname+"  before   "+e.getMessage());
			synchronized(this) {
				if(importer != null)
					importer.saveEMFModel(name);
			}
			
			//writ.println("inner exception occured");
			//if(factory != null)
			//	closeModels(factory);
			f= new File(name+".simulink");
			if(factory != null) {
				closeFiles(factory);
				closeModels(factory);
				clear();
				//closeFiles(factory);
			}
			 
			System.out.println(fname+"   "+e.getMessage());
			
			e.printStackTrace();
			
			return f;
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
	
	private void setupHeadlessEnvironment() {
        // Set up exporter for headless use
			CLIInitializationUtil.setupEnvironment();
		
        ViatraQueryEngineOptions.setSystemDefaultBackends(ReteBackendFactory.INSTANCE, ReteBackendFactory.INSTANCE, LocalSearchEMFBackendFactory.INSTANCE);    
    } 
	private void closeModels(MatlabCommandFactory commandFactory) {
        // Close already opened models in simulink:
        MatlabCommand closeAllCommand = commandFactory.customCommand("bdclose", 0);
        closeAllCommand.addParam("all");
        closeAllCommand.execute();
    }
	private void closeFiles(MatlabCommandFactory commandFactory) {
        // Close already opened models in simulink:
        MatlabCommand closeAllCommand = commandFactory.customCommand("fclose", 0);
        closeAllCommand.addParam("all");
        closeAllCommand.execute();
    }
	private static String getFileName(String name) {
		//String[] nameList= name.split("\\.");
		String result= name.substring(0, name.lastIndexOf("."));
		//number++;
		return result;
		
	}
	public void closeFile(String id) {
		if(factory!= null) {
			System.out.println("files "+id+" are closed");
			MatlabCommand closeAllCommand = factory.customCommand("fclose", 0);
			closeAllCommand.addParam(id);
			closeAllCommand.execute();
		}
	}
	public void closeFiles() {
		if(factory!= null) {
			System.out.println("files are closed");
			MatlabCommand closeAllCommand = factory.customCommand("fclose", 0);
			closeAllCommand.addParam("all");
			closeAllCommand.execute();
		}
	}
	public void closeModel(String id) {
		if(factory!= null) {
			MatlabCommand closeAllCommand = factory.customCommand("bdclose", 0);
			closeAllCommand.addParam(id);
			closeAllCommand.execute();
		}
	}
	public void clear() {
		if(factory!= null) {
			MatlabCommand closeAllCommand = factory.customCommand("clear", 0);
			closeAllCommand.addParam("all");
			closeAllCommand.execute();
		}
	}
	public void clear(String id) {
		if(factory!= null) {
			MatlabCommand closeAllCommand = factory.customCommand("delete", 0);
			closeAllCommand.addParam(id.substring(0, id.lastIndexOf(".")));
			closeAllCommand.execute();
			closeAllCommand = factory.customCommand("clear", 0);
			closeAllCommand.addParam(id.substring(0, id.lastIndexOf(".")));
			closeAllCommand.execute();
		}
	}
}
