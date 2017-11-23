package durbin.samplepsychic

import com.vaadin.ui.Button
import com.vaadin.ui.Button.*
import com.vaadin.ui.Label
import com.vaadin.ui.TabSheet
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.TwinColSelect
import com.vaadin.data.Item
import com.vaadin.data.util.IndexedContainer
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.*
import com.vaadin.ui.Notification.Type;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.shared.Position;
import com.vaadin.server.Page;

import grapnel.util.DoubleTable;
import grapnel.weka.Classification;
import grapnel.swiftml.*;

import java.util.ArrayList;
import java.util.logging.Logger

import com.vaadin.ui.Notification;



/*****
 * View to display upload button and, once upload is complete, some status information.
 *
 * @author james
 *
 */
class SignatureSelectionView extends VerticalLayout implements View{
	
	String name;	
	SamplePsychicUI app;
	
			
	static Logger logger = Logger.getLogger(UploadView.class.getName());
	
	
	
	def SignatureSelectionView(String vname,SamplePsychicUI vapp){
		
		app = vapp;
							
		name = vname
		setMargin(true);
		
		Label l = new Label("""

<h2>Select Signature Sets</h2><p>
Select the signature sets you wish to apply to your uploaded data:
""", ContentMode.HTML);
		addComponent(l);
				
		def listBuilder = new TwinColSelect();
		for (int i = 0; i < 7; i++) {
			listBuilder.addItem(i);			
		}
		
		listBuilder.setItemCaption(0," TCGA PANCAN Mutations")
		listBuilder.setItemCaption(1," TCGA PANCAN MEMO Events")
		listBuilder.setItemCaption(2," TCGA PANCAN Clinical Observations")
		listBuilder.setItemCaption(3," TCGA PANCAN Clinical Outcomes")
		listBuilder.setItemCaption(4," Cancer Cell Line drug sensitivity (CCLE)")
		listBuilder.setItemCaption(5," Tree of Cells Cell Types")
		listBuilder.setItemCaption(6," Stem Cell Types")
		
		
		listBuilder.setRows(6);
		listBuilder.setNullSelectionAllowed(true);
		listBuilder.setMultiSelect(true);
		listBuilder.setImmediate(true);
		listBuilder.setLeftColumnCaption("Available Sets");
		listBuilder.setRightColumnCaption("Selected Sets");
		listBuilder.setWidth("80%")
 
		//listBuilder.addValueChangeListener(e -> Notification.show("Value changed:",
		//		String.valueOf(e.getProperty().getValue()),
		//		Type.TRAY_NOTIFICATION));
		
		addComponent(listBuilder)
		
		def runButton = new Button("Apply Signatures");
		
		runButton.addClickListener(new RunClassifiersClickListener())		
		addComponent(runButton)
		 
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		//logger.entering(getClass().getSimpleName(), "enter");
		// TODO Auto-generated method stub
		//Notification.show("WELCOME to: $name");
		System.err.println "SignatureSelection enter event.parameters= "+event.getParameters()		
	}
	
	class RunClassifiersClickListener extends ClickListener{
		
		def getIDAndModelNames(results){
			def idSet = [] as Set
			def modelSet = [] as Set
			results.each{r->
				idSet<<r.sampleID
				modelSet<<r.modelName
			}
			return([idSet,modelSet])
		}		
				
		public void buttonClick(final ClickEvent event){			
			// Clear the old results when you run a new analysis. 
			app.results = []		
			app.results = app.compendium.applyModels(app.expressionData)			
			//app.results.each{r->
				//System.err.println "RESULT: "+r.sampleID+"\t"+r.modelName+"\t"+r.nullConf0;
				//}
												
			// TODO:  Make this contingent on a check box or something so only save if user asks us to. 			
			// Generate a jobID and a fileName for the results to be saved to. 							
			String jobID,sessionFileName			
			(jobID,sessionFileName) = SessionUtils.generateResultsTokenAndFileName()	
			System.err.println "DEBUG: jobID:$jobID\tfileName: $sessionFileName"
			
			app.currentRunID = jobID; // need to strip off everything but root of temp file name...				
			app.compendium.saveResults(sessionFileName,app.results);						
					
			app.navigator.navigateTo("results/$jobID");
			
			def idnames, modelnames
			(idnames,modelnames) = getIDAndModelNames(app.results)		
			def finishedNotificationStr = "${modelnames.size()} models applied to ${idnames.size()} samples. ${app.results.size()} total results."
			def finNotification = new Notification("Models Applied:",finishedNotificationStr)
			finNotification.setType(Type.TRAY_NOTIFICATION);
			finNotification.setPosition(Position.BOTTOM_CENTER);			
			finNotification.show(Page.getCurrent());			
		}				
	}
}
		