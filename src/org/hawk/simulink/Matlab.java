package org.hawk.simulink;
import java.io.File;


public class Matlab {

	public static void main(String[] args) {
		MatlabModelResourceFactory ma= new MatlabModelResourceFactory();
		System.out.println(ma.getModelExtensions());
		try {
			File file = ma.localParse(new File("C:/Users/student/git/hawk/simple_model2.slx"));
			System.out.println(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		// TODO Auto-generated method stub
		ICommandEvaluator commandEvaluator;
		MatlabCommandFactory factory = null;
		try {
			 //CLIInitializationUtil.setupEnvironment();
			setupHeadlessEnvironment();
			//SimulinkMassifHandler simulinkMassifHandler = new SimulinkMassifHandler(matlabPath);
			
			commandEvaluator = new CommandEvaluatorImpl(new MatlabClient("127.0.0.1", 1098, "MatlabModelProviderr2017b1936"));
			//commandEvaluator= new MatlabControlEvaluator("C:/Program File/MATLAB/R2017b/bin/matlab", true);
			factory = new MatlabCommandFactory(commandEvaluator);
			
			String modelPath= "C:/Users/student/git/hawk/";
			MatlabCommand addModelPath = factory.addPath();
            addModelPath.addParam(modelPath);
            addModelPath.execute();
            ModelObject model = new ModelObject("sldemo_clutch_import", commandEvaluator);
			//model.setLoadPath("C:/Users/student/Desktop/matlab/test.slx");
			model.setLoadPath(modelPath);
			//model.registerApplicableFilters("famfilter");
			System.out.println(model.getLoadPathAsURI());
			Importer importer = new Importer(model, new CLISimulinkAPILogger());
			//Importer importer = new Importer(model);
			importer.traverseAndCreateEMFModel(ImportMode.FLATTENING);
			importer.saveEMFModel("model/testme");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(factory != null)
			 closeModels(factory);
			e.printStackTrace();
		} 
		

		*/
	}
	

}
