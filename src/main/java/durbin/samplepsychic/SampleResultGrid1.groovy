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

class SampleResultGrid1 extends Grid{
	//ArrayList classifierResults;
	def sample2count;
	double cutoff = 0.95
	def selectionSampleID;
	
	def SampleResultGrid1(sample2count){
		System.err.println "SRG1 constructor IN"
		this.sample2count = sample2count
				
		init(sample2count)		
		
		this.setCaption("Samples")
		this.setWidth("425px");
		double numRows = 10;
		this.setHeightMode(HeightMode.ROW);
		this.setHeightByRows(numRows)				
		//setColumnFiltering(true);				
		this.setVisible(true);												
		System.err.println "SRG1 constructor "
	}
	
	def init(sample2count){				
		IndexedContainer indexedContainer = buildContainer(sample2count);
		this.setContainerDataSource(indexedContainer)
		this.setVisible(true);
	}
	
	def getSampleIDFromSelection(selectionID){
		def item = getContainerDataSource().getItem(selectionID)
		def sampleID = item.getItemProperty("Sample ID").getValue()
		System.err.println "DEBUG: selectionID: $selectionID sampleID from selection: "+sampleID
		return(sampleID)
	}		
	
	//def update(bestResults,sample2count){
	//	this.removeAllColumns()
	//	init(bestResults,sample2count)
	//}
	
	def buildContainer(sample2count){
		def container = new IndexedContainer()		
		container.addContainerProperty("Sample ID",String.class,null);
		def countPropertyName = "Matches" as String
		container.addContainerProperty(countPropertyName,Integer.class,null);
		sample2count.each{s,c->
			def itemID = container.addItem();
			Item item = container.getItem(itemID);
			item.getItemProperty("Sample ID").setValue(s as String);
			item.getItemProperty(countPropertyName).setValue(c);
		}		
		return(container)
	}			
}
