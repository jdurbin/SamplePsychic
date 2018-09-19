package durbin.samplepsychic

import com.vaadin.ui.Button
import com.vaadin.ui.Grid
import com.vaadin.ui.GridLayout
import com.vaadin.ui.Label
import com.vaadin.ui.OptionGroup
import com.vaadin.ui.Slider
import com.vaadin.ui.TextField
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.HorizontalLayout
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
import com.vaadin.event.MouseEvents.ClickListener
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.themes.ValoTheme.*;
import com.vaadin.server.Sizeable.Unit.*
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.data.util.filter.Compare
import com.vaadin.data.util.filter.Compare.*;
import com.vaadin.data.Container.*
import com.vaadin.ui.Alignment;
import com.vaadin.data.Property;

import com.jujutsu.tsne.FastTSne;
import com.jujutsu.tsne.MatrixOps;
import com.jujutsu.tsne.TSne;

import at.downdrown.vaadinaddons.highchartsapi.model.series.*;
import at.downdrown.vaadinaddons.highchartsapi.*;
import at.downdrown.vaadinaddons.highchartsapi.exceptions.*;
import at.downdrown.vaadinaddons.highchartsapi.model.*;
import at.downdrown.vaadinaddons.highchartsapi.model.data.*;
import at.downdrown.vaadinaddons.highchartsapi.model.data.base.*;
import at.downdrown.vaadinaddons.highchartsapi.model.series.ScatterChartSeries;
import at.downdrown.vaadinaddons.highchartsapi.model.plotoptions.*;

import grapnel.util.*

class TsneComponent extends VerticalLayout{
	SamplePsychicUI app;
	HeaderRow filterRow;
	
	VerticalLayout chartLayout = new VerticalLayout();

	Button runTSNEButton;
	
	def modelsOrSamples = "Models"
	double perplexity = 50.0;
	
	def TsneComponent(SamplePsychicUI vapp){
		app = vapp			
	}
	
	VerticalLayout buildLayout(){
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setMargin(true);		
		
		GridLayout buttonLayout = new GridLayout(3,1);
		buttonLayout.setSpacing(true)		
		
		runTSNEButton = new Button("Run TSne");
		runTSNEButton.setStyleName("v-button-narrow");
		buttonLayout.addComponent(runTSNEButton);
		
		
		def pSlider = new Slider("Perplexity");
		pSlider.setMin(1.0 as double);
		pSlider.setMax(100.0 as double);
		pSlider.setValue(50.0 as double);
		pSlider.addValueChangeListener({event->
			perplexity = event.getProperty().getValue() as double			
		} as Property.ValueChangeListener)
		buttonLayout.addComponent(pSlider);
		
		// Radio button to choose whether we want points to be 
		// samples or models (e.g. which drugs are most alike given 
		// these samples or which samples are most alike given these 
		// drug responses).
		def clusterBy = new OptionGroup("Cluster Points Are:");
		clusterBy.addItems("Models","Samples"); // Item IDs, really. 
		clusterBy.addValueChangeListener({event->
			def value = event.getProperty().getValue().toString()
			modelsOrSamples = value						
		} as Property.ValueChangeListener)
		clusterBy.setValue(modelsOrSamples)
		clusterBy.addStyleName('mysmalltextstyle');
		clusterBy.addStyleName("horizontal");
 	    buttonLayout.addComponent(clusterBy);
		
		 		 
		runTSNEButton.addClickListener({event->			
			runTSneAndUpdateChart();							
		} as Button.ClickListener)	
		runTSNEButton.addStyleName('mysmalltextstyle');
		
		// placeholder. 
		mainLayout.addComponent(buttonLayout);
		mainLayout.addComponent(chartLayout);
		
		return(mainLayout)	
	}
			
	def runTSneAndUpdateChart(){
		System.err.print "Running tsne..."
		def (Y,t) = runTSne(modelsOrSamples,app.results)
		System.err.println "done."
	
		def chart = createScatterChartFromTSneResults(Y,t)
		chartLayout.removeAllComponents(); // remove previous chart
		chartLayout.addComponent(chart);
		chartLayout.setComponentAlignment(chart, Alignment.MIDDLE_LEFT);
		chartLayout.setExpandRatio(chart, 1.0f);
	}
	
	def getIDAndModelNames(results){
		def idSet = [] as Set
		def modelSet = [] as Set
		results.each{r->
			idSet<<r.sampleID
			modelSet<<r.modelName
		}
		System.err.println idSet
		System.err.println modelSet
		return([idSet,modelSet])
	}
	
	def runTSne(clusterBy,results){
		def idSet,modelSet
		(idSet,modelSet) = getIDAndModelNames(results)
		def t
		
		if (clusterBy == "Models") t = new DoubleTable(modelSet as ArrayList,idSet as ArrayList)
		else t = new DoubleTable(idSet as ArrayList,modelSet as ArrayList)
		
		results.each{r->
			//System.err.println "${r.modelName}\t${r.sampleID}\t"
			//System.err.println "pr: ${r.prForValues[0]}"
			
			if (clusterBy == "Models") t.set(r.modelName,r.sampleID,r.prForValues[0])
			else t.set(r.sampleID,r.modelName,r.prForValues[0])
		}
		
		//double perplexity = 20.0
		int initial_dims = t.cols();		
		def X = t.toArray()
		
		System.err.println("Running TSne...");
		TSne tsne = new FastTSne();
		double [][] Y = tsne.tsne(X, 2, initial_dims, perplexity);
		System.err.println("done. "+Y.length+":"+Y[0].length);
		 
		System.err.println Y
		return([Y,t])
	}
	
	def createScatterChartFromTSneResults(Y,t){
		
		ChartConfiguration scatterConfiguration = new ChartConfiguration();
		scatterConfiguration.setTitle("TSne Cluster Classification Vectors");
		scatterConfiguration.setChartType(ChartType.SCATTER);
		def zoomType = ZoomType.XY;
		scatterConfiguration.setZoomType(ZoomType.XY);
		
		// Populate the data
		List<HighChartsData> values = new ArrayList<>();	
		def xvals = []
		def yvals = []			
		Y.each{
			values.add(new DoubleDoubleData(it[0].round(2),it[1].round(2)));
			xvals.add(it[0] as double)
			yvals.add(it[1] as double)			
		}
		ScatterChartSeries scatterSeries = new ScatterChartSeries("Drugs",values);
		scatterConfiguration.getSeriesList().add(scatterSeries);
		
		// HighCharts will round max and min off off to the nearest tick mark unit..
		// Note that without explicitly setting this, the chart seems to cut off the 
		// rightmost edge (at least I think that is what was doing it). 
		def yAxis = scatterConfiguration.getyAxis()
		def ymax = 600 //yvals.max() + 100				
		def ymin = -600 //yvals.min() + 100		
		yAxis.max = ymax
		yAxis.min = ymin
		//yAxis.tickInterval = ((yAxis.max - yAxis.min)/20) as double
		//yAxis.tickInterval = yAxis.tickInterval.round(0);
		yAxis.tickInterval = 50
		
		def xAxis = scatterConfiguration.getxAxis()
		def xmax = 600  // xvals.max() + 100
		def xmin = -600 // xvals.min() - 100
		xAxis.max = xmax 
		xAxis.min = xmin 
		//xAxis.tickInterval = ((xAxis.max - xAxis.min)/20) as double
		xAxis.tickInterval = 50
		
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

