package durbin.samplepsychic

import com.vaadin.ui.Label
import com.vaadin.ui.TabSheet
import com.vaadin.ui.VerticalLayout
import com.vaadin.navigator.View
import com.vaadin.event.SelectionEvent.SelectionListener
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Notification;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.server.VaadinServlet;

import at.downdrown.vaadinaddons.highchartsapi.model.series.*;
import at.downdrown.vaadinaddons.highchartsapi.*;
import at.downdrown.vaadinaddons.highchartsapi.exceptions.*;
import at.downdrown.vaadinaddons.highchartsapi.model.*;
import at.downdrown.vaadinaddons.highchartsapi.model.data.*;
import at.downdrown.vaadinaddons.highchartsapi.model.data.base.*;
import at.downdrown.vaadinaddons.highchartsapi.model.series.ScatterChartSeries;
import at.downdrown.vaadinaddons.highchartsapi.model.plotoptions.*;

import com.vaadin.server.Sizeable.*;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import grapnel.weka.*



class TabView extends VerticalLayout implements View{
	
	SamplePsychicUI app;
	def tab,tab1,tab2,tab3,tab4,tab2column
	def TabSheet tabsheet
	def sample2Results
	
	def TabView(SamplePsychicUI vapp){	
		app = vapp
		setSizeFull(); // test copied from VaadinSandbox
		//setMargin(true);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		
		// Will want to rewrite URI with the results temp file...
		String URIFragment = app.getPage().getUriFragment();
		System.err.println("DEBUG TabView URIFragment:"+URIFragment);
		String currentState = app.navigator.getState();
		System.err.println("DEBUG: navigator.state:"+currentState);
		
		System.err.println "ViewTab enter event.parameters= "+event.getParameters()
								
		// Rebuild everything on entry??
		if (tabsheet != null) removeComponent(tabsheet);
		tabsheet = new TabSheet();
		tabsheet.setSizeFull(); // test copied from VaadinSandbox
		addComponent(tabsheet);

		// TAB ResultsGric =======================
		tab = new VerticalLayout();	
		tab.setSpacing(true);
		tab.setMargin(true);	
		def resultGrid = new ClassificationResultsGrid(app)
		resultGrid.setVisible(true);
		tab.addComponent(resultGrid);
		
		def sampleSummaryPanel = new SampleSummaryPanel(app);
		sample2Results = SignatureSet.getResultsBySample(app.results)		
		setupResultGridSelectionListener(resultGrid,sampleSummaryPanel);		
		tab.addComponent(sampleSummaryPanel);
		
		// Hang on, I'm going to try something...
		System.err.println "DEBUG TabView: resultGrid.firstItemID:"+resultGrid.firstItemID
		//resultGrid.fireSelectionEvent([resultGrid.firstItemID],[resultGrid.firstItemID]);
		
		//tab.addComponent(new Label("""
//<p>Histogram or waterfall plot of each sample will be shown when you click on a sample. 			
//			""",ContentMode.HTML));
		tabsheet.addTab(tab,"AllResults");
 
		
		// TAB1 Per-Sample Report=======================
		ReportCard asr  = new ReportCard(app);
		VerticalLayout summaryLayout = asr.buildLayout();
		tabsheet.addTab(summaryLayout,"ReportCard		");							
 		
		// Add two grid sample summary...
		def tgss = new TwoGridSampleSummary(app);
		GridLayout tcssLayout = tgss.buildLayout();
		tabsheet.addTab(tcssLayout,"ExploreSamples");										
		
		
		// TAB3 TSNE Component =======================
		TsneComponent tsne = new TsneComponent(app);
		VerticalLayout tsneLayout = tsne.buildLayout();
		tabsheet.addTab(tsneLayout,"Result Clusters");
		
		
		// TAB4 Tsne mock-up		
		//def fileNames = ["wmmodelmodel_dims117_p8.tab","wmmodelmodel_dims117_p7.tab"]
		//def chart = createScatterChartFromFiles(fileNames);
		//def chart = createScatterChart();
		//tab4 = new VerticalLayout();
		//tab4.addComponent(chart);
		//tab4.setComponentAlignment(chart, Alignment.MIDDLE_LEFT);
		//tab4.setExpandRatio(chart, 1.0f);					
		//tabsheet.addTab(tab4,"Mockup");

	}
	
	
	def setupResultGridSelectionListener(grid,panel){
		grid.addSelectionListener({event->
			def selected = event.getSelected() as ArrayList;
			def selectionID = selected[0]				
			def sampleID = grid.getSampleIDFromSelection(selectionID)	
			def modelName = grid.getFieldFromSelection("Model",selectionID)
			def model = app.signatureSets.modelName2Model[modelName]
			def sampleResults = sample2Results[sampleID]
			// Find the selected result
			sampleResults.each{r->
				if ((r.sampleID == sampleID) && (r.modelName == modelName)){
					panel.update(sampleID,model,r)
				}
			}												
		} as SelectionListener)
	}		
		

	
	def createScatterChartFromFiles(fileNames){
		ChartConfiguration scatterConfiguration = new ChartConfiguration();
		scatterConfiguration.setTitle("Tsne Chart");
		scatterConfiguration.setChartType(ChartType.SCATTER);
		
		def zoomType = ZoomType.XY;
		System.err.println "DEBUG: zoomtype name = "+zoomType.name().toLowerCase()
				
		scatterConfiguration.setZoomType(ZoomType.XY);
				
		fileNames.each{fileName->
			def pathName = VaadinServlet.getCurrent().getServletContext().getRealPath("/tsne/$fileName");
			//def pathName = "/Users/james/sandbox/java/clustering/tsne_java/"+fileName
			
			List<HighChartsData> values = new ArrayList<>();
			new File(pathName).splitEachLine("\t"){fields->	
				def v1 = fields[0] as double
				def v2 = fields[1] as double			
				values.add(new DoubleDoubleData(v1.round(2),v2.round(2)));				
			}					
			ScatterChartSeries scatterSeries = new ScatterChartSeries(fileName,values);
								
			scatterConfiguration.getSeriesList().add(scatterSeries);
		}	
		
		// HighCharts will round max and min off off to the nearest tick mark unit..
		// Note that without explicitly setting this, the chart seems to cut off the 
		// rightmost edge (at least I think that is what was doing it). 
		def yAxis = scatterConfiguration.getyAxis()
		yAxis.max = 600;
		yAxis.min = -600;
		yAxis.tickInterval = 50;
		
		def xAxis = scatterConfiguration.getxAxis()
		xAxis.max = 600;
		xAxis.min = -600;
		xAxis.tickInterval = 50;
		
		ScatterChartPlotOptions scatterChartPlotOptions = new ScatterChartPlotOptions();
		scatterChartPlotOptions.setAnimated(false);
		scatterChartPlotOptions.setDataLabelsEnabled(false);
		scatterConfiguration.setPlotOptions(scatterChartPlotOptions);	
				
		try {
			HighChart scatterChart = HighChartFactory.renderChart(scatterConfiguration);
			// Specifying height and width as percent had some issues for me... 
			// For one, the plot wasn't coming out square when 60% was width and height
			// presumably because they are percents of different dimensions??
			//scatterChart.setHeight(70, Unit.PERCENTAGE);
			//scatterChart.setWidth(70, Unit.PERCENTAGE);
			scatterChart.setHeight(600, Unit.PIXELS);
			scatterChart.setWidth(600, Unit.PIXELS);
			return(scatterChart);
		} catch (HighChartsException e) {
			e.printStackTrace();
		}
	}
	
	
}