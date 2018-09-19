package durbin.samplepsychic

import com.vaadin.data.Container.*
import com.vaadin.data.Container.Filterable
import com.vaadin.data.Item
import com.vaadin.data.util.filter.Compare
import com.vaadin.data.util.filter.Compare.*;
import com.vaadin.data.util.filter.SimpleStringFilter
import com.vaadin.data.util.IndexedContainer
import com.vaadin.event.FieldEvents.TextChangeEvent
import com.vaadin.event.FieldEvents.TextChangeListener
import com.vaadin.event.SelectionEvent.SelectionListener
import com.vaadin.server.Sizeable.Unit.*
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.grid.HeightMode
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Grid
import com.vaadin.ui.Grid.HeaderCell
import com.vaadin.ui.Grid.HeaderRow
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.Notification
import com.vaadin.ui.Panel
import com.vaadin.ui.TextField
import com.vaadin.ui.themes.ValoTheme.*;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.shared.ui.MarginInfo

import at.downdrown.vaadinaddons.highchartsapi.HighChart;
import at.downdrown.vaadinaddons.highchartsapi.HighChartFactory;
import at.downdrown.vaadinaddons.highchartsapi.exceptions.HighChartsException;
import at.downdrown.vaadinaddons.highchartsapi.model.ChartConfiguration;
import at.downdrown.vaadinaddons.highchartsapi.model.ChartType;
import at.downdrown.vaadinaddons.highchartsapi.model.data.HighChartsData;
import at.downdrown.vaadinaddons.highchartsapi.model.data.base.DoubleData;
import at.downdrown.vaadinaddons.highchartsapi.model.plotoptions.ColumnChartPlotOptions;
import at.downdrown.vaadinaddons.highchartsapi.model.series.ColumnChartSeries;
import at.downdrown.vaadinaddons.highchartsapi.model.data.base.DoubleDoubleData;

import grapnel.weka.*;
import weka.core.*;
import weka.filters.*;
import swiftml.*;
import hep.aida.ref.Histogram1D;


public class SampleSummaryPanel extends Panel{
	
	VerticalLayout contentLayout;
	SamplePsychicUI app;
	Grid geneGrid;
	Panel descriptionPanel;
						
	def SampleSummaryPanel(SamplePsychicUI mapp){
		this.app = mapp				
		setCaption("Sample:\tClassifier:")				
		setSizeUndefined(); // Shrink to fit content
		contentLayout = new VerticalLayout();
		contentLayout.setWidth("860px");
		//contentLayout.setSpacing(true);
		//contentLayout.setMargin(true);
		contentLayout.addComponent(new Label("", ContentMode.HTML));
		setContent(contentLayout)
	}
		
	// r = result
	// Builds a new sample result summary panel for a given selected sample. 
	// 
	def update(sampleID,model,r){
		setCaption("Sample:\t${sampleID}")
		setSizeUndefined(); // Shrink to fit content
		
		//def (bestCallValue,bestIdx) = r.callAndIdx()		
		def callValue = r.classValues[r.preferredIdx] 
				
		def bnm = model.bnm
		def dynamicbin = bnm.nullDistribution[r.preferredIdx]
		def values = dynamicbin.elements()
		def elements = values.elements() as ArrayList
		def pr = r.prForValues[r.preferredIdx] // probability.
		pr = pr.round(3)		
		// Idx looks up one of two null distributions
		// then computes significance of value pr on that distribution. 
		def nullConf = model.bnm.getSignificance(pr,r.preferredIdx)				
		nullConf = nullConf.round(3)
							
		contentLayout = new VerticalLayout();
		contentLayout.setWidth("860px");
		contentLayout.setSpacing(true);		
		
		// Left side of split will be a number of stats and such
		// Right side will be a plot. 
		def majorSplit = new HorizontalLayout();
		majorSplit.setWidth("100%");
		//majorSplit.setMargin(true);
		majorSplit.setMargin(new MarginInfo(false, true, false, true)); // top right bottom left
		
		def statsList = new VerticalLayout();
		def modelSet = app.signatureSets.modelName2Set[r.modelName];
				
		statsList.addComponent(new Label("<small><b>Model: </b>${r.modelName}</small>",ContentMode.HTML));
		statsList.addComponent(new Label("<small><b>Call: </b>${callValue}</small>",ContentMode.HTML));
		statsList.addComponent(new Label("<small><b>Classifier Score: </b>${pr}</small>",ContentMode.HTML));	
		statsList.addComponent(new Label("<small><b>Background Confidence: </b>${nullConf}</small>",ContentMode.HTML));	
		statsList.addComponent(new Label("<small><b>Background: </b>${modelSet.backgroundDescription}</small>",ContentMode.HTML));	
		majorSplit.addComponent(statsList)
		
		def hc = new HistogramChart()
		hc.height=120
		hc.width = 240
		def chart = hc.createChart(elements,pr)
		
		majorSplit.addComponent(chart)	
		majorSplit.setComponentAlignment(chart, Alignment.MIDDLE_RIGHT);
		
		contentLayout.addComponent(majorSplit)
				
		def majorSplit2 = new HorizontalLayout();
		majorSplit2.setWidth("100%");
		//majorSplit2.setMargin(true);
		majorSplit2.setMargin(new MarginInfo(false, true, true, true)); // top right bottom left

		// Get gene list from model...
		def feature2score = WekaClassifierInfo.getFeatures(model.classifier);
		// If we didn't get a valid mapping, just list the genes in 
		// the model with uniform weight...
		if (feature2score == null){
			feature2score = [:]
			model.attributes().each{feature2score[it] = 1.0 as double}
		}
		geneGrid = buildGeneGrid(feature2score)		
						
		majorSplit2.addComponent(geneGrid)

		descriptionPanel = new Panel("Gene Description");
		//descriptionPanel.setSizeUndefined(); // Shrink to fit content
		descriptionPanel.setWidth("400px")
		descriptionPanel.setHeight("200px")
		def descriptionLayout = new VerticalLayout()
		descriptionLayout.setWidth("100%");
		descriptionLayout.setSizeUndefined() // enable scroll bars?
		descriptionLayout.setSpacing(true)
		descriptionLayout.addComponent(new Label("", ContentMode.HTML));
		descriptionPanel.setContent(descriptionLayout);
		majorSplit2.addComponent(descriptionPanel)
		
		// Set up listener so that clicks in geneGrid updates descriptionPanel
		setupGridSelectionListener()				
		
		// Unofficial way to force a selection event to happen.
		//geneGrid.fireSelectionEvent([1],[1]);
		//geneGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
		//geneGrid.select(1);
				
		contentLayout.addComponent(majorSplit2)
					
		setContent(contentLayout)
	}
	
	def buildGeneGrid(feature2score){
		System.err.println "buildGeneGrid"
		def container = buildGeneContainer(feature2score)
		def geneGrid = new Grid();		
		geneGrid.setContainerDataSource(container)
		geneGrid.setCaption("Gene Weights")
		geneGrid.setWidth("300px");
		double numRows = 7;
		geneGrid.setHeightMode(HeightMode.ROW);
		geneGrid.setHeightByRows(numRows)				
		//setColumnFiltering(true);				
		geneGrid.setVisible(true);						
		return(geneGrid)
	}
	
	def buildGeneContainer(feature2score){
		System.err.println "buildGeneContainer"
		def container = new IndexedContainer()		
		container.addContainerProperty("Gene",String.class,null);
		container.addContainerProperty("Weight",Double.class,null);
		feature2score.each{f,s->
			def itemID = container.addItem();
			Item item = container.getItem(itemID);
			//System.err.println "gene: $f\t$s"
			item.getItemProperty("Gene").setValue(f as String);
			item.getItemProperty("Weight").setValue(s);
		}		
		return(container)
	}	
	
	def setupGridSelectionListener(){
		System.err.println "setupGridSelectionListener"
		geneGrid.addSelectionListener({event->
			def selected = event.getSelected() as ArrayList;
			def selectionID = selected[0]							
			def item = geneGrid.getContainerDataSource().getItem(selectionID)
			def geneName = item.getItemProperty("Gene").getValue()
			def description = app.gene2description[geneName]
			System.err.println "[$geneName]\t[$description]"
			if (description == null) description = "No description available."
			
			// Use Html styling to shrink text.
			description = "<small>$description</small>"
			def descriptionLayout = new VerticalLayout()
			descriptionLayout.setWidth("400px");
			//descriptionLayout.setSizeUndefined() // enable scroll bars?
			descriptionLayout.setMargin(true)
			descriptionLayout.addComponent(new Label(description,ContentMode.HTML));
			descriptionPanel.setContent(descriptionLayout)
			descriptionPanel.setVisible(true);
		} as SelectionListener)
	}	
	
								
}