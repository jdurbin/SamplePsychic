package durbin.samplepsychic

import org.apache.commons.io.FilenameUtils

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.vaadin.data.Item
import com.vaadin.data.util.IndexedContainer
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.*
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.shared.Position;

import grapnel.util.DoubleTable;
import grapnel.weka.*;
import weka.core.*;
import weka.filters.*;
import swiftml.*;


/****
 * Borrowed extensively from:
 * 
 * https://gist.github.com/canthony/3655917
 */


class ExpressionUploadComponent extends CustomComponent{	
	//private static final long serialVersionUID = -4292553844521293140L;			

	def app
	def dataFileRoot;
	//def table; // The table of expression values. 
	//def dtg;
	
	def ExpressionUploadComponent(SamplePsychicUI vapp){
		app = vapp;
	}
				
	void init(){
		VerticalLayout layout = new VerticalLayout();
		basic(layout);
		setCompositionRoot(layout);
	}
		
	void basic(VerticalLayout layout) {
		def tempFileName;
			
		// KJD Currently saves it first to a temporary file.   Could read the stream 
		// directly into an object...
		Upload upload = new Upload("",new Upload.Receiver(){
			@Override
			public OutputStream receiveUpload(String filename,String mimeType){													
																												
				// Create upload stream
				FileOutputStream fos = null; // Stream to write to
				try {
					
					// Save the root name of the file for the future...
					String filenamecopy = filename
					dataFileRoot = FilenameUtils.removeExtension(filenamecopy)	
					
					// Open the file for writing.
					tempFileName = "/tmp/uploads/" + filename
					def tempFile = new File(tempFileName)
					fos = new FileOutputStream(tempFile);
				} catch (final java.io.FileNotFoundException e) {
					new Notification("Could not open file:",
									 e.getMessage(),
									 Notification.Type.ERROR_MESSAGE)
						.show(Page.getCurrent());
					return null;
				}
			}
		})
		
		upload.addListener(new Upload.StartedListener() {
			@Override
			public void uploadStarted(StartedEvent event) {
				System.err.println "UPLOAD started: "+event.getFilename()
			}			
		});
	
	
		upload.addListener(new Upload.ProgressListener() {
			public void updateProgress(long readBytes, long contentLength) {
				// This method gets called several times during the update
				//pi.setValue(new Float(readBytes / (float) contentLength));
				System.err.println "UPLOAD progress: "+readBytes+"\tof\t"+contentLength
			}
		});
	
		upload.addListener(new Upload.SucceededListener() {
			public void uploadSucceeded(SucceededEvent event) {
				System.err.println "UPLOAD succeded "
			}
		});
	
		upload.addListener(new Upload.FailedListener() {
			public void uploadFailed(FailedEvent event) {
				// This method gets called when the upload failed
				//status.setValue("Uploading interrupted");
				System.err.println "UPLOAD failed"
			}
		});
	
		upload.addListener(new Upload.FinishedListener() {			
			@Override
			public void uploadFinished(Upload.FinishedEvent finishedEvent) {
				System.err.println "UPLOAD Finished"
				try {						
					//table = new durbin.util.DoubleTable(tempFileName)
					app.expressionData = WekaMine.readNumericFromTable(tempFileName)
					app.fileNameRoot = dataFileRoot
										
					// Determines if genes are in HGNC namespace, and if not converts them. 					
					//if (GeneInfo.isENSEMBL(app.expressionData)){
					//	System.err.println "Converting genes from EMBL to HGNC gene names...";
						//app.expressionData = AttributeUtils.renameAttributes(app.expressionData,app.ensembl2hgnc)
					//	app.expressionData = AttributeUtils.renameAttributesAndRemoveDupsUnknowns(app.expressionData,app.ensembl2hgnc)
					//	System.err.println "done converting genes."
					//}
					
					// Normalize expression data using exponential normalization (quantiles fit to exponential) 
					// All SamplePsychic models are built with exponential normalized training data.
					app.expressionData = SignatureSet.normalize(app.expressionData);
					
					def uploadNotifyStr = "${app.expressionData.numAttributes()} genes x ${app.expressionData.numInstances()} samples"
					def uploadNote = new Notification("Expression data uploaded: ",uploadNotifyStr);
					uploadNote.setType(Type.TRAY_NOTIFICATION);
					uploadNote.setPosition(Position.BOTTOM_CENTER);
					uploadNote.show(Page.getCurrent());
											
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
			
		// Put the components in a panel
		//Panel panel = new Panel("Gene Expression Input");
		//Layout panelContent = new VerticalLayout();
		//panelContent.addComponents(upload, image);
		//panelContent.addComponent(upload);
		//panel.setContent(panelContent);
		// END-EXAMPLE: component.upload.basic
	
		layout.addComponent(upload);
	
		// Create uploads directory
		File uploads = new File("/tmp/uploads");
		if (!uploads.exists()) uploads.mkdir();
		
		 //   layout.addComponent(new Label("ERROR: Could not create upload dir"));
		//((VerticalLayout) panel.getContent()).setSpacing(true);
		//panel.setWidth("-1");
		//layout.addComponent(panel);
	}
}



/*
 new Notification("Expression data uploaded: ",
				  "${app.expressionData.numAttributes()} genes x ${app.expressionData.numInstances()} samples",
				  //Notification.Type.HUMANIZED_MESSAGE)
				  Notification.Type.TRAY_NOTIFICATION)
	 .show(Page.getCurrent());
 */
	 
	 /* Let's build a container from the CSV File */
	 //FileReader reader = new FileReader(tempFile);
	 //IndexedContainer indexedContainer = buildContainerFromTable(table);
	 //tempFile.delete();
		 
	 //dtg = new DoubleTableGrid(table)
	 
		 
	 /* Finally, let's update the table with the container */
	 //table.setCaption(finishedEvent.getFilename());
	 //dtg.setContainerDataSource(indexedContainer);
	 //layout.addComponent(dtg);
	 //dtg.setVisible(true);
	


/*
 def buildContainerFromTable(DoubleTable table){
	 IndexedContainer container = new IndexedContainer()
	 table.colNames.each{colName->
		 container.addContainerProperty(colName,Double.class,null);
	 }
	 
	 for (int r = 0;r < table.numRows;r++) {
		 def rowName = table.rowNames[r]
		 def itemID = container.addItem();
		 Item item = container.getItem(itemID);
		 table.colNames.each{colName->
			 def value = table[rowName][colName]
			 item.getItemProperty(colName).setValue(value)
		 }
	 }
	 return(container)
 }
 */