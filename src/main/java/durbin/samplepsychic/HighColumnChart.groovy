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

class HighColumnChart{
	
	ChartConfiguration columnConfiguration = new ChartConfiguration();
	
	def addDataToSeries(){
	}
		
	def createChart(){	
		ColumnChartSeries columnChartSeries = new ColumnChartSeries("columnScript");		
		
		//columnChartSeries.addData(2);
		columnChartSeries.addData(2.3);
		columnChartSeries.addData(4.3);
		columnChartSeries.addData(3.3);
		columnChartSeries.addData(6.3);
		columnChartSeries.addData(10.3);
		columnChartSeries.addData(8.3);
		columnChartSeries.addData(1.3);
		columnChartSeries.addData(6.3);
		columnChartSeries.addData(2.3);
		columnChartSeries.addData(1.3);
		//columnChartSeries.addData(new DoubleDoubleData(2.3, 45.2));
		//columnChartSeries.addData(new DoubleIntData(2.3, 45));
		//columnChartSeries.addData(new IntDoubleData(2, 45.2));
		//columnChartSeries.addData(new IntIntData(2, 45));
	
			
		columnConfiguration.setChartType(ChartType.COLUMN);						
		columnConfiguration.setZoomType(ZoomType.XY);
		
		// HighCharts will round max and min off to the nearest tick mark unit..
		// Note that without explicitly setting this, the chart seems to cut off the
		// rightmost edge (at least I think that is what was doing it).
		//def yAxis = columnConfiguration.getyAxis()
		//yAxis.max = maxy;
		//yAxis.min = miny;
		//yAxis.tickInterval = 50;
		
		//def xAxis = columnConfiguration.getxAxis()
		//xAxis.max = maxx;
		//xAxis.min = minx;
		//xAxis.tickInterval = 50;
		
		ColumnChartPlotOptions columnChartPlotOptions = new ColumnChartPlotOptions();
		columnChartPlotOptions.setAnimated(false);
		columnChartPlotOptions.setDataLabelsEnabled(false);
		columnConfiguration.setPlotOptions(columnChartPlotOptions);
				
		try {
			HighChart columnChart = HighChartFactory.renderChart(columnConfiguration);
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