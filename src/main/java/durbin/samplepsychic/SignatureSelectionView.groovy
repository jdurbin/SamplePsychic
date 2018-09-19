package durbin.samplepsychic

import com.vaadin.ui.Button
import com.vaadin.ui.Button.*
import com.vaadin.ui.Label
import com.vaadin.ui.TabSheet
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.TwinColSelect
import com.vaadin.data.Item
import com.vaadin.data.Property.ValueChangeListener
import com.vaadin.data.util.IndexedContainer
import com.vaadin.event.ContextClickEvent.ContextClickListener
import com.vaadin.event.SelectionEvent.SelectionListener
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.*
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.shared.Position;
import com.vaadin.server.Page;

import grapnel.util.DoubleTable;
import grapnel.weka.Classification;
import grapnel.weka.ClassificationPlus;
import grapnel.swiftml.*;
import grapnel.weka.*;

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
	def listBuilder;
				
	static Logger logger = Logger.getLogger(UploadView.class.getName());
			
	def SignatureSelectionView(String vname,SamplePsychicUI vapp){
		
		app = vapp;
							
		name = vname
		setMargin(true);
		
		Label l = new Label("""

<h2>Select Model Sets</h2><p>
Select the model sets you wish to apply to your uploaded data:
""", ContentMode.HTML);
		addComponent(l);

		listBuilder = new TwinColSelect();
		
		// KJD Test
		listBuilder.addStyleName('mysmalltextstyle');								
		
		
		app.signatureSets.eachWithIndex{signatureSet,i->
			listBuilder.addItem(i)
			System.err.println "set $i setCaption: "+signatureSet.shortName
			listBuilder.setItemCaption(i,signatureSet.shortName)			
		} 
		
		def n = app.signatureSets.size()
		listBuilder.addItem(n)
		listBuilder.setItemCaption(n,"MEMo Events");
		listBuilder.addItem(n+1)
		listBuilder.setItemCaption(n+1,"TCGA Mutations");
		listBuilder.addItem(n+2)
		listBuilder.setItemCaption(n+2,"Epigenome States");		
		
					
		listBuilder.setRows(6);
		listBuilder.setNullSelectionAllowed(false);
		listBuilder.setMultiSelect(true);
		listBuilder.setImmediate(true);
		listBuilder.setLeftColumnCaption("Available Sets");
		listBuilder.setRightColumnCaption("Selected Sets");
		listBuilder.setWidth("80%")
		
		// KJD DEBUG Temporary placeholder until work out valueChangeListener
		//selectedSignatures = app.signatureSets[0]
		//app.selectedSignatureSets = new SignatureSets()
		// Note: this will merge modelName2Model maps. 		
		//app.selectedSignatureSets.add(app.signatureSets[0])  		
		
		
		listBuilder.addValueChangeListener({event->
			// Could have info about selected signatures in a list pane 
			// that is updated here.  But really want info about signatures 
			// before you add them to help you decide whether to add...
			// not in the TwinColSelect API, though...
			
			
			//String.valueOf(event.getProperty().getValue())
			// Think it returns the whole current list...
			System.err.println "Value Change Event: "+event
			System.err.println "Event Property: "+event.getProperty()
			System.err.println "Event value: "+event.getProperty().getValue()
			// ^^ So this returns the list of indices of selected items. 
		} as ValueChangeListener)									
						 
		addComponent(listBuilder)
		
		def runButton = new Button("Apply Models");		
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

			// Collect the selected signatures...
			System.err.println "BUTTON CLICK: "+listBuilder.getValue();
			def selectedItems = listBuilder.getValue();
			app.selectedSignatureSets = new SignatureSets()
			selectedItems.each{item->
				app.selectedSignatureSets.add(app.signatureSets[item])
			}
			
			// Apply models to these signatures. 
			//def minConfidence = 0.7 // Don't even return results that don't have either Pr > 0.7 or background > 0.7
			app.results = app.selectedSignatureSets.applyModels(app.expressionData)	
			// Clean them up a little. 
			app.results = filterResults(app.results);														


			// TODO:  Make this contingent on a check box or something so only save session if user asks us to. 
						
			// Generate a jobID and a fileName for the results to be saved to. 							
			String jobID,sessionFileName			
			(jobID,sessionFileName) = SamplePsychicSession.generateResultsTokenAndFileName(app.jobsDir)	
			System.err.println "DEBUG: jobID:$jobID\tfileName: $sessionFileName"
			
			app.currentRunID = jobID; // need to strip off everything but root of temp file name...				
			SignatureSet.saveResults(sessionFileName,app.results);						
					
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
	
	def filterResults(results){
		// Limit results somewhat to sane and useful results. 
		// 
		def newResults = []
	
		results.each{r->	
			// KJD Temp hack.
			// if no preferred Idx is given, then choose the one that doesn't
			// contain indications it's a negative class.  People tend not to
			// want to see that it's "not lung".
			// Need a way to specify this when model is being built. 
			if (r.preferredIdx == null) {
				if (r.classValues[0].contains("not_")) r.preferredIdx = 1;
				else if (r.classValues[1].contains("not_")) r.preferredIdx = 0;
				else r.preferredIdx = 0;
			}
			
			// No one wants to see classifiers with super low scores either.  70% is about the 
			// cutoff for a useful classifier in most applications, so just filter those out. 
			def pr = r.prForValues[r.preferredIdx]
			def br = r.nullForValues[r.preferredIdx]
			
			// Sometimes the null model and the classifier probability can diverge. 
			// Don't want to prejudice one measure over the other, so if either is good
			// the result stays. 
			//if ((pr >= 0.7) || (br >=0.7)){
			//	newResults.add(r)	
			//}
			
			// Changed my mind.  No one wants to see low scoring garbage that 
			// is low on either measure.  Usually means something has gone wrong. 
			if ((pr >= 0.7) && (br >=0.7)){
				newResults.add(r)
			}
						
		}
		// probably redundant, but make sure gc knows we are done with this.		
		results = null 
		return(newResults)	
	}
	
}
		