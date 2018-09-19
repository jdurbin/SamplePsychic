package durbin.samplepsychic

import com.vaadin.ui.Label
import com.vaadin.ui.TabSheet
import com.vaadin.ui.VerticalLayout
import com.vaadin.data.Item
import com.vaadin.data.util.IndexedContainer
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Notification;
import com.vaadin.shared.ui.label.ContentMode;

import grapnel.util.DoubleTable;
import grapnel.util.*

import java.util.logging.Logger

import com.vaadin.ui.Notification;

import weka.core.*



/*****
 * View to display upload button and, once upload is complete, some status information.
 *
 * @author james
 *
 */
class UploadView extends VerticalLayout implements View{
	
	String name;
	ExpressionUploadComponent uc;
	TableUploadComponent tc;
	//SamplePsychicUI app;
	
	static Logger logger = Logger.getLogger(UploadView.class.getName());
	
	def UploadView(String vname,SamplePsychicUI app){
							
		name = vname
		setMargin(true);
		
		Label l = new Label("""<small>
<h2>Welcome to Sample Psychic</h2><p>
Sample Psychic is compendium of classifiers that illuminate various aspects of cell state 
from transcriptome data (either RNA-seq or microarray).  Predictors are grouped
into sets including: 

<ul>
<li> TCGA PANCAN Mutations </li>
<li> TCGA PANCAN <a href="http://cbio.mskcc.org/tools/memo/">MEMO Events</a> </li>
<li> Chromatin State Models </li> 
<li> Gene Essentiality Models </li> 
<li> Cancer Cell Line drug sensitivity (<a href="http://www.broadinstitute.org/ccle/home">CCLE</a>)</li>
<li> Fetal Brain Clusters </li> 
<li> CIBERSORT LM22</li>
</ul>

Sample Psychic is a heterogeneous collection of classifiers  tuned to each specific prediction target. Individual 
predictors may be linear or non-linear SVMs, random forest predictors, or logistic regression predictors. 
<p>

Expression data should be a tab delimited file formatted in genes (rows) x samples (columns) format. 
Both row names and column names are required.  No normalization needs to be applied beyond standard sample processing (e.g. RPKM).  
SamplePsychic's built-in normalization should allow reasonable performance with both microarray and RNASeq data though 
models were all trained on RNASeq   
<p>
<h2>Click below to upload your data and get started:</h2> <p>  
</small>
"""
		, ContentMode.HTML);
		addComponent(l);
		//setComponentAlignment(l, Alignment.MIDDLE_CENTER);
		//setMargin(true);
		
		//addComponent(new Label("Upload some data here"));
		uc = new ExpressionUploadComponent(app)
		uc.init()
		
		//TestCustomComponent tc = new TestCustomComponent()
		//tc.init()
		addComponent(uc)
		
		//Label l2 = new Label("""<small>
//(Optional) Upload metadata file in samples (rows) x features (cols) tab delimeted format: 
//</small>
//"""
		//, ContentMode.HTML);		
	//	addComponent(l2);
			
		
	//	tc = new TableUploadComponent(app)
	//	tc.init()
	//	addComponent(tc)
				
	}
	

	@Override
	public void enter(ViewChangeEvent event) {
		//logger.entering(getClass().getSimpleName(), "enter");
		// TODO Auto-generated method stub
		//Notification.show("WELCOME to: $name");
		//if (uc.table == null) logger.info("*****   uc.t == NULL  *****")
		//else logger.info("***** uc.t.numRows = ${uc.table.rows()}")
		//System.err.println("HEY3!!!!!!!!!!!!")
		System.err.println "UploadView enter event.parameters= "+event.getParameters()				
	}

}