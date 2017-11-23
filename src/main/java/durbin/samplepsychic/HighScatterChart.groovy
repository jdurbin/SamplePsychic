package durbin.samplepsychic

import com.vaadin.ui.Label
import com.vaadin.ui.TabSheet
import com.vaadin.ui.VerticalLayout
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Notification;
import com.vaadin.shared.ui.label.ContentMode;

import at.downdrown.vaadinaddons.highchartsapi.model.series.*;

import at.downdrown.vaadinaddons.highchartsapi.*;
import at.downdrown.vaadinaddons.highchartsapi.exceptions.*;
import at.downdrown.vaadinaddons.highchartsapi.model.*;
import at.downdrown.vaadinaddons.highchartsapi.model.ZoomType;
import at.downdrown.vaadinaddons.highchartsapi.model.data.*;
import at.downdrown.vaadinaddons.highchartsapi.model.data.base.*;
import at.downdrown.vaadinaddons.highchartsapi.model.series.ScatterChartSeries;
import at.downdrown.vaadinaddons.highchartsapi.model.plotoptions.*;

import com.vaadin.server.Sizeable.*;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import durbin.weka.*

class HighScatterChart {
	
	ChartConfiguration scatterConfiguration = new ChartConfiguration();
	def maxx;
	def maxy;
	def minx;
	def miny;
	
	def addTestDataToSeries(fileNames){
		fileNames.each{fileName->
			def pathName = "/Users/james/sandbox/java/clustering/tsne_java/"+fileName
			
			List<HighChartsData> values = new ArrayList<>();
			new File(pathName).splitEachLine("\t"){fields->
				def v1 = fields[0] as double
				def v2 = fields[1] as double
				values.add(new DoubleDoubleData(v1.round(2),v2.round(2)));
			}
			ScatterChartSeries scatterSeries = new ScatterChartSeries(fileName,values);
								
			scatterConfiguration.getSeriesList().add(scatterSeries);
		}		
		scatterConfiguration.setTitle("Test Chart");
	}
	
	def addXYDataToSeries(seriesName,xdata,ydata){	
		System.err.println("Adding values to series: ");
		List<HighChartsData> values = new ArrayList<>();
		maxx = xdata.max()
		minx = xdata.min()
		maxy = ydata.max()
		miny = ydata.min()
		xdata.eachWithIndex{x,i->	
			Double xval = x	
			Double yval = ydata[i]		
			System.err.println "Adding $i\t$xval\t$yval"	
			values.add(new DoubleDoubleData(xval.round(2),yval.round(2)));
		}
		ScatterChartSeries scatterSeries = new ScatterChartSeries(seriesName,values);								
		scatterConfiguration.getSeriesList().add(scatterSeries);
	}
	
	def createChart(fileNames){		
		scatterConfiguration.setChartType(ChartType.SCATTER);
		
		def zoomType = ZoomType.XY;
		System.err.println "DEBUG: zoomtype name = "+zoomType.name().toLowerCase()
				
		scatterConfiguration.setZoomType(ZoomType.XY);				
		
		// HighCharts will round max and min off to the nearest tick mark unit..
		// Note that without explicitly setting this, the chart seems to cut off the
		// rightmost edge (at least I think that is what was doing it).
		def yAxis = scatterConfiguration.getyAxis()
		yAxis.max = maxy;
		yAxis.min = miny;
		yAxis.tickInterval = 50;
		
		def xAxis = scatterConfiguration.getxAxis()
		xAxis.max = maxx;
		xAxis.min = minx;
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
