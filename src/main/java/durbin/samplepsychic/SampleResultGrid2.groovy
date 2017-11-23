package durbin.samplepsychic

import com.vaadin.ui.Grid
import com.vaadin.ui.TextField
import com.vaadin.shared.ui.grid.HeightMode
import com.vaadin.ui.Notification
import com.vaadin.ui.Grid.HeaderRow
import com.vaadin.ui.Grid.HeaderCell
import com.vaadin.data.Container.Filterable
import com.vaadin.data.util.IndexedContainer
import com.vaadin.data.util.filter.SimpleStringFilter
import com.vaadin.data.Item
import com.vaadin.event.FieldEvents.TextChangeEvent
import com.vaadin.event.FieldEvents.TextChangeListener
import com.vaadin.event.SelectionEvent.SelectionListener
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.themes.ValoTheme.*;
import com.vaadin.server.Sizeable.Unit.*
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.data.util.filter.Compare
import com.vaadin.data.util.filter.Compare.*;
import com.vaadin.data.Container.*
import com.vaadin.shared.ui.label.ContentMode


import grapnel.util.*

class SampleResultGrid2 extends Grid{
	def cc 
	
	def SampleResultGrid2(results,compendium){
		cc = compendium;
		init(results)				
		this.setCaption("Classification Results")
		this.setWidth("425px");
		double numRows = 10;
		this.setHeightMode(HeightMode.ROW);
		this.setHeightByRows(numRows)				
		//setColumnFiltering(true);				
		this.setVisible(true);																		
	}
	
	def init(results){
		IndexedContainer indexedContainer = buildContainerFromResults(results);
		setContainerDataSource(indexedContainer)
		setVisible(true);
	}		
	
	def update(results){
		removeAllColumns()
		init(results)
	}				
	
	def getFieldFromSelection(field,selectionID){
		def item = getContainerDataSource().getItem(selectionID)
		def fieldValue = item.getItemProperty(field).getValue()
		return(fieldValue)
	}
	
	def getSampleIDFromSelection(selectionID){
		def item = getContainerDataSource().getItem(selectionID)
		def sampleID = item.getItemProperty("Sample ID").getValue()
		return(sampleID)
	}	
	
	def buildContainerFromResults(results){
		def container = new IndexedContainer()		
		container.addContainerProperty("Sample ID",String.class,null);
		container.addContainerProperty("Model Name",String.class,null);
		container.addContainerProperty("Confidence",Double.class,null);
		
		results.each{r->									
			def (callValue,idx) = r.callAndIdx()									
			// Only interested in results where best result is 0
			if (idx != 0) return;
			//def idx = 0; // Always take the perspective of the 0 value. 
			//def callValue = r.classValues[idx] 
			
			def itemID = container.addItem();
			Item item = container.getItem(itemID);
			item.getItemProperty("Sample ID").setValue(r.sampleID as String);			
			item.getItemProperty("Model Name").setValue(r.modelName as String);
							
			def model = cc.modelName2Model[r.modelName]																				
			def bnm = model.bnm
			def dynamicbin = bnm.nullDistribution[idx]
			def values = dynamicbin.elements()
			def elements = values.elements() as ArrayList
			def pr = r.prForValues[idx] // probability.
			pr = pr.round(3)
			def nullConf = model.bnm.getSignificance(pr,idx)
			nullConf = nullConf.round(3)
			def pr0 = (float) r.prForValues[0]																								
			item.getItemProperty("Confidence").setValue(nullConf)
			System.err.println "buildContainer ${r.sampleID}"
		}
		return(container)
	}	

}

/*

def results = theApp.mam.results;		
// Get the best results and build a result by sample list.
def bestResults = cc.getBestResults(results,cutoff)
System.err.println("Total Results: ${results.size()} Best results: "+bestResults.size())
def sample2ResultList = cc.getResultsBySample(bestResults)
*/