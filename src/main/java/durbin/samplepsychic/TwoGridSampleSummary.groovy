package durbin.samplepsychic

import java.awt.Image
import java.awt.image.BufferedImage
import java.util.ArrayList;
import java.util.List;

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
import com.vaadin.shared.ui.grid.HeightMode;

import com.vaadin.data.Item
import com.vaadin.data.Property.ValueChangeListener
import com.vaadin.data.util.IndexedContainer
import com.vaadin.event.SelectionEvent.SelectionListener
import com.vaadin.server.FileDownloader
import com.vaadin.server.FileResource
import com.vaadin.server.Resource
import com.vaadin.server.StreamResource
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button
import com.vaadin.ui.ComboBox
import com.vaadin.ui.Component
import com.vaadin.ui.FormLayout
import com.vaadin.ui.Grid
import com.vaadin.ui.GridLayout
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Alignment.*;
import com.vaadin.ui.Grid.SingleSelectionModel

import grapnel.util.CounterMap
import grapnel.weka.*;
import weka.core.*;
import weka.filters.*;
import swiftml.*;

import hep.aida.ref.Histogram1D;

/**
 * Test functionality for samplepsychic summary pane.
 *
 * @author james
 */
class TwoGridSampleSummary {
	
	SamplePsychicUI app;
	ClassifierCompendium cc;
	GridLayout mainLayout;
	def bestResults;
	def sample2Results;
	def grid1;
	def grid2;
	
	double cutoff = 0.51;
		
	def TwoGridSampleSummary(SamplePsychicUI vapp){
		app = vapp;
		cc = app.compendium;
	}
	
	GridLayout buildLayout(){	
		GridLayout mainLayout = new GridLayout(2, 2);
		
//		mainLayout = new VerticalLayout();
//		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		mainLayout.setMargin(true);
		// top, right, bottom, left. 
		//mainLayout.setMargin(new MarginInfo(false, true, true,true));
		//mainLayout.setExpandRatio(contentPanel,1f);
		
		//def controlBar = new HorizontalLayout();
		//def comboBox = buildSampleComboBox(app.classifierResults);
		//controlBar.addComponent(comboBox);		
		
		//mainLayout.addComponent(controlBar);
		
		
		// CREATE GRID1
		def sampleSelectorLayout = new HorizontalLayout();
		sampleSelectorLayout.setSpacing(true);
		sampleSelectorLayout.setMargin(true);
				
		bestResults = cc.getBestResults(app.results,cutoff)
		def sample2count = getSample2Count(bestResults)												
		grid1 = new SampleResultGrid1(sample2count);	
		
		// CREATE GRID2
		sample2Results = cc.getResultsBySample(bestResults)		
		// TEMP debug
		def sampleIDs = sample2Results.keySet()	as ArrayList
		def sampleID = sampleIDs[0]
		def sampleResults = sample2Results[sampleID]
		grid2 = new SampleResultGrid2(sampleResults,cc);	

		// CREATE INFO PANEL
		// Wrap in a layout to add margin/spacing
		def sampleSummaryPanel = new SampleSummaryPanel(app);
		
		//def sampleSummaryLayout = new HorizontalLayout();
		//sampleSummaryLayout.setSpacing(true);
		//sampleSummaryLayout.setMargin(true);
		//sampleSummaryLayout.addComponent(sampleSummaryPanel)
						
		//sampleSelectorLayout.addComponent(grid1);	
//		sampleSelectorLayout.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML)); // empty space
		//sampleSelectorLayout.addComponent(grid2);	

		// Instead of adding label spacer, try right justifying it...
		//sampleSelectorLayout.setComponentAlignment(grid2, Alignment.MIDDLE_RIGHT);
						
		mainLayout.addComponent(grid1,0,0)
		mainLayout.addComponent(grid2,1,0)
		
		//mainLayout.setComponentAlignment(grid2, Alignment.TOP_RIGHT);
//		mainLayout.addComponent(sampleSelectorLayout,0,0)
		mainLayout.addComponent(sampleSummaryPanel,0,1,1,1)
										
		// Hook up event listeners... grid2 changes 
		// based on selection in grid1  
		setupGrid1SelectionListener(grid1,grid2,sampleSummaryPanel)
		setupGrid2SelectionListener(grid2,sampleSummaryPanel)
		return(mainLayout);				
	}	
				
	def setupGrid1SelectionListener(grid1,grid2,panel){
		grid1.addSelectionListener({event->
			def selected = event.getSelected() as ArrayList;
			def selectionID = selected[0]				
			def sampleID = grid1.getSampleIDFromSelection(selectionID)						
			def sample2Results = cc.getResultsBySample(bestResults)
			grid2.update(sample2Results[sampleID])						
		} as SelectionListener)
	}		
	
	def setupGrid2SelectionListener(grid2,panel){
		grid2.addSelectionListener({event->
			def selected = event.getSelected() as ArrayList;			
			// Detects case where selection is triggered by selection1 event 
			// deleting and replacing the entire grid2 table.  In that case
			// this selection event gets called inside the grid selection listener
			// but it won't yet have any data.  
			if (selected == null ) return;
			if (selected.size() == 0) return;
			
			def selectionID = selected[0]				
			def sampleID = grid2.getFieldFromSelection("Sample ID",selectionID)	
			def modelName = grid2.getFieldFromSelection("Model Name",selectionID)	
			System.err.println "Grid2 Selection Listener $sampleID"			
			def model = cc.modelName2Model[modelName]	
			def results = sample2Results[sampleID]
			
			// Find the selected result
			results.each{r->
				if ((r.sampleID == sampleID) && (r.modelName == modelName)){
					panel.update(sampleID,model,r)
				}
			}
		} as SelectionListener)
	}		
	
	
	def getSample2Count(results){
		def sample2count = new CounterMap();
		results.each{r->
			sample2count.inc(r.sampleID)			
		}
		return(sample2count)		
	}
}