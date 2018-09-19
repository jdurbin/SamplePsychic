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
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.themes.ValoTheme.*;
import com.vaadin.server.Sizeable.Unit.*
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.data.util.filter.Compare
import com.vaadin.data.util.filter.Compare.*;
import com.vaadin.data.Container.*

import grapnel.util.*
import grapnel.weka.*

class ClassificationResultsGrid extends Grid{
	SamplePsychicUI app;
	HeaderRow filterRow;
	def firstItemID;
	
	
	def ClassificationResultsGrid(SamplePsychicUI vapp){		
		app = vapp
		
		System.err.println "*** App..size = "+app.results.size()
		
		IndexedContainer indexedContainer = buildContainerFromResults(app.results);
		this.setCaption("Classification Results")
		this.setWidth("870px");
		double numRows = 8;
		this.setHeightMode(HeightMode.ROW);
		this.setHeightByRows(numRows)
		this.setContainerDataSource(indexedContainer)

		
		// Attempt to adjust column sizes
		// Remaining columns should expand to fill??
		// https://vaadin.com/docs/v7/framework/articles/ConfiguringGridColumnWidths.html
		this.getColumn("Score").setWidth(100)
		this.getColumn("Confidence").setWidth(100)
				
		// Try to select an initial row..
		System.err.println "firstItemID: $firstItemID"
		this.setSelectionMode(Grid.SelectionMode.SINGLE);
		if (firstItemID != null) this.select(firstItemID);				
				
		setColumnFiltering(true);		
		
		this.setVisible(true);
	}	
	
	
	def getSampleIDFromSelection(selectionID){
		def item = getContainerDataSource().getItem(selectionID)
		def sampleID = item.getItemProperty("Sample ID").getValue()
		return(sampleID)
	}
	
	def getFieldFromSelection(field,selectionID){
		def item = getContainerDataSource().getItem(selectionID)
		def fieldValue = item.getItemProperty(field).getValue()
		return(fieldValue)
	}	
	
	def setColumnFiltering(boolean filtered){
		if (filtered && filterRow == null){
			filterRow = this.appendHeaderRow();		 
			// Set up a filter for all columns
			for (Object pid: this.getContainerDataSource().getContainerPropertyIds()) {
				TextField filter = getColumnFilter(pid);
				
				// Tooltip for filter. 
				filter.setDescription("<b>Filter Rows</b>"
					+ "<small><ul>"
					+ "<li>In numeric column, type \">0.9\" to show only rows >0.9  </li>"
					+ "<li>In numeric column, type \"<0.5\" to show only rows <0.5  </li>"
					+ "<li>Type string to show only rows containing that string     </li>"
					+ "<li>Multiple columns can be filtered at once to narrow results.</li>"
					+ "<li>Click on column to sort by that column. </li>"
					+ "</ul></small>");
				
				filterRow.getCell(pid).setComponent(filter);
				filterRow.getCell(pid).setStyleName("filter-header"); 
			}
		}else if (!filtered && filteringHeader != null){
			this.removeHeaderRow(filterRow);
			filterRow = null;		
		}
	}
	
	TextField getColumnFilter(columnId){
		TextField filter = new TextField();
		filter.setWidth("100%");
		filter.setHeight("16px");
		filter.addStyleName(ValoTheme.TEXTFIELD_TINY);
		filter.setInputPrompt("Filter");
		filter.addTextChangeListener(new TextChangeListener() {
 
			//SimpleStringFilter ssf = null;
			Filter ssf = null;
 
			@Override
			public void textChange(TextChangeEvent event) {
				Filterable f = (Filterable) this.getContainerDataSource();
 
				// Remove old filter
				if (ssf != null) {
					f.removeContainerFilter(ssf);
				}
				
				// Sample ID, Model, Call, Bootstrap
				//System.err.println "CONTAINER PROPERTY IDS: "+f.getContainerPropertyIds();
				//f.getContainerProperty(columnId)
				
				def newFilterText = event.getText()
				
				if (newFilterText.contains(">") || newFilterText.contains("<")){
					System.err.println "POSSIBLE NUMERIC FILTER: "+newFilterText
					def test = newFilterText.replaceAll(">","")
					System.err.println "test string: "+test
					test = test.replaceAll("<","")
					System.err.println "test string: "+test
					test = test.replaceAll("=","")
					System.err.println "test string: "+test
					test = test.replaceAll("0","")
					System.err.println "test string: "+test
					test = test.replaceAll(/\./,"")
					System.err.println "test string: "+test					
					if (test == "") return ; // If all we have is > or < symbol, 0s, or .0s, punt and wait for more. 
					
					// Should check that column is numeric, but this is QND way to get started...
					ssf = getNumericFilterForString(columnId,newFilterText)
				}else{				
					ssf = new SimpleStringFilter(columnId,newFilterText,true,false);
				}
				f.addContainerFilter(ssf);
 
				this.cancelEditor();
			}
		});
		return filter;
	}			
		
	/**
	 * Parses a string like \> 0.5 or \< 3.6 and returns appropriate filter.
	 */
	def getNumericFilterForString(id,fstring){
		System.err.println("GET NUMERIC FILTER")
		def rval
		if (fstring.contains(">")){
			fstring = fstring.replaceAll(">","")
			fstring = fstring.replaceAll("=","") // in case they type >=, which we do not support
			rval = new Greater(id,fstring as Double);			
		}else{
			fstring = fstring.replaceAll("<","")
			fstring = fstring.replaceAll("=","") // in case they type >=, which we do not support
			rval = new Less(id,fstring as Double)		
		}
		return(rval)
	}
	
	
	def buildContainerFromResults(results){
		
		def bFirstTime = true;
		def container = new IndexedContainer()
		
		container.addContainerProperty("Sample ID",String.class,null);
		container.addContainerProperty("Model",String.class,null);
		container.addContainerProperty("Call",String.class,null);
		container.addContainerProperty("Score",Double.class,null);
		container.addContainerProperty("Confidence",Double.class,null);
		
		System.err.println "Grid about to add ${results.size()} results to grid."		
		
		results.eachWithIndex{r,i->			
			// Use signatureSets instead of selectedSignature sets because
			// might be accessing saved results. 			
			def model = app.signatureSets.modelName2Model[r.modelName]
			if (model == null){
				System.err.println "DEBUG: r.modelName: "+r.modelName
				System.err.println "DEBUG: modelName2Model: "+app.signatureSets.modelName2Model.keySet()
			}
					
			// KJD Temp hack.
			// Hack moved to SignatureSelectionView and applied to all results for app 
			// if no preferred Idx is given, then choose the one that doesn't 
			// contain indications it's a negative class.  People tend not to 
			// want to see that it's "not lung".  
			//if (r.preferredIdx == null) {
			//	if (r.classValues[0].contains("not_")) r.preferredIdx = 1;
			//	else if (r.classValues[1].contains("not_")) r.preferredIdx = 0;
			//	else r.preferredIdx = 0;
			//}
			
			def callValue = r.classValues[r.preferredIdx]
			if (model == null){
				System.err.println "NULL MODEL: ${r.modelName}"
				System.err.println app.signatureSets.modelName2Model.keySet()
			}
			
			def bnm = model.bnm
			if (bnm == null){
				System.err.println "NULL BACKGROUND: ${r.modelName}"
			}
			
			def dynamicbin = bnm.nullDistribution[r.preferredIdx]
			//def values = dynamicbin.elements()
			//def elements = values.elements() as ArrayList
			def pr = r.prForValues[r.preferredIdx] // probability from classifier
			pr = pr.round(3)			
			def nullConf = model.bnm.getSignificance(pr,r.preferredIdx) // null confidence from background
			nullConf = nullConf.round(3)
			//def pr0 = (float) r.prForValues[0]
			
			//System.err.println "buildContainer RESULT: "+r.sampleID+"\t"+r.modelName+"\t"+nullConf;						

			def itemID = container.addItem();						
			Item item = container.getItem(itemID);									
			item.getItemProperty("Sample ID").setValue(r.sampleID)
			item.getItemProperty("Model").setValue(r.modelName)					
			item.getItemProperty("Call").setValue(callValue)						
			//item.getItemProperty("Confidence").setValue(nullConf)
			item.getItemProperty("Score").setValue(pr)
			item.getItemProperty("Confidence").setValue(nullConf)
			
			//System.err.println "Added item itemID: $itemID"
						
			// Save the first row for initial selection.
			// Probably first itemID is 0 or 1, but save it in case not a guarantee. 
			if (bFirstTime){
				bFirstTime = false;
				firstItemID = itemID; 
			}			
		}	
		return(container)	
	}
	
	def getMaxIdx(list){
		def maxVal = -9999999;
		def maxIdx = 0;
		list.eachWithIndex{val,i->
			if (val > maxVal) {
				maxVal = val
				maxIdx = i
			}
		}
		return(maxIdx)
	}
	
}



/*

FileReader reader = new FileReader(tempFile);
IndexedContainer indexedContainer = buildContainerFromTable(table);
 
dtg = new DoubleTableGrid(table)
table.setCaption(finishedEvent.getFilename());
dtg.setContainerDataSource(indexedContainer);
layout.addComponent(dtg);
dtg.setVisible(true);
 
 
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